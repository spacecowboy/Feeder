package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET

interface OPMLParserHandler {
    suspend fun saveFeed(
        feed: Feed,
        folderId: Long,
    )

    suspend fun saveFolder(
        name: String,
        parentId: Long,
    ): Long

    suspend fun saveSetting(
        key: String,
        value: String,
    )

    suspend fun saveBlocklistPatterns(patterns: Iterable<String>)
}

interface ImportNode {
    suspend fun save(
        folderId: Long = ID_UNSET,
        opmlToDb: OPMLParserHandler,
    )
}

data class ImportFeed(
    val feed: Feed,
) : ImportNode {
    override suspend fun save(
        folderId: Long,
        opmlToDb: OPMLParserHandler,
    ) {
        opmlToDb.saveFeed(feed, folderId)
    }
}

data class ImportFolder(
    val name: String,
    val children: MutableList<ImportNode> = mutableListOf(),
) : ImportNode {
    override suspend fun save(
        folderId: Long,
        opmlToDb: OPMLParserHandler,
    ) {
        val newFolderId = opmlToDb.saveFolder(name, folderId)

        children.forEach { node ->
            node.save(newFolderId, opmlToDb)
        }
    }
}
