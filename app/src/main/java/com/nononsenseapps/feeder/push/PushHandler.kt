package com.nononsenseapps.feeder.push

import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.crypto.Alan
import com.nononsenseapps.feeder.crypto.EncryptedMessage
import com.nononsenseapps.feeder.db.room.Feed as DbFeed
import com.nononsenseapps.feeder.model.workmanager.requestFeedSync
import com.nononsenseapps.feeder.util.logDebug
import java.net.URL
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.threeten.bp.Instant

class PushHandler(override val di: DI) : DIAware {
    private val repository by instance<Repository>()
    private val alan by instance<Alan>()

    suspend fun onUpdate(update: Update) {
        logDebug(LOG_TAG, "onUpdate")
        val sender = update.sender ?: throw IllegalMessage("Missing sender in update")
        val timestamp = update.timestamp ?: throw IllegalMessage("Missing timestamp")

        repository.updateKnownDeviceLastSeen(sender.endpoint, timestamp = timestamp.toInstant())

        when {
            update.devices != null -> onUpdateDevices(update.devices)
            update.feeds != null -> onUpdateFeeds(sender, update.feeds)
            update.read_marks != null -> onUpdateReadMarks(sender, update.read_marks)
            update.deleted_feeds != null -> onUpdateDeletedFeeds(sender, update.deleted_feeds)
            update.deleted_devices != null -> onUpdateDeletedDevices(update.deleted_devices)
            update.proof_of_life != null -> onUpdateProofOfLife(sender)
            update.snapshot_request != null -> onUpdateSnapshotRequest(
                sender,
                update.snapshot_request
            )
            else -> throw IllegalMessage("None of the oneof was set or this fun is out of date!")
        }
    }

    suspend fun onEncryptedUpdate(encryptedUpdate: EncryptedUpdate) {
        logDebug(LOG_TAG, "onEncryptedUpdate")

        // TODO actual secret key
        val secretKey = ByteArray(0)

        val bytes = alan.decryptMessage(
            encryptedMessage = EncryptedMessage(
                cipherBytes = encryptedUpdate.cipher_text.toByteArray(),
                nonce = encryptedUpdate.nonce.toByteArray(),
            ),
            publicKey = encryptedUpdate.sender_public_key.toByteArray(),
            secretKey = secretKey,
        )
        onUpdate(Update.ADAPTER.decode(bytes))
    }

    private suspend fun onUpdateSnapshotRequest(sender: Device, snapshotRequest: SnapshotRequest) {
        logDebug(LOG_TAG, "onUpdateSnapshotRequest")
        checkIfKnownSender(sender)
        when {
            snapshotRequest.devices_request != null -> repository.broadcastKnownDevices(sender.endpoint)
            snapshotRequest.feeds_request != null -> repository.broadcastFeeds(sender.endpoint)
            else -> throw IllegalMessage("None of the oneof was set or this fun is out of date!")
        }
    }

    private suspend fun onUpdateProofOfLife(sender: Device) {
        logDebug(LOG_TAG, "onUpdateProofOfLife")
        checkIfKnownSender(sender)
        // LastSeen already updated by common code
    }

    private suspend fun onUpdateDeletedDevices(deletedDevices: DeletedDevices) {
        logDebug(LOG_TAG, "onUpdateDeletedDevices")
        repository.deleteDevicesNoBroadcast(deletedDevices.deleted_devices.map { it.endpoint })
    }

    private suspend fun onUpdateDeletedFeeds(sender: Device, deletedFeeds: DeletedFeeds) {
        logDebug(LOG_TAG, "onUpdateDeletedFeeds")
        checkIfKnownSender(sender)
        for (deletedFeed in deletedFeeds.deleted_feeds) {
            repository.deleteFeedNoBroadcast(URL(deletedFeed.url))
        }
    }

    private suspend fun onUpdateReadMarks(sender: Device, readMarks: ReadMarks) {
        logDebug(LOG_TAG, "onUpdateReadMarks")
        checkIfKnownSender(sender)
        checkIfKnownFeeds(readMarks.read_marks.map { it.feed_url })

        repository.markAsReadByPush(readMarks.read_marks)
    }

