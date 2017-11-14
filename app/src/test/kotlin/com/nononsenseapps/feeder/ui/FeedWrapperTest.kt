package com.nononsenseapps.feeder.ui

import com.nononsenseapps.feeder.db.FeedSQL
import org.junit.Test
import kotlin.test.assertTrue

class FeedWrapperTest {
    @Test
    fun topSortsBefore() {
        val top = FeedWrapper(tag = "", isTop = true)

        val z = FeedWrapper(tag = "z")
        assertTrue { top < z }
        assertTrue { z > top}

        val zf = FeedWrapper(item = FeedSQL(tag = "z"))
        assertTrue { top < zf }
        assertTrue { zf > top}

        val f = FeedWrapper(item = FeedSQL(tag = ""))
        assertTrue { top < f }
        assertTrue { f > top}
    }

    @Test
    fun tagsSort() {
        val middle = FeedWrapper(tag = "middle")

        // All sort after
        val z = FeedWrapper(tag = "z")
        assertTrue(middle < z, "$middle should < $z" )
        assertTrue(z > middle, "$z should > $middle")

        val zf = FeedWrapper(item = FeedSQL(tag = "z"))
        assertTrue(middle < zf, "$middle should < $zf" )
        assertTrue(zf > middle, "$zf should > $middle")

        val f = FeedWrapper(item = FeedSQL(tag = ""))
        assertTrue(middle < f, "$middle should < $f" )
        assertTrue(f > middle, "$f should > $middle")

        // All sort before
        val a = FeedWrapper(tag = "a")
        assertTrue(middle > a, "$middle should > $a" )
        assertTrue(a < middle, "$a should < $middle")

        val a2 = FeedWrapper(item = FeedSQL(tag = "a"))
        assertTrue(a2 <= a, "$a2 should = $a" )
        assertTrue(a <= a2, "$a should = $a2")

        val af = FeedWrapper(item = FeedSQL(tag = "a"))
        assertTrue(middle > af, "$middle should > $af" )
        assertTrue(af < middle, "$af should < $middle")
    }

    @Test
    fun feedsSort() {
        val fa = FeedWrapper(item = FeedSQL(title = "1", tag = "a"))
        val fa2 = FeedWrapper(item = FeedSQL(title = "2", tag = "a"))
        val fz = FeedWrapper(item = FeedSQL(tag = "z"))
        val f = FeedWrapper(item = FeedSQL(title = "bob"))

        assertTrue(fa < fa2, "$fa should < $fa2" )
        assertTrue(fa2 > fa, "$fa2 should > $fa" )

        assertTrue(fa < fz, "$fa should < $fz" )
        assertTrue(fz > fa, "$fz should > $fa" )

        assertTrue(fa < f, "$fa should < $f" )
        assertTrue(f > fa, "$f should > $fa" )

        assertTrue(fz < f, "$fz should < $f" )
        assertTrue(f > fz, "$f should > $fz" )

        val f2 = FeedWrapper(item = FeedSQL(title = "zod"))

        assertTrue(f < f2, "$f should < $f2" )
        assertTrue(f2 > f, "$f2 should > $f" )
    }
}
