package com.nononsenseapps.feeder.sync

import android.util.Log
import coil.network.HttpException
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.crypto.AesCbcWithIntegrity
import com.nononsenseapps.feeder.crypto.SecretKeys
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItemForReadMark
import com.nononsenseapps.feeder.db.room.RemoteFeed
import com.nononsenseapps.feeder.db.room.SyncDevice
import com.nononsenseapps.feeder.db.room.SyncRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.threeten.bp.Instant

class SyncRestClient(override val di: DI) : DIAware {
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val repository: Repository by instance()
    private val okHttpClient: OkHttpClient by instance()
    private var feederSync: FeederSync? = null
    private var syncRemote: SyncRemote? = null
    private var secretKey: SecretKeys? = null
    private var foreverJob: Job? = null
    private var readSenderJob: Job? = null
    private var readGetterJob: Job? = null
    private var feedSenderJob: Job? = null
    private val initMutex = Mutex()
    private val moshi = getMoshi()
    private val readMarkAdapter = moshi.adapter<ReadMarkContent>()
    private val feedsAdapter = moshi.adapter<EncryptedFeeds>()

    init {
        applicationCoroutineScope.launch(Dispatchers.IO) {
            initialize(null)
        }
    }

    val isInitialized: Boolean
        get() = syncRemote != null && feederSync != null && secretKey != null
    val isNotInitialized = !isInitialized

