package com.nononsenseapps.feeder.model

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.SAXParserFactory

private const val XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace"
private const val ATOM_LINK_REL_ALTERNATE = "alternate"

/**
 * Resolved xml:base information for one Atom <entry>.
 *
 * @param base  Effective base URI for the entry itself (entry xml:base resolved against the feed
 *              base, or the feed base when no entry-level xml:base was declared).
 * @param resolvedAlternateHref  Fully resolved href of the first alternate (or untyped) <link>
 *   element inside this entry, with any element-level xml:base on that <link> taken into account.
 *   Null when no such link element was present in the source XML.  This field is provided so that
 *   an element-level xml:base on <link> can be respected even though the underlying Go parser
 *   does not expose xml:base information.
 */
data class AtomEntryInfo(
    val base: URL,
    val resolvedAlternateHref: String?,
)

/**
 * Effective xml:base context for an Atom feed: the feed-level base and per-entry info,
 * in document order.
 */
data class AtomXmlBases(
    /** Effective base URI for the feed itself. Equals feedUrl when no xml:base was declared. */
    val feedBase: URL,
    /**
     * Per-entry information, in document order (entry 0 corresponds to the first <entry> in the
     * feed XML, which also maps to index 0 in the Go-parsed items list).
     */
    val entries: List<AtomEntryInfo>,
)

/**
 * Parses only xml:base and link-href attributes from an Atom feed body and returns the effective
 * base URL and resolved alternate-link href for each <entry>, in document order.
 *
 * On any parse error the function returns a safe default (feedUrl for all entries) rather than
 * propagating the exception, because feed parsing should be resilient.
 *
 * @param body     Raw Atom XML bytes.
 * @param feedUrl  The URL from which the feed was fetched; used as the root base URI per
 *                 RFC 3986 / the XML Base spec.
 */
fun extractAtomXmlBases(
    body: ByteArray,
    feedUrl: URL,
): AtomXmlBases =
    try {
        val factory =
            SAXParserFactory.newInstance().apply {
                isNamespaceAware = true
                // Harden against XXE and entity-expansion attacks.
                // Each setFeature is wrapped individually: an unsupported feature on a
                // given runtime (e.g. Android's SAX implementation) silently falls through
                // rather than preventing parsing.
                runCatching {
                    // Primary defence: reject any DOCTYPE declaration outright.
                    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                }
                // Belt-and-suspenders for runtimes that don't support the above:
                runCatching {
                    setFeature("http://xml.org/sax/features/external-general-entities", false)
                }
                runCatching {
                    setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                }
                runCatching {
                    setFeature(
                        "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                        false,
                    )
                }
            }
        val saxParser = factory.newSAXParser()
        val handler = AtomXmlBaseHandler(feedUrl)
        saxParser.parse(body.inputStream(), handler)
        AtomXmlBases(feedBase = handler.feedBase, entries = handler.entries)
    } catch (_: Exception) {
        AtomXmlBases(feedBase = feedUrl, entries = emptyList())
    }

private class AtomXmlBaseHandler(
    private val feedUrl: URL,
) : DefaultHandler() {
    /**
     * Return an empty document for any external entity request, so that even on SAX
     * implementations that do not support the disallow-doctype-decl feature nothing
     * external is fetched.
     */
    override fun resolveEntity(
        publicId: String?,
        systemId: String?,
    ): InputSource = InputSource(StringReader(""))

    /**
     * Stack of effective base URIs mirroring the element nesting depth.
     * Initialised with [feedUrl] as the document-level base.
     */
    private val baseStack: ArrayDeque<URL> = ArrayDeque<URL>().also { it.addLast(feedUrl) }

    var feedBase: URL = feedUrl
    val entries: MutableList<AtomEntryInfo> = mutableListOf()

    /** True while the parser is inside an <entry> element. */
    private var inEntry = false

    /** Effective xml:base for the <entry> currently being parsed. */
    private var currentEntryBase: URL = feedUrl

    /**
     * The resolved alternate-link href encountered inside the current entry.
     * Set to the first qualifying <link> element found.
     */
    private var currentEntryAlternateHref: String? = null

    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes,
    ) {
        val parentBase = baseStack.last()
        val xmlBaseValue =
            attributes.getValue(XML_NAMESPACE, "base")
                ?: attributes.getValue("xml:base")
        val elementBase =
            if (xmlBaseValue != null) {
                resolveXmlBase(xmlBaseValue, parentBase)
            } else {
                parentBase
            }
        baseStack.addLast(elementBase)

        when (localName) {
            "feed" -> feedBase = elementBase
            "entry" -> {
                inEntry = true
                currentEntryBase = elementBase
                currentEntryAlternateHref = null
            }
            "link" -> {
                if (inEntry && currentEntryAlternateHref == null) {
                    val rel = attributes.getValue("rel") ?: ATOM_LINK_REL_ALTERNATE
                    if (rel == ATOM_LINK_REL_ALTERNATE) {
                        val href = attributes.getValue("href")
                        if (href != null) {
                            // Resolve href against this element's effective base, which already
                            // incorporates any xml:base declared on the <link> element itself.
                            currentEntryAlternateHref =
                                try {
                                    URL(elementBase, href).toString()
                                } catch (_: Exception) {
                                    href
                                }
                        }
                    }
                }
            }
        }
    }

    override fun endElement(
        uri: String,
        localName: String,
        qName: String,
    ) {
        baseStack.removeLastOrNull()
        if (localName == "entry") {
            entries.add(AtomEntryInfo(base = currentEntryBase, resolvedAlternateHref = currentEntryAlternateHref))
            inEntry = false
            currentEntryBase = feedBase
            currentEntryAlternateHref = null
        }
    }

    private fun resolveXmlBase(
        xmlBase: String,
        parentBase: URL,
    ): URL =
        try {
            URL(parentBase, xmlBase)
        } catch (_: Exception) {
            parentBase
        }
}
