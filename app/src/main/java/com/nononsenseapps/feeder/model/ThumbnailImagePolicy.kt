package com.nononsenseapps.feeder.model

/**
 * Centralized rules for picking an article thumbnail across feed parsing and
 * optional article-page metadata enrichment.
 */
internal object ThumbnailImagePolicy {
    /**
     * Apply the image coming from the parsed feed entry.
     *
     * Feed-provided non-body images are authoritative. If a previous sync already
     * upgraded the item to a non-body image, keep it unless the feed now provides
     * its own non-body image. Body-derived images are treated as fallback only.
     */
    fun applyParsedEntryImage(
        current: ThumbnailImage?,
        incoming: ThumbnailImage?,
    ): ThumbnailImage? =
        when {
            incoming != null && !incoming.fromBody -> incoming
            current != null && !current.fromBody -> current
            else -> incoming
        }

    /**
     * Only body-derived or missing thumbnails are candidates for og:image
     * enrichment, and only when there is an article URL and the parsed feed
     * did not expose its own image candidate.
     */
    fun shouldFetchOgImage(
        current: ThumbnailImage?,
        articleUrl: String?,
        hasFeedImage: Boolean,
    ): Boolean = articleUrl.isNullOrBlank().not() && !hasFeedImage && (current == null || current.fromBody)

    /**
     * Apply the result of og:image lookup. Existing feed-provided or previously
     * upgraded non-body images always win.
     */
    fun applyOgImage(
        current: ThumbnailImage?,
        ogImage: ThumbnailImage?,
    ): ThumbnailImage? =
        when {
            current != null && !current.fromBody -> current
            ogImage != null -> ogImage
            else -> current
        }
}
