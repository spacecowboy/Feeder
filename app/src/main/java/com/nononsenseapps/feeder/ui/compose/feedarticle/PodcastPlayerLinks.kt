package com.nononsenseapps.feeder.ui.compose.feedarticle

import com.nononsenseapps.feeder.archmodel.Enclosure
import java.net.URLDecoder

private val AUDIO_FILE_EXTENSIONS =
    setOf(
        "aac",
        "flac",
        "m4a",
        "m4b",
        "mp3",
        "mp4",
        "oga",
        "ogg",
        "opus",
        "wav",
        "weba",
    )

fun shouldOpenInPodcastPlayer(
    link: String,
    enclosure: Enclosure,
): Boolean {
    if (link.isBlank() || link.startsWith("#")) {
        return false
    }

    if (link == enclosure.link && enclosure.type.startsWith("audio/")) {
        return true
    }

    val path = lastPathSegment(link).lowercase()
    val extension = path.substringAfterLast('.', missingDelimiterValue = "")

    return extension in AUDIO_FILE_EXTENSIONS
}

fun audioTitleFromLink(
    link: String,
    fallback: String = "",
): String {
    val decoded = decodeUrlComponent(lastPathSegment(link)).trim()

    return when {
        decoded.isNotBlank() -> decoded
        fallback.isNotBlank() -> fallback
        else -> link
    }
}

private fun lastPathSegment(link: String): String =
    link
        .substringBefore('#')
        .substringBefore('?')
        .substringAfterLast('/', "")

private fun decodeUrlComponent(value: String): String =
    runCatching {
        URLDecoder.decode(value, "UTF-8")
    }.getOrDefault(value)
