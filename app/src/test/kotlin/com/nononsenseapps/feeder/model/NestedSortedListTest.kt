package com.nononsenseapps.feeder.model

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import kotlin.test.assertEquals


inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)

class NestedSortedListTest {

    var callback = mock<TestCallback>()
    lateinit var list: NestedSortedList<String>

    @Before
    fun setup() {
        `when`(callback.compare(anyString(), anyString())).thenCallRealMethod()
        `when`(callback.areContentsTheSame(anyString(), anyString())).thenCallRealMethod()
        `when`(callback.areItemsTheSame(anyString(), anyString())).thenCallRealMethod()

        list = NestedSortedList(String::class.java, callback = callback)
    }

    @Test
    fun basic() {
        list.add("a")
        list.add("b")
        list.add("c")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("a"))
        assertEquals(1, list.indexOf("b"))
        assertEquals(2, list.indexOf("c"))

        list.remove("a")

        assertEquals(2, list.size())
        assertEquals(0, list.indexOf("b"))
        assertEquals(1, list.indexOf("c"))

        list.add("ba")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("b"))
        assertEquals(1, list.indexOf("ba"))
        assertEquals(2, list.indexOf("c"))
    }

    @Test
    fun basicNesting() {
        `when`(callback.getItemLevel("A")).thenReturn(0)
        `when`(callback.getItemLevel("Aa")).thenReturn(1)
        `when`(callback.getItemLevel("Ab")).thenReturn(1)
        `when`(callback.getParentOf("Aa")).thenReturn("A")
        `when`(callback.getParentOf("Ab")).thenReturn("A")

        list.add("A")
        list.add("Aa")
        list.add("Ab")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("Ab"))

        list.expand("A")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("Ab"))

        list.contract("A")

        assertEquals(1, list.size())
        assertEquals(0, list.indexOf("A"))
    }

    @Test
    fun contractNested() {
        `when`(callback.getItemLevel("A")).thenReturn(0)
        `when`(callback.getItemLevel("Aa")).thenReturn(1)
        `when`(callback.getItemLevel("AB")).thenReturn(1)
        `when`(callback.getItemLevel("ABa")).thenReturn(2)
        `when`(callback.getParentOf("Aa")).thenReturn("A")
        `when`(callback.getParentOf("AB")).thenReturn("A")
        `when`(callback.getParentOf("ABa")).thenReturn("AB")

        list.add("Aa")
        list.add("ABa")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("AB"))

        list.expand("AB")

        assertEquals(4, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("AB"))
        assertEquals(3, list.indexOf("ABa"))

        list.contract("A")

        assertEquals(1, list.size())
        assertEquals(0, list.indexOf("A"))
    }

    @Test
    fun removeNested() {
        `when`(callback.getItemLevel("A")).thenReturn(0)
        `when`(callback.getItemLevel("Aa")).thenReturn(1)
        `when`(callback.getItemLevel("AB")).thenReturn(1)
        `when`(callback.getItemLevel("ABa")).thenReturn(2)
        `when`(callback.getParentOf("Aa")).thenReturn("A")
        `when`(callback.getParentOf("AB")).thenReturn("A")
        `when`(callback.getParentOf("ABa")).thenReturn("AB")

        list.add("Aa")
        list.add("ABa")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("AB"))

        list.expand("AB")

        assertEquals(4, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("AB"))
        assertEquals(3, list.indexOf("ABa"))

        list.remove("AB")

        assertEquals(2, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
    }

    @Test
    fun removeItemAtNested() {
        `when`(callback.getItemLevel("A")).thenReturn(0)
        `when`(callback.getItemLevel("Aa")).thenReturn(1)
        `when`(callback.getItemLevel("AB")).thenReturn(1)
        `when`(callback.getItemLevel("ABa")).thenReturn(2)
        `when`(callback.getParentOf("Aa")).thenReturn("A")
        `when`(callback.getParentOf("AB")).thenReturn("A")
        `when`(callback.getParentOf("ABa")).thenReturn("AB")

        list.add("Aa")
        list.add("ABa")

        assertEquals(3, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("AB"))

        list.expand("AB")

        assertEquals(4, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
        assertEquals(2, list.indexOf("AB"))
        assertEquals(3, list.indexOf("ABa"))

        list.removeItemAt(3)

        assertEquals(2, list.size())
        assertEquals(0, list.indexOf("A"))
        assertEquals(1, list.indexOf("Aa"))
    }
}

open class TestCallback: NestedCallback<String>() {
    override fun compare(a: String?, b: String?): Int {
        return when {
            a != null && b != null -> a.toLowerCase().compareTo(b.toLowerCase())
            else -> 0
        }
    }

    override fun getItemLevel(item: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInserted(p0: Int, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMoved(p0: Int, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentOf(item: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRemoved(p0: Int, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChanged(p0: Int, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun areItemsTheSame(a: String?, b: String?): Boolean {
        return a == b
    }

    override fun areContentsTheSame(a: String?, b: String?): Boolean {
        return a == b
    }

    override fun getParentUnreadCount(parent: String, ts: Set<String>): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
