package com.nononsenseapps.feeder.util

import android.content.Context
import java.io.File
import java.util.*


private val DEFAULT_MAX_LINES = 1000

fun d(context: Context, line: String) {
    FileLog.singleton.getInstance(context).d(line)
}

class FileLog internal constructor(val logFile: File, val maxLines: Int) {
    companion object singleton {
        private var instance: FileLog? = null

        @Synchronized
        fun getInstance(context: Context): FileLog {
            if (instance == null) {
                instance = FileLog(File(context.filesDir, "feeder.log"), DEFAULT_MAX_LINES)
            }
            return instance!!
        }
    }

    private val memLog: Queue<String>

    init {
        this.memLog = readLog()
    }

    private fun readLog(): Queue<String> {
        val log = ArrayDeque<String>()
        if (logFile.exists()) {
            logFile.forEachLine { log.add(it) }
        }
        ensureSize(log)
        return log
    }

    private fun ensureSize(log: Queue<String>) {
        while (log.size > maxLines) {
            log.remove()
        }
    }

    val log: String
        get() {
            ensureSize(memLog)
            return memLog.fold("") { acc, it ->
                acc + it + "\n"
            }
        }

    private fun persist() {
        ensureSize(memLog)
        logFile.printWriter().use { pw ->
            memLog.forEach {
                pw.println(it)
            }
        }
    }

    @Synchronized
    fun d(line: String) {
        memLog.add(line)
        ensureSize(memLog)
        persist()
    }
}
