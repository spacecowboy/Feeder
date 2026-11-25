package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    value = """
    WITH RECURSIVE recursive_folder_closures AS (
        SELECT id AS ancestor_id, id AS descendant_id, 0 AS depth
        FROM folders
        UNION ALL
        SELECT cte.ancestor_id, f.id AS descendant_id, cte.depth + 1
        FROM folders f
        JOIN recursive_folder_closures cte
        ON f.parent_id = cte.descendant_id
            AND f.id <> cte.ancestor_id
    )
    SELECT ancestor_id, descendant_id, depth
    FROM recursive_folder_closures
    """,
    viewName = "folder_closures",
)
data class FolderClosures(
    @ColumnInfo(name = "ancestor_id")
    val ancestorId: String,
    @ColumnInfo(name = "descendant_id")
    val descendantId: Long,
    val depth: Int,
)
