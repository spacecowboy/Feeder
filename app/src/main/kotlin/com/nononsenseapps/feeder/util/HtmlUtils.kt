package com.nononsenseapps.feeder.util

import java.util.regex.Pattern

//val patternImgSrc = Pattern.compile("img\b.*?src=\"(.*?)\"")
private val patternImgSrc: Pattern = Pattern.compile("img.*?src=[\"'](.*?)[\"']")

fun naiveFindImageLink(text: String?): String? {
    if (text != null) {
        val matcher = patternImgSrc.matcher(text)

        if (matcher?.find() == true) {
            return matcher.group(1).toString()
        }
    }

    return null
}
