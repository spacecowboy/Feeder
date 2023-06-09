package com.nononsenseapps.feeder.model.opml

import android.util.Log
import com.nononsenseapps.feeder.db.room.Feed
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun writeFile(
    path: String,
    tags: Iterable<String>,
    feedsWithTag: suspend (String) -> Iterable<Feed>,
) {
    withContext(Dispatchers.IO) {
        writeOutputStream(FileOutputStream(path), tags, feedsWithTag)
    }
}

suspend fun writeOutputStream(
    os: OutputStream,
    tags: Iterable<String>,
    feedsWithTag: suspend (String) -> Iterable<Feed>,
) {
    try {
        os.bufferedWriter().use {
            it.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            it.write(
                opml {
                    head {
                        title { +"Feeder" }
                    }
                    body {
                        tags.forEach { tag ->
                            if (tag.isEmpty()) {
                                feedsWithTag(tag).forEach { feed ->
                                    outline(
                                        title = escape(feed.displayTitle),
                                        type = "rss",
                                        xmlUrl = escape(feed.url.toString()),
                                    ) {}
                                }
                            } else {
                                outline(title = escape(tag)) {
                                    feedsWithTag(tag).forEach { feed ->
                                        outline(
                                            title = escape(feed.displayTitle),
                                            type = "rss",
                                            xmlUrl = escape(feed.url.toString()),
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }.toString(),
            )
        }
    } catch (e: IOException) {
        Log.e("OmplWriter", "Failed to write stream", e)
    }
}

/**

 * @param s string to escape
 * *
 * @return String with xml stuff escaped
 */
internal fun escape(s: String): String {
    return s.replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

/**

 * @param s string to unescape
 * *
 * @return String with xml stuff unescaped
 */
internal fun unescape(s: String): String {
    return s.replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
}

// OPML DSL

suspend fun opml(init: suspend Opml.() -> Unit): Opml {
    val opml = Opml()
    opml.init()
    return opml
}

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

class TextElement(val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
}

abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    val attributes = linkedMapOf<String, String>()

    protected suspend fun <T : Element> initTag(tag: T, init: suspend T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<$name${renderAttributes()}")
        if (children.isEmpty()) {
            builder.append("/>\n")
        } else {
            builder.append(">\n")
            for (c in children) {
                c.render(builder, "$indent  ")
            }
            builder.append("$indent</$name>\n")
        }
    }

    private fun renderAttributes(): String {
        val builder = StringBuilder()
        for (a in attributes.keys) {
            builder.append(" $a=\"${attributes[a]}\"")
        }
        return builder.toString()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class Opml : TagWithText("opml") {
    init {
        attributes["version"] = "1.1"
    }

    suspend fun head(init: suspend Head.() -> Unit) = initTag(Head(), init)
    suspend fun body(init: suspend Body.() -> Unit) = initTag(Body(), init)
}

class Head : TagWithText("head") {
    suspend fun title(init: suspend Title.() -> Unit) = initTag(Title(), init)
}

class Title : TagWithText("title")

abstract class BodyTag(name: String) : TagWithText(name) {
    suspend fun outline(
        title: String,
        text: String = title,
        type: String? = null,
        xmlUrl: String? = null,
        init: suspend Outline.() -> Unit,
    ) {
        val o = initTag(Outline(), init)
        o.title = title
        o.text = text
        if (type != null) {
            o.type = type
        }
        if (xmlUrl != null) {
            o.xmlUrl = xmlUrl
        }
    }
}

class Body : BodyTag("body")

class Outline : BodyTag("outline") {
    var title: String
        get() = attributes["title"]!!
        set(value) {
            attributes["title"] = value
        }
    var text: String
        get() = attributes["text"]!!
        set(value) {
            attributes["text"] = value
        }
    var type: String
        get() = attributes["type"]!!
        set(value) {
            attributes["type"] = value
        }
    var xmlUrl: String
        get() = attributes["xmlUrl"]!!
        set(value) {
            attributes["xmlUrl"] = value
        }
}