    private suspend fun onUpdateFeeds(sender: Device, feeds: Feeds) {
        logDebug(LOG_TAG, "onUpdateFeeds")
        checkIfKnownSender(sender)

        for (feed in feeds.feeds) {
            when (val dbFeed = repository.getFeed(URL(feed.url))) {
                null -> {
                    // New feed
                    val feedId = repository.saveFeedNoBroadcast(
                        feed.updateDbModel(DbFeed())
                    )
                    requestFeedSync(
                        di = di,
                        feedId = feedId
                    )
                }
                else -> {
                    // Existing feed - only update if newer
                    if (dbFeed.whenModified.isBefore(
                            feed.modified_at?.toInstant() ?: Instant.EPOCH
                        )
                    ) {
                        repository.saveFeedNoBroadcast(
                            feed.updateDbModel(dbFeed)
                        )
                    }
                }
            }
        }
    }

    private suspend fun onUpdateDevices(devices: Devices) {
        logDebug(LOG_TAG, "onUpdateDevices")
        repository.updateKnownDevices(devices.devices.map { it.toKnownDevice() })
    }

    private suspend fun checkIfKnownSender(sender: Device) {
        logDebug(LOG_TAG, "checkIfKnownSender")
        val knownDevice = repository.getKnownDevice(endpoint = sender.endpoint)
        val unknownSender = (knownDevice?.name ?: "").isEmpty()

        if (unknownSender) {
            // It already exists in the database due to last_seen update.
            // This sender is new to me. Better request a snapshot. Maybe I missed the introduction
            repository.requestSnapshotDevices(fallbackEndpoint = sender.endpoint)
        }
    }

    private suspend fun checkIfKnownFeeds(feedUrls: List<String>) {
        logDebug(LOG_TAG, "checkIfKnownFeeds")
        val knownFeeds = repository.getFeedUrls().map { it.toString() }

        val unknownFeeds = feedUrls.filterNot { knownFeeds.contains(it) }

        if (unknownFeeds.isNotEmpty()) {
            // One or more new feeds. Better request a snapshot.
            repository.requestSnapshotFeeds()
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_PUSHHANDLER"
    }
}

class IllegalMessage(message: String?) : java.lang.RuntimeException(message)

/*
fun DIAware.sendPushMessage(msg: String) {
    val okHttpClient by instance<OkHttpClient>()

    val bytes = msg.encodeToByteArray()
    val body = bytes.toRequestBody()
    val request = Request.Builder()
        .url("https://ntfy.sh/up0xKJNXvQFH6W?up=1")
        .post(body)
        .build()

    try {
        logDebug(LOG_TAG, "Posting ${bytes.count()} bytes")
        okHttpClient.newCall(request).execute().use { response ->
            logDebug(LOG_TAG, "Response: ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.e("UNIFIED", "BOOM", e)
    }

    val foo = Device()
    foo.encode()
}
 */

fun Feed.updateDbModel(dbFeed: DbFeed) = dbFeed.copy(
    url = URL(url),
    title = title.ifEmptyOrNull { dbFeed.title },
    customTitle = custom_title,
    tag = tag,
    imageUrl = image_url?.let { URL(it) } ?: dbFeed.imageUrl,
    fullTextByDefault = full_text_by_default,
    openArticlesWith = open_articles_with,
    alternateId = alternate_id,
)

fun DbFeed.toProto() = Feed(
    url = url.toString(),
    title = title,
    custom_title = customTitle,
    tag = tag,
    image_url = imageUrl?.toString(),
    full_text_by_default = fullTextByDefault,
    open_articles_with = openArticlesWith,
    alternate_id = alternateId,
    modified_at = whenModified.toProto(),
)

fun String?.ifEmptyOrNull(block: () -> String): String =
    when (this) {
        null -> block()
        else -> this
    }