    internal suspend fun initialize(block: (suspend (SyncRemote, FeederSync, SecretKeys) -> Unit)?) {
        if (isNotInitialized) {
            initMutex.withLock {
                // Check again
                if (isNotInitialized) {
                    try {
                        syncRemote = repository.getSyncRemote()
                        syncRemote?.let { syncRemote ->
                            secretKey = AesCbcWithIntegrity.decodeKey(syncRemote.secretKey)
                            feederSync = getFeederSyncClient(
                                syncRemote = syncRemote,
                                okHttpClient = okHttpClient
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to initialize", e)
                    }
                }
            }
        }
        if (block != null) {
            syncRemote?.let { syncRemote ->
                feederSync?.let { feederSync ->
                    secretKey?.let { secretKey ->
                        block(syncRemote, feederSync, secretKey)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun runForever() {
        Log.v(LOG_TAG, "runForever")
        foreverJob?.cancel("runForever invoked")
        foreverJob = applicationCoroutineScope.launch(Dispatchers.IO) {
            repository.getSyncRemoteFlow()
                .runningReduce { lastValue, value ->
                    if (value == null) {
                        secretKey = null
                        feederSync = null
                    } else if (lastValue?.url != value.url
                        || lastValue?.syncChainId != value.syncChainId
                        || lastValue?.secretKey != value.secretKey
                    ) {
                        Log.v(LOG_TAG, "runForever: new client")
                        secretKey = AesCbcWithIntegrity.decodeKey(value.secretKey)
                        feederSync = getFeederSyncClient(
                            syncRemote = value,
                            okHttpClient = okHttpClient
                        )
                        // Setting it here AS WELL just to make sure correct client is always set
                        syncRemote = value
                    }

                    if (value == null
                        || lastValue?.url != value.url
                        || value.syncChainId.length != 64
                    ) {
                        Log.v(
                            LOG_TAG,
                            "runForever: deleting read status sync because new sync remote"
                        )
                        repository.deleteAllReadStatusSyncs()
                    }

                    // Always emit the value along
                    value
                }
                .collect { syncRemote ->
                    this@SyncRestClient.syncRemote = syncRemote
                }
        }
        readSenderJob?.cancel("runForever invoked")
        readSenderJob = applicationCoroutineScope.launch(Dispatchers.IO) {
            repository.getFeedItemsWithoutSyncedReadMark()
                .collect { feedItems ->
                    if (feedItems.isEmpty()) {
                        return@collect
                    }

                    try {
                        markAsRead(feedItems)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error in read status push job: ${e.message}", e)
                    }
                }
        }
        readGetterJob?.cancel("runForever invoked")
        readGetterJob = applicationCoroutineScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    getDevices()
                    getFeeds()
                    getRead()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error in readGetterJob", e)
                }
                // TODO think about times and place for this
                delay(60_000L)
            }
        }
        feedSenderJob?.cancel("runForever invoked")
        feedSenderJob = applicationCoroutineScope.launch(Dispatchers.IO) {
            repository.getFlowOfFeedsOrderedByUrl()
                .collectLatest {
                    delay(1000)
                    try {
                        sendUpdatedFeeds()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error in sendFeedsJob", e)
                    }
                }
        }
    }


    fun stopForeverJob() {
        Log.v(LOG_TAG, "stopForeverJob")
        foreverJob?.cancel("stopForever")
        readSenderJob?.cancel("stopForever")
        readGetterJob?.cancel("stopForever")
        feedSenderJob?.cancel("stopForever")
    }

    suspend fun create(): String {
        Log.v(LOG_TAG, "create")
        // To ensure always uses correct client, manually set remote here ALWAYS
        val syncRemote = repository.getSyncRemote()
        this.syncRemote = syncRemote

        val secretKey = AesCbcWithIntegrity.decodeKey(syncRemote.secretKey)
        this.secretKey = secretKey

        val feederSync = getFeederSyncClient(
            syncRemote = syncRemote,
            okHttpClient = okHttpClient
        )
        this.feederSync = feederSync

        val response = feederSync.create(
            CreateRequest(
                deviceName = AesCbcWithIntegrity.encryptString(syncRemote.deviceName, secretKey)
            )
        )

        repository.updateSyncRemote(
            repository.getSyncRemote().copy(
                syncChainId = response.syncCode,
                deviceId = response.deviceId,
                latestMessageTimestamp = Instant.EPOCH
            )
        )

        return response.syncCode
    }

    suspend fun join(syncCode: String, remoteSecretKey: String): String {
        Log.v(LOG_TAG, "join")
        // To ensure always uses correct client, manually set remote here ALWAYS
        val syncRemote = repository.getSyncRemote()
        syncRemote.secretKey = remoteSecretKey
        repository.updateSyncRemote(syncRemote)
        this.syncRemote = syncRemote

        val secretKey = AesCbcWithIntegrity.decodeKey(syncRemote.secretKey)
        this.secretKey = secretKey

        val feederSync = getFeederSyncClient(
            syncRemote = syncRemote,
            okHttpClient = okHttpClient
        )
        this.feederSync = feederSync

        val response = feederSync.join(
            syncChainId = syncCode,
            request = JoinRequest(
                deviceName = AesCbcWithIntegrity.encryptString(syncRemote.deviceName, secretKey)
            )
        )

        repository.updateSyncRemote(
            repository.getSyncRemote().copy(
                syncChainId = response.syncCode,
                deviceId = response.deviceId,
                latestMessageTimestamp = Instant.EPOCH
            )
        )

        return response.syncCode
    }

    suspend fun leave() {
        Log.v(LOG_TAG, "leave")
        try {
            syncRemote?.let { syncRemote ->
                feederSync?.removeDevice(
                    syncChainId = syncRemote.syncChainId,
                    currentDeviceId = syncRemote.deviceId,
                    deviceId = syncRemote.deviceId
                )
            }
        } catch (e: Exception) {
            // Always proceed with database reset
            Log.e(LOG_TAG, "Error during leave", e)
        }
        repository.replaceWithDefaultSyncRemote()
    }

    suspend fun removeDevice(deviceId: Long) {
        Log.v(LOG_TAG, "removeDevice")
        syncRemote?.let { syncRemote ->
            secretKey?.let { secretKey ->
                val deviceListResponse = feederSync?.removeDevice(
                    syncChainId = syncRemote.syncChainId,
                    currentDeviceId = syncRemote.deviceId,
                    deviceId = deviceId
                ) ?: return

                repository.replaceDevices(
                    deviceListResponse.devices.map {
                        SyncDevice(
                            deviceId = it.deviceId,
                            deviceName = AesCbcWithIntegrity.decryptString(
                                it.deviceName,
                                secretKey,
                            ),
                            syncRemote = syncRemote.id
                        )
                    }
                )
            }
        }
    }

    internal suspend fun markAsRead(feedItems: List<FeedItemForReadMark>) {
        Log.v(LOG_TAG, "markAsRead: ${feedItems.size} items")
        initialize { syncRemote, feederSync, secretKey ->
            feederSync.sendEncryptedReadMarks(
                currentDeviceId = syncRemote.deviceId,
                syncChainId = syncRemote.syncChainId,
                request = SendEncryptedReadMarkBulkRequest(
                    items = feedItems.map { feedItem ->
                        SendEncryptedReadMarkRequest(
                            encrypted = AesCbcWithIntegrity.encryptString(
                                secretKeys = secretKey,
                                plaintext = readMarkAdapter.toJson(
                                    ReadMarkContent(
                                        feedUrl = feedItem.feedUrl,
                                        articleGuid = feedItem.guid,
                                    )
                                )
                            )
                        )
//                                SendReadMarkRequest(
//                                    feedUrl = feedItem.feedUrl,
//                                    articleGuid = feedItem.guid,
//                                )
                    }
                )
            )
            for (feedItem in feedItems) {
                repository.setSynced(feedItemId = feedItem.id)
            }
            // Should not set latest timestamp here because we cant be sure to retrieved them
        }
    }

    internal suspend fun getDevices() {
        Log.v(LOG_TAG, "getDevices. Initialized: $isInitialized")
        initialize { syncRemote, feederSync, secretKey ->
            val response = feederSync.getDevices(
                syncChainId = syncRemote.syncChainId,
                currentDeviceId = syncRemote.deviceId
            )

            repository.replaceDevices(
                response.devices.map {
                    Log.v(LOG_TAG, "device: $it")
                    SyncDevice(
                        deviceId = it.deviceId,
                        deviceName = AesCbcWithIntegrity.decryptString(
                            it.deviceName,
                            secretKey
                        ),
                        syncRemote = syncRemote.id,
                    )
                }
            )
        }
    }

    internal suspend fun getRead() {
        Log.v(LOG_TAG, "getRead. Initialized: $isInitialized")
        initialize { syncRemote, feederSync, secretKey ->
            val response = feederSync.getEncryptedReadMarks(
                currentDeviceId = syncRemote.deviceId,
                syncChainId = syncRemote.syncChainId,
                // Add one ms so we don't get inclusive of last message we got
                sinceMillis = syncRemote.latestMessageTimestamp.plusMillis(1).toEpochMilli()
            )
            for (readMark in response.readMarks) {
                val readMarkContent = readMarkAdapter.fromJson(
                    AesCbcWithIntegrity.decryptString(readMark.encrypted, secretKey)
                )

                if (readMarkContent == null) {
                    Log.e(LOG_TAG, "Failed to decrypt readMark content")
                    continue
                }

                repository.remoteMarkAsRead(
                    feedUrl = readMarkContent.feedUrl,
                    articleGuid = readMarkContent.articleGuid,
                )
                repository.updateSyncRemoteMessageTimestamp(readMark.timestamp)
            }
        }
    }

    internal suspend fun getFeeds() {
        Log.v(LOG_TAG, "getFeeds")
        initialize { syncRemote, feederSync, secretKey ->
            val response = feederSync.getFeeds(
                syncChainId = syncRemote.syncChainId,
                currentDeviceId = syncRemote.deviceId,
            )

            Log.v(LOG_TAG, "GetFeeds response hash: ${response.hash}")

            if (response.hash == syncRemote.lastFeedsRemoteHash) {
                // Nothing to do
                Log.v(LOG_TAG, "GetFeeds got nothing new, returning.")
                return@initialize
            }

            val encryptedFeeds = feedsAdapter.fromJson(
                AesCbcWithIntegrity.decryptString(
                    response.encrypted,
                    secretKeys = secretKey,
                )
            )

            if (encryptedFeeds == null) {
                Log.e(LOG_TAG, "Failed to decrypt encrypted feeds")
                return@initialize
            }

            feedDiffing(encryptedFeeds.feeds)

            syncRemote.lastFeedsRemoteHash = response.hash
            this.syncRemote = syncRemote
            repository.updateSyncRemote(syncRemote)
        }
    }

    private suspend fun feedDiffing(
        remoteFeeds: List<EncryptedFeed>,
    ) {
        Log.v(LOG_TAG, "feedDiffing: ${remoteFeeds.size}")
        val remotelySeenFeedUrls = repository.getRemotelySeenFeeds()

        val feedUrlsWhichWereDeletedOnRemote = remotelySeenFeedUrls
            .filterNot { url -> remoteFeeds.asSequence().map { it.url }.contains(url) }

        Log.v(LOG_TAG, "RemotelyDeleted: ${feedUrlsWhichWereDeletedOnRemote.size}")

        for (url in feedUrlsWhichWereDeletedOnRemote) {
            Log.v(LOG_TAG, "Deleting remotely deleted feed: $url")
            repository.deleteFeed(url)
        }

        for (remoteFeed in remoteFeeds) {
            val seenRemotelyBefore = remoteFeed.url in remotelySeenFeedUrls
            val dbFeed = repository.getFeed(remoteFeed.url)

            when {
                dbFeed == null && !seenRemotelyBefore -> {
                    // Entirely new remote feed
                    Log.v(LOG_TAG, "Saving new feed: ${remoteFeed.url}")
                    repository.saveFeed(
                        remoteFeed.updateFeedCopy(Feed())
                    )
                }
                dbFeed == null && seenRemotelyBefore -> {
                    // Has been locally deleted, it will be deleted on next call of updateFeeds
                    Log.v(LOG_TAG, "Received update for locally deleted feed: ${remoteFeed.url}")
                }
                dbFeed != null -> {
                    // Update of feed
                    // Compare modification date - only save if remote is newer
                    if (remoteFeed.whenModified > dbFeed.whenModified) {
                        Log.v(LOG_TAG, "Saving updated feed: ${remoteFeed.url}")
                        repository.saveFeed(
                            remoteFeed.updateFeedCopy(dbFeed)
                        )
                    } else {
                        Log.v(
                            LOG_TAG,
                            "Not saving feed because local date trumps it: ${remoteFeed.url}"
                        )
                    }
                }
            }

        }

        repository.replaceRemoteFeedsWith(
            remoteFeeds.map {
                RemoteFeed(
                    syncRemote = 1L,
                    url = it.url,
                )
            }
        )
    }

    suspend fun sendUpdatedFeeds() {
        Log.v(LOG_TAG, "sendUpdatedFeeds")
        initialize { syncRemote, feederSync, secretKey ->
            val lastRemoteHash = syncRemote.lastFeedsRemoteHash

            // Only send if hash does not match
            // Important to keep iteration order stable - across devices. So sort on URL, not ID or date
            val feeds = repository.getFeedsOrderedByUrl()
                .map { it.toEncryptedFeed() }

            // Yes, List hashCodes are based on elements. Just remember to hash what you send
            // - and not raw database objects
            val currentContentHash = feeds.hashCode()

            if (lastRemoteHash == currentContentHash) {
                // Nothing to do
                Log.v(LOG_TAG, "Feeds haven't changed - so not sending")
                return@initialize
            }

            val encrypted = AesCbcWithIntegrity.encryptString(
                feedsAdapter.toJson(
                    EncryptedFeeds(
                        feeds = feeds,
                    )
                ),
                secretKeys = secretKey,
            )

            Log.v(
                LOG_TAG,
                "Sending updated feeds with locally computed hash: $currentContentHash"
            )
            // Might fail with 412 in case already updated remotely - need to call get
            try {
                val response = feederSync.updateFeeds(
                    syncChainId = syncRemote.syncChainId,
                    currentDeviceId = syncRemote.deviceId,
                    etagValue = syncRemote.lastFeedsRemoteHash.asWeakETagValue(),
                    request = UpdateFeedsRequest(
                        contentHash = currentContentHash,
                        encrypted = encrypted
                    )
                )

                // Store hash for future
                syncRemote.lastFeedsRemoteHash = response.hash
                this.syncRemote = syncRemote
                repository.updateSyncRemote(syncRemote)

                Log.v(LOG_TAG, "Received updated feeds hash: ${response.hash}")
            } catch (e: HttpException) {
                if (e.response.code == 412) {
                    // Need to call get first because updates have happened
                    getFeeds()
                    // Now try again
                    sendUpdatedFeeds()
                } else {
                    Log.e(LOG_TAG, "Error when sending feeds", e)
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error when sending feeds", e)
            }
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_REST_CLIENT"
    }
}

fun Any.asWeakETagValue() =
    "W/\"$this\""
