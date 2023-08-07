package com.nononsenseapps.feeder.sync

import android.util.Log
import coil.network.HttpException
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.crypto.AesCbcWithIntegrity
import com.nononsenseapps.feeder.crypto.SecretKeys
import com.nononsenseapps.feeder.db.room.DEFAULT_SERVER_ADDRESS
import com.nononsenseapps.feeder.db.room.DEPRECATED_SYNC_HOSTS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItemForReadMark
import com.nononsenseapps.feeder.db.room.RemoteFeed
import com.nononsenseapps.feeder.db.room.SyncDevice
import com.nononsenseapps.feeder.db.room.SyncRemote
import com.nononsenseapps.feeder.db.room.generateDeviceName
import com.nononsenseapps.feeder.util.logDebug
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SyncRestClient(override val di: DI) : DIAware {
    private val repository: Repository by instance()
    private val okHttpClient: OkHttpClient by instance()
    private var feederSync: FeederSync? = null
    private var secretKey: SecretKeys? = null
    private val moshi = getMoshi()
    private val readMarkAdapter = moshi.adapter<ReadMarkContent>()
    private val feedsAdapter = moshi.adapter<EncryptedFeeds>()

    init {
        runBlocking {
            initialize()
        }
    }

    private val isInitialized: Boolean
        get() = feederSync != null && secretKey != null
    private val isNotInitialized
        get() = !isInitialized

    internal suspend fun initialize() {
        if (isNotInitialized) {
            try {
                var syncRemote = repository.getSyncRemote()
                if (DEPRECATED_SYNC_HOSTS.any { host -> host in "${syncRemote.url}" }) {
                    logDebug(
                        LOG_TAG,
                        "Updating to latest sync host: $DEFAULT_SERVER_ADDRESS",
                    )
                    syncRemote = syncRemote.copy(
                        url = URL(DEFAULT_SERVER_ADDRESS),
                    )
                    repository.updateSyncRemote(syncRemote)
                }
                if (syncRemote.hasSyncChain()) {
                    secretKey = AesCbcWithIntegrity.decodeKey(syncRemote.secretKey)
                    feederSync = getFeederSyncClient(
                        syncRemote = syncRemote,
                        okHttpClient = okHttpClient,
                    )
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to initialize", e)
            }
        }
    }

    private suspend fun safeBlock(block: (suspend (SyncRemote, FeederSync, SecretKeys) -> Unit)?) {
        if (block != null) {
            repository.getSyncRemote().let { syncRemote ->
                if (syncRemote.hasSyncChain()) {
                    feederSync?.let { feederSync ->
                        secretKey?.let { secretKey ->
                            block(syncRemote, feederSync, secretKey)
                        }
                    }
                }
            }
        }
    }

    suspend fun create(): String {
        logDebug(LOG_TAG, "create")
        // To ensure always uses correct client, manually set remote here ALWAYS
        var syncRemote = repository.getSyncRemote()

        val secretKey = AesCbcWithIntegrity.decodeKey(syncRemote.secretKey)
        this.secretKey = secretKey

        val feederSync = getFeederSyncClient(
            syncRemote = syncRemote,
            okHttpClient = okHttpClient,
        )
        this.feederSync = feederSync

        val response = feederSync.create(
            CreateRequest(
                deviceName = AesCbcWithIntegrity.encryptString(syncRemote.deviceName, secretKey),
            ),
        )

        syncRemote = syncRemote.copy(
            syncChainId = response.syncCode,
            deviceId = response.deviceId,
            deviceName = generateDeviceName(),
            latestMessageTimestamp = Instant.EPOCH,
        )

        repository.updateSyncRemote(
            syncRemote,
        )

        return response.syncCode
    }

    suspend fun join(syncCode: String, remoteSecretKey: String): String {
        logDebug(LOG_TAG, "join")
        try {
            logDebug(LOG_TAG, "Really joining")
            // To ensure always uses correct client, manually set remote here ALWAYS
            var syncRemote = repository.getSyncRemote()
            syncRemote.secretKey = remoteSecretKey
            syncRemote.deviceName = generateDeviceName()
            repository.updateSyncRemote(syncRemote)

            val secretKey = AesCbcWithIntegrity.decodeKey(syncRemote.secretKey)
            this.secretKey = secretKey

            val feederSync = getFeederSyncClient(
                syncRemote = syncRemote,
                okHttpClient = okHttpClient,
            )
            this.feederSync = feederSync

            logDebug(LOG_TAG, "Updated objects")

            val response = feederSync.join(
                syncChainId = syncCode,
                request = JoinRequest(
                    deviceName = AesCbcWithIntegrity.encryptString(
                        syncRemote.deviceName,
                        secretKey,
                    ),
                ),
            )

            logDebug(LOG_TAG, "Join response: $response")

            syncRemote = syncRemote.copy(
                syncChainId = response.syncCode,
                deviceId = response.deviceId,
                latestMessageTimestamp = Instant.EPOCH,
            )

            repository.updateSyncRemote(
                syncRemote,
            )

            logDebug(LOG_TAG, "Updated sync remote")

            return response.syncCode
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                Log.e(
                    LOG_TAG,
                    "Error during leave: msg: code: ${e.code()}, error: ${
                    e.response()?.errorBody()?.string()
                    }",
                    e,
                )
            } else {
                Log.e(LOG_TAG, "Error during leave", e)
            }
            throw e
        }
    }

    suspend fun leave() {
        logDebug(LOG_TAG, "leave")
        try {
            safeBlock { syncRemote, feederSync, _ ->
                logDebug(LOG_TAG, "Really leaving")
                feederSync.removeDevice(
                    syncChainId = syncRemote.syncChainId,
                    currentDeviceId = syncRemote.deviceId,
                    deviceId = syncRemote.deviceId,
                )
                this.feederSync = null
                this.secretKey = null
            }
        } catch (e: Exception) {
            // Always proceed with database reset
            if (e is retrofit2.HttpException) {
                Log.e(
                    LOG_TAG,
                    "Error during leave: msg: code: ${e.code()}, error: ${
                    e.response()?.errorBody()?.string()
                    }",
                    e,
                )
            } else {
                Log.e(LOG_TAG, "Error during leave", e)
            }
        }
        repository.replaceWithDefaultSyncRemote()
    }

    suspend fun removeDevice(deviceId: Long) {
        safeBlock { syncRemote, feederSync, secretKey ->
            logDebug(LOG_TAG, "removeDevice")
            val deviceListResponse = feederSync.removeDevice(
                syncChainId = syncRemote.syncChainId,
                currentDeviceId = syncRemote.deviceId,
                deviceId = deviceId,
            )

            logDebug(LOG_TAG, "Updating device list: $deviceListResponse")

            repository.replaceDevices(
                deviceListResponse.devices.map {
                    SyncDevice(
                        deviceId = it.deviceId,
                        deviceName = AesCbcWithIntegrity.decryptString(
                            it.deviceName,
                            secretKey,
                        ),
                        syncRemote = syncRemote.id,
                    )
                },
            )
        }
    }

    internal suspend fun markAsRead(feedItems: List<FeedItemForReadMark>) {
        safeBlock { syncRemote, feederSync, secretKey ->
            logDebug(LOG_TAG, "markAsRead: ${feedItems.size} items")
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
                                    ),
                                ),
                            ),
                        )
                    },
                ),
            )
            for (feedItem in feedItems) {
                repository.setSynced(feedItemId = feedItem.id)
            }
            // Should not set latest timestamp here because we cant be sure to retrieved them
        }
    }

    suspend fun markAsRead() {
        safeBlock { _, _, _ ->
            val readItems = repository.getFeedItemsWithoutSyncedReadMark()

            if (readItems.isEmpty()) {
                return@safeBlock
            }

            logDebug(LOG_TAG, "markAsReadBatch: ${readItems.size} items")

            readItems.asSequence()
                .chunked(100)
                .forEach { feedItems ->
                    markAsRead(feedItems)
                }
        }
    }

    internal suspend fun getDevices() {
        logDebug(LOG_TAG, "getDevices")
        safeBlock { syncRemote, feederSync, secretKey ->
            logDebug(LOG_TAG, "getDevices Inside block")
            val response = feederSync.getDevices(
                syncChainId = syncRemote.syncChainId,
                currentDeviceId = syncRemote.deviceId,
            )

            logDebug(LOG_TAG, "getDevices: $response")

            repository.replaceDevices(
                response.devices.map {
                    logDebug(LOG_TAG, "device: $it")
                    SyncDevice(
                        deviceId = it.deviceId,
                        deviceName = AesCbcWithIntegrity.decryptString(
                            it.deviceName,
                            secretKey,
                        ),
                        syncRemote = syncRemote.id,
                    )
                },
            )
        }
    }

    internal suspend fun getRead() {
        safeBlock { syncRemote, feederSync, secretKey ->
            logDebug(LOG_TAG, "getRead")
            val response = feederSync.getEncryptedReadMarks(
                currentDeviceId = syncRemote.deviceId,
                syncChainId = syncRemote.syncChainId,
                // Add one ms so we don't get inclusive of last message we got
                sinceMillis = syncRemote.latestMessageTimestamp.plusMillis(1).toEpochMilli(),
            )
            logDebug(LOG_TAG, "getRead: ${response.readMarks.size} read marks")
            for (readMark in response.readMarks) {
                val readMarkContent = readMarkAdapter.fromJson(
                    AesCbcWithIntegrity.decryptString(readMark.encrypted, secretKey),
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
        safeBlock { syncRemote, feederSync, secretKey ->
            logDebug(LOG_TAG, "getFeeds")
            val response = feederSync.getFeeds(
                syncChainId = syncRemote.syncChainId,
                currentDeviceId = syncRemote.deviceId,
            )

            logDebug(LOG_TAG, "GetFeeds response hash: ${response.hash}")

            if (response.hash == syncRemote.lastFeedsRemoteHash) {
                // Nothing to do
                logDebug(LOG_TAG, "GetFeeds got nothing new, returning.")
                return@safeBlock
            }

            val encryptedFeeds = feedsAdapter.fromJson(
                AesCbcWithIntegrity.decryptString(
                    response.encrypted,
                    secretKeys = secretKey,
                ),
            )

            if (encryptedFeeds == null) {
                Log.e(LOG_TAG, "Failed to decrypt encrypted feeds")
                return@safeBlock
            }

            feedDiffing(encryptedFeeds.feeds)

            syncRemote.lastFeedsRemoteHash = response.hash
            repository.updateSyncRemote(syncRemote)
        }
    }

    private suspend fun feedDiffing(
        remoteFeeds: List<EncryptedFeed>,
    ) {
        logDebug(LOG_TAG, "feedDiffing: ${remoteFeeds.size}")
        val remotelySeenFeedUrls = repository.getRemotelySeenFeeds()

        val feedUrlsWhichWereDeletedOnRemote = remotelySeenFeedUrls
            .filterNot { url -> remoteFeeds.asSequence().map { it.url }.contains(url) }

        logDebug(LOG_TAG, "RemotelyDeleted: ${feedUrlsWhichWereDeletedOnRemote.size}")

        for (url in feedUrlsWhichWereDeletedOnRemote) {
            logDebug(LOG_TAG, "Deleting remotely deleted feed: $url")
            repository.deleteFeed(url)
        }

        for (remoteFeed in remoteFeeds) {
            val seenRemotelyBefore = remoteFeed.url in remotelySeenFeedUrls
            val dbFeed = repository.getFeed(remoteFeed.url)

            when {
                dbFeed == null && !seenRemotelyBefore -> {
                    // Entirely new remote feed
                    logDebug(LOG_TAG, "Saving new feed: ${remoteFeed.url}")
                    repository.saveFeed(
                        remoteFeed.updateFeedCopy(Feed()),
                    )
                }
                dbFeed == null && seenRemotelyBefore -> {
                    // Has been locally deleted, it will be deleted on next call of updateFeeds
                    logDebug(LOG_TAG, "Received update for locally deleted feed: ${remoteFeed.url}")
                }
                dbFeed != null -> {
                    // Update of feed
                    // Compare modification date - only save if remote is newer
                    if (remoteFeed.whenModified > dbFeed.whenModified) {
                        logDebug(LOG_TAG, "Saving updated feed: ${remoteFeed.url}")
                        repository.saveFeed(
                            remoteFeed.updateFeedCopy(dbFeed),
                        )
                    } else {
                        logDebug(
                            LOG_TAG,
                            "Not saving feed because local date trumps it: ${remoteFeed.url}",
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
            },
        )
    }

    suspend fun sendUpdatedFeeds() {
        safeBlock { syncRemote, feederSync, secretKey ->
            logDebug(LOG_TAG, "sendUpdatedFeeds")
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
                logDebug(LOG_TAG, "Feeds haven't changed - so not sending")
                return@safeBlock
            }

            val encrypted = AesCbcWithIntegrity.encryptString(
                feedsAdapter.toJson(
                    EncryptedFeeds(
                        feeds = feeds,
                    ),
                ),
                secretKeys = secretKey,
            )

            logDebug(
                LOG_TAG,
                "Sending updated feeds with locally computed hash: $currentContentHash",
            )
            // Might fail with 412 in case already updated remotely - need to call get
            try {
                val response = feederSync.updateFeeds(
                    syncChainId = syncRemote.syncChainId,
                    currentDeviceId = syncRemote.deviceId,
                    etagValue = syncRemote.lastFeedsRemoteHash.asWeakETagValue(),
                    request = UpdateFeedsRequest(
                        contentHash = currentContentHash,
                        encrypted = encrypted,
                    ),
                )

                // Store hash for future
                syncRemote.lastFeedsRemoteHash = response.hash
                repository.updateSyncRemote(syncRemote)

                logDebug(LOG_TAG, "Received updated feeds hash: ${response.hash}")
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
