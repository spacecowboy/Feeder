package com.nononsenseapps.feeder.sync

import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.OPEN_ARTICLE_WITH_APPLICATION_DEFAULT
import java.net.URL
import java.time.Instant

data class CreateRequest(
    val deviceName: String,
)

data class JoinRequest(
    val deviceName: String,
)

data class JoinResponse(
    val syncCode: String,
    val deviceId: Long,
)

data class DeviceListResponse(
    val devices: List<DeviceMessage>,
)

data class DeviceMessage(
    val deviceId: Long,
    val deviceName: String,
)

data class GetReadMarksResponse(
    val readMarks: List<ReadMark>,
)

data class GetEncryptedReadMarksResponse(
    val readMarks: List<EncryptedReadMark>,
)

data class ReadMark(
    val timestamp: Instant,
    val feedUrl: URL,
    val articleGuid: String,
)

data class EncryptedReadMark(
    val timestamp: Instant,
    // Of type ReadMarkContent
    val encrypted: String,
)

data class SendReadMarkBulkRequest(
    val items: List<SendReadMarkRequest>,
)

data class SendEncryptedReadMarkBulkRequest(
    val items: List<SendEncryptedReadMarkRequest>,
)

data class SendReadMarkRequest(
    val feedUrl: URL,
    val articleGuid: String,
)

data class SendEncryptedReadMarkRequest(
    val encrypted: String,
)

data class SendReadMarkResponse(
    val timestamp: Instant,
)

data class ReadMarkContent(
    val feedUrl: URL,
    val articleGuid: String,
)

data class UpdateFeedsRequest(
    val contentHash: Int,
    // Of type EncryptedFeeds
    val encrypted: String,
)

data class UpdateFeedsResponse(
    val hash: Int,
)

data class GetFeedsResponse(
    val hash: Int,
    // Of type EncryptedFeeds
    val encrypted: String,
)

data class EncryptedFeeds(
    val feeds: List<EncryptedFeed>,
)

data class EncryptedFeed(
    val url: URL,
    val title: String = "",
    val customTitle: String = "",
    val tag: String = "",
    val folderPath: List<String> = emptyList(),
    val imageUrl: URL? = null,
    val fullTextByDefault: Boolean = false,
    val openArticlesWith: String = OPEN_ARTICLE_WITH_APPLICATION_DEFAULT,
    val alternateId: Boolean = false,
    val fetchOgImages: Boolean = false,
    val blockRules: String = "",
    val allowRules: String = "",
    val whenModified: Instant = Instant.EPOCH,
)

suspend fun Feed.toEncryptedFeed(repository: Repository): EncryptedFeed =
    EncryptedFeed(
        url = url,
        title = title,
        customTitle = customTitle,
        // Keep tag for now to avoid changing it on devices with old version
        // when syncing from device with new version
        tag = oldTag,
        folderPath = repository.getFolderPathStringList(folderId),
        imageUrl = imageUrl,
        fullTextByDefault = fullTextByDefault,
        openArticlesWith = openArticlesWith,
        alternateId = alternateId,
        fetchOgImages = fetchOgImages,
        blockRules = blockRules,
        allowRules = allowRules,
        whenModified = whenModified,
    )

suspend fun EncryptedFeed.updateFeedCopy(
    feed: Feed,
    repository: Repository,
): Feed =
    feed.copy(
        url = url,
        title = title,
        customTitle = customTitle,
        oldTag = tag,
        folderIdNullable =
            repository
                .folderPathStringToFolderId(folderPath)
                .takeUnless { it == ID_UNSET },
        imageUrl = imageUrl,
        fullTextByDefault = fullTextByDefault,
        openArticlesWith = openArticlesWith,
        alternateId = alternateId,
        fetchOgImages = fetchOgImages,
        blockRules = blockRules,
        allowRules = allowRules,
        whenModified = whenModified,
    )
