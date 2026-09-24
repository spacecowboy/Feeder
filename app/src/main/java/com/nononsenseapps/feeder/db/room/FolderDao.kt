package com.nononsenseapps.feeder.db.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: Folder): Long

    suspend fun insertFolder(
        name: String,
        parentId: Long,
    ): Long =
        insertFolder(
            Folder(
                parentIdNullable = parentId.takeUnless { it == ID_UNSET },
                name = name,
                expanded = false,
            ),
        )

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long): Int

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolder(folderId: Long): Folder?

    @Query("SELECT * FROM folders WHERE name = :name AND parent_id IS NULLIF(:parentId, $ID_UNSET)")
    suspend fun getFolder(
        name: String,
        parentId: Long,
    ): Folder?

    /**
     * This function prevents duplicate names for parentId = null while `@Insert` with `OnConflictStrategy.IGNORE` does not
     * because [sqlite treats nulls as distinct](https://sqlite.org/lang_createindex.html#unique_indexes) by default for unique indexes.
     */
    @Transaction
    suspend fun insertOrIgnoreFolder(
        name: String,
        parentId: Long,
    ): Long? =
        when (getFolder(name, parentId)) {
            null -> insertFolder(name, parentId)
            else -> null
        }

    @Transaction
    suspend fun insertOrGetFolder(
        name: String,
        parentId: Long,
    ): Long =
        getFolder(name, parentId)?.id
            ?: insertFolder(name, parentId)

    @Query("SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId")
    suspend fun getAllSubfolderIds(folderId: Long): List<Long>

    @Query("SELECT * FROM folders WHERE parent_id IS NULLIF(:folderId, $ID_UNSET) ORDER BY name COLLATE NOCASE")
    suspend fun getChildFolders(folderId: Long): List<Folder>

    @Query("SELECT name FROM folders WHERE id = :folderId")
    suspend fun getFolderName(folderId: Long): String?

    @Query("SELECT name FROM folders WHERE id = :folderId")
    fun getFolderNameFlow(folderId: Long): Flow<String?>

    @Query(
        """
        UPDATE folders
        SET name = :name
        WHERE id = :folderId
        AND NOT EXISTS(
            SELECT 1 FROM folders f
            WHERE folders.parent_id IS f.parent_id AND f.name = :name
                AND id <> :folderId
        )
        """,
    )
    suspend fun renameFolder(
        folderId: Long,
        name: String,
    ): Int

    @Query(
        """
        UPDATE folders
        SET parent_id = NULLIF(:targetFolderId, $ID_UNSET)
        WHERE id = :folderId
        AND NOT EXISTS(
            SELECT 1 FROM folders f
            WHERE f.parent_id IS NULLIF(:targetFolderId, $ID_UNSET) AND folders.name = f.name
                AND id <> :folderId
        )
        """,
    )
    suspend fun moveFolder(
        folderId: Long,
        targetFolderId: Long,
    ): Int

    @Query("SELECT expanded FROM folders WHERE id = :folderId")
    suspend fun isFolderExpanded(folderId: Long): Boolean?

    @Query("UPDATE folders SET expanded = :value WHERE id = :folderId")
    suspend fun setFolderExpanded(
        folderId: Long,
        value: Boolean,
    )

    @Query(
        """
        UPDATE folders SET expanded = :value
        WHERE id IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId)
        """,
    )
    suspend fun setAllFoldersExpanded(
        folderId: Long,
        value: Boolean,
    )

    @Transaction
    suspend fun toggleFolderExpansion(folderId: Long) =
        isFolderExpanded(folderId)?.let { expanded ->
            if (expanded) {
                setAllFoldersExpanded(folderId, false)
            } else {
                setFolderExpanded(folderId, true)
            }
        }

    @Query(
        """
        SELECT name
        FROM folder_closures
        LEFT JOIN folders
        ON ancestor_id = id
        WHERE descendant_id = :folderId
        ORDER BY depth DESC
        """,
    )
    suspend fun getFolderPathStringList(folderId: Long): List<String>

    @Transaction
    suspend fun folderPathStringToFolderId(folderPath: List<String>): Long {
        var folderId = ID_UNSET
        folderPath.forEach { name ->
            folderId = insertOrGetFolder(name, folderId)
        }
        return folderId
    }

    @Query(
        """
        SELECT id, name
        FROM folder_closures
        LEFT JOIN folders
        ON ancestor_id = id
        WHERE descendant_id = :folderId
        ORDER BY depth DESC
        """,
    )
    fun getFolderDisplayItemsPath(folderId: Long): Flow<List<FolderDisplayItem>>

    @Query(
        """
        SELECT id, name
        FROM folders
        WHERE parent_id IS NULLIF(:folderId, $ID_UNSET)
        ORDER BY name COLLATE NOCASE
        """,
    )
    fun getFolderDisplayItemsInFolder(folderId: Long): Flow<List<FolderDisplayItem>>

    @Query(
        """
        WITH RECURSIVE folder_tree AS (
            SELECT id, parent_id, name, 0 AS depth
            FROM folders
            WHERE parent_id IS NULL
            UNION ALL
            SELECT f.id, f.parent_id, f.name, p.depth + 1
            FROM folders f
            JOIN folder_tree p
            ON f.parent_id IS p.id
            ORDER BY depth DESC, name COLLATE NOCASE
        )
        SELECT COALESCE(id, $ID_UNSET) AS id, name, depth
        FROM folder_tree
        """,
    )
    fun loadAllSelectableFolders(): Flow<List<SelectableFolder>>

    @Query(
        """
        SELECT name
        FROM folders
        WHERE parent_id IS (SELECT parent_id FROM folders WHERE id = :folderId)
        AND id <> :folderId
        -- Don't return names from the root folder if the specified folderId does not exist
        AND EXISTS(SELECT 1 FROM folders WHERE id = :folderId)
        """,
    )
    fun getFolderNamesFolderCannotBeRenamedTo(folderId: Long): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT COALESCE(parent_id, $ID_UNSET) AS not_allowed_id
        FROM folders f
        WHERE name IN (SELECT name FROM folders WHERE f.id <> id AND id IN (:folderIds))
        UNION
        SELECT DISTINCT descendant_id AS not_allowed_id
        FROM folder_closures
        WHERE ancestor_id IN (:folderIds)
        """,
    )
    fun getFolderIdsFoldersCannotBeMovedInto(folderIds: List<Long>): Flow<List<Long>>

    @Query(
        """
        SELECT ancestor_id
        FROM folder_closures
        WHERE descendant_id IN (:folderIds)
        GROUP BY ancestor_id
        HAVING COUNT(descendant_id) = :folderCount
        ORDER BY MAX(depth) ASC
        LIMIT 1
        """,
    )
    suspend fun getClosestAncestorContainingAllFolders(
        folderIds: Set<Long>,
        folderCount: Int = folderIds.size,
    ): Long?

    @Query(
        """
        WITH RECURSIVE reachable_folders AS (
            SELECT id
            FROM folders
            WHERE parent_id IS NULL
            UNION ALL
            SELECT f.id
            FROM folders f
            JOIN reachable_folders p
            ON f.parent_id IS p.id
        )
        UPDATE folders
        SET parent_id = NULL
        WHERE id IN (
            SELECT id FROM folders
            EXCEPT
            SELECT id FROM reachable_folders
        )
        """,
    )
    suspend fun fixFoldersNotReachableFromRoot()

    // For Tests only
    @Query(
        "SELECT * FROM folders",
    )
    fun getAllFolders(): List<Folder>
}
