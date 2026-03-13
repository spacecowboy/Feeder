package com.nononsenseapps.feeder.archmodel

import android.content.SharedPreferences
import com.nononsenseapps.feeder.db.room.BlocklistDao
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import kotlin.test.assertEquals

class PagingSettingsTest : DIAware {
    private val store: SettingsStore by instance()

    @MockK
    private lateinit var sp: SharedPreferences

    @MockK
    private lateinit var blocklistDao: BlocklistDao

    override val di by DI.lazy {
        bind<SharedPreferences>() with instance(sp)
        bind<SettingsStore>() with singleton { SettingsStore(di) }
        bind<BlocklistDao>() with singleton { blocklistDao }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)

        // Necessary globally for enum conversion
        every { sp.getString(PREF_THEME, null) } returns null
        every { sp.getString(PREF_DARK_THEME, null) } returns null
        every { sp.getString(PREF_SORT, null) } returns null
        every { sp.getString(any(), any()) } returns null
        every { sp.getBoolean(any(), any()) } returns false
    }

    @Test
    fun pagingModeSet() {
        store.setIsPagingMode(true)

        verify {
            sp.edit().putBoolean("pref_paging_mode", true).apply()
        }

        assertEquals(true, store.isPagingMode.value, "Expected get to match mock")
    }

    @Test
    fun animatedPagingSet() {
        store.setIsAnimatedPaging(true)

        verify {
            sp.edit().putBoolean("pref_animated_paging", true).apply()
        }

        assertEquals(true, store.isAnimatedPaging.value, "Expected get to match mock")
    }
}
