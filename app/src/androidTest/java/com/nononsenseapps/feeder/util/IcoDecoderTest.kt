package com.nononsenseapps.feeder.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.danielrampelt.coil.ico.IcoDecoder
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.buffer
import okio.source
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@SmallTest
class IcoDecoderTest {
    private val factory = IcoDecoder.Factory(ApplicationProvider.getApplicationContext())

    @Test
    fun testPngFavicon() {
        val decoder =
            factory.create(
                pngIco,
                Options(ApplicationProvider.getApplicationContext()),
                ImageLoader(ApplicationProvider.getApplicationContext()),
            )

        assertNotNull(decoder)
        val result =
            runBlocking {
                decoder.decode()
            }
        assertNotNull(result)
    }

    @Test
    fun testGitlabIco() {
        val decoder =
            factory.create(
                pngIco,
                Options(ApplicationProvider.getApplicationContext()),
                ImageLoader(ApplicationProvider.getApplicationContext()),
            )

        assertNotNull(decoder)
        val result =
            runBlocking {
                decoder.decode()
            }
        assertNotNull(result)
    }

    companion object {
        private val gitlabIco: SourceFetchResult
            get() {
                val buf =
                    Companion::class.java
                        .getResourceAsStream("gitlab.ico")!!
                        .source()
                        .buffer()

                val imageSource =
                    ImageSource(
                        source = buf,
                        fileSystem = FileSystem.SYSTEM,
                    )

                return SourceFetchResult(
                    source = imageSource,
                    mimeType = null,
                    DataSource.DISK,
                )
            }

        private val pngIco: SourceFetchResult
            get() {
                val buf =
                    Companion::class.java
                        .getResourceAsStream("png.ico")!!
                        .source()
                        .buffer()

                val imageSource =
                    ImageSource(
                        source = buf,
                        fileSystem = FileSystem.SYSTEM,
                    )

                return SourceFetchResult(
                    source = imageSource,
                    mimeType = null,
                    DataSource.DISK,
                )
            }
    }
}
