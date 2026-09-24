package com.nononsenseapps.feeder.archmodel

import com.nononsenseapps.feeder.db.room.Folder
import com.nononsenseapps.feeder.db.room.FolderDao
import com.nononsenseapps.feeder.db.room.FolderDisplayItem
import com.nononsenseapps.feeder.db.room.SelectableFolder
import kotlinx.coroutines.flow.Flow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.getValue

class FolderStore(
    override val di: DI,
) : DIAware {
    private val folderDao: FolderDao by instance()

    val allFolders: Flow<List<SelectableFolder>> = folderDao.loadAllSelectableFolders()

    suspend fun deleteFolder(folderId: Long): Int = folderDao.deleteFolder(folderId)

    suspend fun getFolder(folderId: Long): Folder? = folderDao.getFolder(folderId)

    suspend fun insertOrIgnoreFolder(
        name: String,
        parentId: Long,
    ): Long? = folderDao.insertOrIgnoreFolder(name, parentId)

    suspend fun getAllSubfolderIds(folderId: Long): List<Long> = folderDao.getAllSubfolderIds(folderId)

    fun getFolderNameFlow(folderId: Long): Flow<String?> = folderDao.getFolderNameFlow(folderId)

    suspend fun renameFolder(
        folderId: Long,
        name: String,
    ): Int = folderDao.renameFolder(folderId, name)

    suspend fun moveFolder(
        folderId: Long,
        targetFolderId: Long,
    ): Int = folderDao.moveFolder(folderId, targetFolderId)

    suspend fun toggleFolderExpansion(folderId: Long) = folderDao.toggleFolderExpansion(folderId)

    suspend fun getFolderPathStringList(folderId: Long): List<String> = folderDao.getFolderPathStringList(folderId)

    suspend fun folderPathStringToFolderId(folderPath: List<String>): Long = folderDao.folderPathStringToFolderId(folderPath)

    fun getFolderDisplayItemsPath(folderId: Long): Flow<List<FolderDisplayItem>> = folderDao.getFolderDisplayItemsPath(folderId)

    fun getFolderDisplayItemsInFolder(folderId: Long): Flow<List<FolderDisplayItem>> = folderDao.getFolderDisplayItemsInFolder(folderId)

    fun getFolderNamesFolderCannotBeRenamedTo(folderId: Long): Flow<List<String>> = folderDao.getFolderNamesFolderCannotBeRenamedTo(folderId)

    fun getFolderIdsFoldersCannotBeMovedInto(folderIds: List<Long>): Flow<List<Long>> = folderDao.getFolderIdsFoldersCannotBeMovedInto(folderIds)
}
