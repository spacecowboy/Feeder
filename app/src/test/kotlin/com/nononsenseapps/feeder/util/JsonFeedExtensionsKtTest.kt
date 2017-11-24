package com.nononsenseapps.feeder.util

import android.content.ContentProviderOperation
import com.nononsenseapps.feeder.db.COL_AUTHOR
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.jsonfeed.Author
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class JsonFeedExtensionsKtTest {
    @Test
    fun itemsGetFeedAuthorSetIfNull() {
        val item = Item(id = "foo")
        val feed = Feed(title = "fooFeed", author = Author(name = "feedAuthor"), items = listOf(item))

        val builder = mock(ContentProviderOperation.Builder::class.java)
        `when`(builder.withValue(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(builder)

        item.intoContentProviderOperation(feed, builder)

        verify(builder).withValue(COL_AUTHOR, "feedAuthor")
    }

    @Test
    fun itemRelativeImageIsMadeAbsolute1() {
        val item = Item(id = "foo", image = "a/b.png")
        val feed = Feed(title = "fooFeed", feed_url = "http://site.com/index.xml", items = listOf(item))

        val builder = mock(ContentProviderOperation.Builder::class.java)
        `when`(builder.withValue(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(builder)

        item.intoContentProviderOperation(feed, builder)

        verify(builder).withValue(COL_IMAGEURL, "http://site.com/a/b.png")
    }

    @Test
    fun itemRelativeImageIsMadeAbsolute2() {
        val item = Item(id = "foo", image = "/a/b.png")
        val feed = Feed(title = "fooFeed", feed_url = "http://site.com/index.xml", items = listOf(item))

        val builder = mock(ContentProviderOperation.Builder::class.java)
        `when`(builder.withValue(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(builder)

        item.intoContentProviderOperation(feed, builder)

        verify(builder).withValue(COL_IMAGEURL, "http://site.com/a/b.png")
    }

    @Test
    fun itemAbsoluteImageNotChanged() {
        val item = Item(id = "foo", image = "http://site.com/b.png")
        val feed = Feed(title = "fooFeed", feed_url = "http://site.com/index.xml", items = listOf(item))

        val builder = mock(ContentProviderOperation.Builder::class.java)
        `when`(builder.withValue(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(builder)

        item.intoContentProviderOperation(feed, builder)

        verify(builder).withValue(COL_IMAGEURL, "http://site.com/b.png")
    }
}
