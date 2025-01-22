package com.nononsenseapps.feeder.util

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri

fun openGithubIssues(): Intent =
    Intent(ACTION_VIEW).also {
        it.data = Uri.parse("https://github.com/spacecowboy/feeder/issues")
    }

const val KOFI_URL = "https://ko-fi.com/spacecowboy"

fun openKoFiIntent(): Intent =
    Intent(ACTION_VIEW).also {
        it.data = Uri.parse(KOFI_URL)
    }
