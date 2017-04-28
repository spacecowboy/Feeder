package com.nononsenseapps.feeder.ui.text;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

/**
 * Intended primarily to convert HTML into plaintext snippets, useful for previewing content in list.
 */
public class HtmlToPlainTextConverter implements ContentHandler {

    private final String mSource;
    private final Parser mReader;
    private StringBuilder builder;
    private Stack<HtmlToSpannedConverter.Listing> listings = new Stack<>();

    public static String HtmlToPlainText(String html) {
        return new HtmlToPlainTextConverter(html).convert();
    }

    public HtmlToPlainTextConverter(String source) {
        mSource = source;
        mReader = getParser();
    }

    public String convert() {
        this.builder = new StringBuilder();

        mReader.setContentHandler(this);
        try {
            mReader.parse(new InputSource(new StringReader(mSource)));
        } catch (IOException e) {
            // We are reading from a string. There should not be IO problems.
            throw new RuntimeException(e);
        } catch (SAXException e) {
            // TagSoup doesn't throw parse exceptions.
            throw new RuntimeException(e);
        }

        // Replace non-breaking space (160) with normal space
        return builder.toString().replace((char) 160, ' ').trim();
    }

    @Override
    public void setDocumentLocator(Locator locator) {

    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        handleStartTag(localName, attributes);
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emit the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("p")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("div")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("strong")) {
            strong(builder);
        } else if (tag.equalsIgnoreCase("b")) {
            strong(builder);
        } else if (tag.equalsIgnoreCase("em")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("cite")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("dfn")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("i")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("a")) {
            startA(builder, attributes);
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("ul")) {
            startUl(builder);
        } else if (tag.equalsIgnoreCase("ol")) {
            startOl(builder);
        } else if (tag.equalsIgnoreCase("li")) {
            startLi(builder);
        }
    }

    private void startOl(StringBuilder text) {
        // Start lists with linebreak
        int len = text.length();
        if (len > 0 && text.charAt(len - 1) != '\n') {
            text.append("\n");
        }

        // Remember list type
        listings.push(new HtmlToSpannedConverter.Listing(true));
    }

    private void startLi(StringBuilder builder) {
        builder.append(repeated("  ", listings.size() - 1));
        if (isOrderedList()) {
            HtmlToSpannedConverter.Listing listing = listings.peek();
            builder.append("" + listing.mNumber++).append(". ");
        } else {
            builder.append("* ");
        }
    }

    public static String repeated(String string, int count) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            sb.append(string);
        }

        return sb.toString();
    }

    private void endLi(StringBuilder text) {
        // Add newline
        int len = text.length();
        if (len > 0 && text.charAt(len - 1) != '\n') {
            text.append("\n");
        }
    }

    private void startUl(StringBuilder text) {
        // Start lists with linebreak
        int len = text.length();
        if (len > 0 && text.charAt(len - 1) != '\n') {
            text.append("\n");
        }

        // Remember list type
        listings.push(new HtmlToSpannedConverter.Listing(false));
    }

    private void endOl(StringBuilder builder) {
        listings.pop();
    }

    private void endUl(StringBuilder builder) {
        listings.pop();
    }

    private boolean isOrderedList() {
        return !listings.isEmpty() && listings.peek().mOrdered;
    }

    private void startA(StringBuilder builder, Attributes attributes) {
    }

    private void endA(StringBuilder builder) {
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        handleEndTag(localName);
    }

    protected void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("p")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("div")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("strong")) {
            strong(builder);
        } else if (tag.equalsIgnoreCase("b")) {
            strong(builder);
        } else if (tag.equalsIgnoreCase("em")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("cite")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("dfn")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("i")) {
            emphasize(builder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("a")) {
            endA(builder);
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            ensureSpace(builder);
        } else if (tag.equalsIgnoreCase("ul")) {
            endUl(builder);
        } else if (tag.equalsIgnoreCase("ol")) {
            endOl(builder);
        } else if (tag.equalsIgnoreCase("li")) {
            endLi(builder);
        }
    }

    private void emphasize(StringBuilder builder) {
        builder.append("*");
    }

    private void strong(StringBuilder builder) {
        builder.append("**");
    }

    protected void ensureSpace(StringBuilder text) {
        int len = text.length();
        if (len != 0) {
            char c = text.charAt(len - 1);
            // Non-breaking space (160) is not caught by trim or whitespace identification
            if (Character.isWhitespace(c) || c == 160) {
                return;
            }
            text.append(" ");
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char prev;
                int len = sb.length();

                if (len == 0) {
                    len = builder.length();

                    if (len == 0) {
                        prev = '\n';
                    } else {
                        prev = builder.charAt(len - 1);
                    }
                } else {
                    prev = sb.charAt(len - 1);
                }

                if (prev != ' ' && prev != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        builder.append(sb);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }

    public Parser getParser() {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        } catch (org.xml.sax.SAXNotRecognizedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        } catch (org.xml.sax.SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }
        return parser;
    }
}
