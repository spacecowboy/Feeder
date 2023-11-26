package com.nononsenseapps.feeder.model

/**
 * URL should be absolute at all times
 */
sealed class ThumbnailImage(
    val url: String,
    val width: Int?,
    val height: Int?,
    val fromBody: Boolean,
)

class ImageFromHTML(url: String, width: Int?, height: Int?) :
    ThumbnailImage(url, width, height, fromBody = true) {
    override fun equals(other: Any?): Boolean {
        return if (other is ImageFromHTML) {
            return url == other.url && width == other.width && height == other.height
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + javaClass.simpleName.hashCode()
        result = prime * result + url.hashCode()
        result = prime * result + width.hashCode()
        result = prime * result + height.hashCode()
        return result
    }

    override fun toString(): String {
        return "ImageFromHTML(url='$url', width=$width, height=$height, fromBody=$fromBody)"
    }
}

class EnclosureImage(url: String, width: Int?, height: Int?) :
    ThumbnailImage(url, width, height, fromBody = false) {
    override fun equals(other: Any?): Boolean {
        return if (other is EnclosureImage) {
            return url == other.url && width == other.width && height == other.height
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + javaClass.simpleName.hashCode()
        result = prime * result + url.hashCode()
        result = prime * result + width.hashCode()
        result = prime * result + height.hashCode()
        return result
    }

    override fun toString(): String {
        return "EnclosureImage(url='$url', width=$width, height=$height, fromBody=$fromBody)"
    }
}

class MediaImage(url: String, width: Int?, height: Int?) :
    ThumbnailImage(url, width, height, fromBody = false) {
    override fun equals(other: Any?): Boolean {
        return if (other is MediaImage) {
            return url == other.url && width == other.width && height == other.height
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + javaClass.simpleName.hashCode()
        result = prime * result + url.hashCode()
        result = prime * result + width.hashCode()
        result = prime * result + height.hashCode()
        return result
    }

    override fun toString(): String {
        return "MediaImage(url='$url', width=$width, height=$height, fromBody=$fromBody)"
    }
}
