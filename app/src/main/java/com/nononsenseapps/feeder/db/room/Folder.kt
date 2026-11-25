package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_EXPANDED
import com.nononsenseapps.feeder.db.COL_FOLDER_NAME
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_PARENT_ID
import com.nononsenseapps.feeder.db.FOLDERS_TABLE_NAME

@Entity(
    tableName = FOLDERS_TABLE_NAME,
    indices = [
        Index(value = [COL_PARENT_ID, COL_FOLDER_NAME], unique = true),
        Index(value = [COL_PARENT_ID]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = [COL_ID],
            childColumns = [COL_PARENT_ID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Folder
    @Ignore
    constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = COL_ID)
        var id: Long = ID_UNSET,
        @ColumnInfo(name = COL_PARENT_ID)
        var parentIdNullable: Long? = null,
        @ColumnInfo(name = COL_FOLDER_NAME)
        var name: String = "",
        @ColumnInfo(name = COL_EXPANDED)
        var expanded: Boolean = false,
    ) {
        constructor() : this(id = ID_UNSET)

        val parentId: Long
            get() = parentIdNullable ?: ID_UNSET
    }
