package com.nononsenseapps.feeder.archmodel

import org.junit.Assert.*
import org.junit.Test

class SessionStoreTest {
    private val store = SessionStore()

    @Test
    fun expandedTags() {
        assertEquals(emptySet<String>(), store.expandedTags.value)

        store.toggleTagExpansion("foo")

        assertEquals(setOf("foo"), store.expandedTags.value)

        store.toggleTagExpansion("foo")

        assertEquals(emptySet<String>(), store.expandedTags.value)
    }
}
