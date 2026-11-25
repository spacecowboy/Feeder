package com.nononsenseapps.feeder.archmodel

import com.nononsenseapps.feeder.db.room.Folder
import com.nononsenseapps.feeder.db.room.FolderDao
import com.nononsenseapps.feeder.db.room.FolderDisplayItem
import com.nononsenseapps.feeder.db.room.SelectableFolder
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class FolderStoreTest : DIAware {
    private val store: FolderStore by instance()

    @MockK
    private lateinit var dao: FolderDao

    override val di by DI.lazy {
        bind<FolderDao>() with instance(dao)
        bind<FolderStore>() with singleton { FolderStore(di) }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
    }

    @Test
    fun allFolders() {
        every { dao.loadAllSelectableFolders() } returns
            flowOf(
                listOf(
                    SelectableFolder(1L, "Folder", 0),
                ),
            )

        val folders =
            runBlocking {
                store.allFolders.toList().first()
            }

        assertEquals(
            listOf(
                SelectableFolder(1L, "Folder", 0),
            ),
            folders,
        )
    }

    @Test
    fun deleteFolder() {
        coEvery { dao.deleteFolder(5L) } returns 1

        runBlocking {
            store.deleteFolder(5L)
        }

        coVerify {
            dao.deleteFolder(5L)
        }
    }

    @Test
    fun getFolder() {
        coEvery { dao.getFolder(5L) } returns Folder(id = 5L)

        val folder =
            runBlocking {
                store.getFolder(5L)
            }

        assertEquals(
            Folder(id = 5L),
            folder,
        )
    }

    @Test
    fun insertOrIgnoreFolder() {
        coEvery { dao.insertOrIgnoreFolder("Test", 3L) } returns 4L

        val id =
            runBlocking {
                store.insertOrIgnoreFolder("Test", 3L)
            }

        assertEquals(
            4L,
            id,
        )
    }

    @Test
    fun getAllSubfolderIds() {
        coEvery { dao.getAllSubfolderIds(5L) } returns listOf(3L, 5L)

        val ids =
            runBlocking {
                store.getAllSubfolderIds(5L)
            }

        assertEquals(
            listOf(3L, 5L),
            ids,
        )
    }

    @Test
    fun getFolderNameFlow() {
        every { dao.getFolderNameFlow(5L) } returns flowOf("folder")

        val result =
            runBlocking {
                store.getFolderNameFlow(5L).toList()
            }

        assertEquals(
            "folder",
            result.first(),
        )
    }

    @Test
    fun renameFolder() {
        coEvery { dao.renameFolder(5L, "folder") } returns 1

        runBlocking {
            store.renameFolder(5L, "folder")
        }

        coVerify {
            dao.renameFolder(5L, "folder")
        }
    }

    @Test
    fun moveFolder() {
        coEvery { dao.moveFolder(5L, 3L) } returns 1

        runBlocking {
            store.moveFolder(5L, 3L)
        }

        coVerify {
            dao.moveFolder(5L, 3L)
        }
    }

    @Test
    fun toggleFolderExpansion() {
        runBlocking {
            store.toggleFolderExpansion(5L)
        }

        coVerify {
            dao.toggleFolderExpansion(5L)
        }
    }

    @Test
    fun getFolderPathStringList() {
        coEvery { dao.getFolderPathStringList(5L) } returns listOf("folder1", "folder2")

        val pathList =
            runBlocking {
                store.getFolderPathStringList(5L)
            }

        assertEquals(
            listOf("folder1", "folder2"),
            pathList,
        )
    }

    @Test
    fun folderPathStringToFolderId() {
        coEvery { dao.folderPathStringToFolderId(listOf("folder1", "folder2")) } returns 5L

        val id =
            runBlocking {
                store.folderPathStringToFolderId(listOf("folder1", "folder2"))
            }

        assertEquals(
            5L,
            id,
        )
    }

    @Test
    fun getFolderDisplayItemsPath() {
        every { dao.getFolderDisplayItemsPath(5L) } returns
            flowOf(
                listOf(
                    FolderDisplayItem(3L, "folder1"),
                    FolderDisplayItem(5L, "folder2"),
                ),
            )

        val result =
            runBlocking {
                store.getFolderDisplayItemsPath(5L).toList()
            }

        assertEquals(
            listOf(
                FolderDisplayItem(3L, "folder1"),
                FolderDisplayItem(5L, "folder2"),
            ),
            result.first(),
        )
    }

    @Test
    fun getFolderDisplayItemsInFolder() {
        every { dao.getFolderDisplayItemsInFolder(5L) } returns
            flowOf(
                listOf(
                    FolderDisplayItem(3L, "folder1"),
                    FolderDisplayItem(5L, "folder2"),
                ),
            )

        val result =
            runBlocking {
                store.getFolderDisplayItemsInFolder(5L).toList()
            }

        assertEquals(
            listOf(
                FolderDisplayItem(3L, "folder1"),
                FolderDisplayItem(5L, "folder2"),
            ),
            result.first(),
        )
    }

    @Test
    fun getFolderNamesFolderCannotBeRenamedTo() {
        every { dao.getFolderNamesFolderCannotBeRenamedTo(5L) } returns
            flowOf(
                listOf("folder1", "folder2"),
            )

        val result =
            runBlocking {
                store.getFolderNamesFolderCannotBeRenamedTo(5L).toList()
            }

        assertEquals(
            listOf("folder1", "folder2"),
            result.first(),
        )
    }

    @Test
    fun getFolderIdsFoldersCannotBeMovedInto() {
        every { dao.getFolderIdsFoldersCannotBeMovedInto(listOf(5L)) } returns
            flowOf(
                listOf(3L, 5L),
            )

        val result =
            runBlocking {
                store.getFolderIdsFoldersCannotBeMovedInto(listOf(5L)).toList()
            }

        assertEquals(
            listOf(3L, 5L),
            result.first(),
        )
    }
}
