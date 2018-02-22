package com.nononsenseapps.feeder.util

import android.database.sqlite.SQLiteDatabase

fun SQLiteDatabase.inTransaction(init: (SQLiteDatabase) -> Unit): Unit {
    beginTransaction()
    try {
        init(this)
        setTransactionSuccessful()
    } finally {
        endTransaction()
    }
}
