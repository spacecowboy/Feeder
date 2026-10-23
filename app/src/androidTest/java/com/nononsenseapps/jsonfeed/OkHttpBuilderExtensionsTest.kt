package com.nononsenseapps.jsonfeed

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class OkHttpBuilderExtensionsTest {
    @Test
    fun legacyTlsProtocolsAreRemovedWhileSupportedAndFutureProtocolsRemainEnabled() {
        val filteredProtocols =
            filterLegacyTlsProtocols(
                arrayOf(
                    "SSL",
                    "SSLv2Hello",
                    "SSLv3",
                    "sSlCustom",
                    "TLSv1",
                    "TLSv1.0",
                    "TLSv1.1",
                    "TLSv1.2",
                    "TLSv1.3",
                    "TLSv9.0",
                ),
            )

        assertEquals(listOf("TLSv1.2", "TLSv1.3", "TLSv9.0"), filteredProtocols.toList())
    }

    @Test
    fun legacyTlsProtocolFilteringFailsClosedWhenNothingAcceptableRemains() {
        assertFailsWith<IOException> {
            filterLegacyTlsProtocols(
                arrayOf("SSLv3", "TLSv1", "TLSv1.0", "TLSv1.1"),
            )
        }
    }
}
