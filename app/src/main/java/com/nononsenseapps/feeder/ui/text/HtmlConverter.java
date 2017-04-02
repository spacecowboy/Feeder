package com.nononsenseapps.feeder.ui.text;


import android.content.Context;
import android.graphics.Point;
import android.text.Spanned;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;

/**
 * This class processes HTML strings into displayable styled text.
 * Not all HTML tags are supported.
 * <p/>
 * Modified from android.text.Html. It does not extend it because all
 * functionality is placed in static methods, which makes extension impossible.
 * <p/>
 * Adds more extensibility to the parsing.
 */
public class HtmlConverter {

    private HtmlConverter() {}

    public static Spanned toSpannedWithImages(String source, Context context, Point maxSize) {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
        } catch (org.xml.sax.SAXNotRecognizedException | org.xml.sax.SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        GlideConverter converter = new GlideConverter(source, parser, context, maxSize);
        return converter.convert();
    }

    /**
     * Returns displayable styled text from the provided HTML string.
     * Any &lt;img&gt; tags in the HTML will use the specified ImageGetter
     * to request a representation of the image (use null if you don't
     * want this) and the specified TagHandler to handle unknown tags
     * (specify null if you don't want this).
     * <p/>
     * <p>This uses TagSoup to handle real HTML, including all of the brokenness
     * found in the wild.
     */
    public static Spanned toSpannedWithNoImages(String source, Context context) {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
        } catch (org.xml.sax.SAXNotRecognizedException | org.xml.sax.SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        HtmlToSpannedConverter converter = new HtmlToSpannedConverter(source, parser, context);
        return converter.convert();
    }

    /**
     * Returns plain text representation of the provided HTML string.
     */
    public String toPlainText(String source) {
        return getPlainTextConverter(source).convert();
    }

    private HtmlToPlainTextConverter getPlainTextConverter(String source) {
        return new HtmlToPlainTextConverter(source);
    }

    /**
     * Lazy initialization holder for HTML parser. This class will
     * a) be preloaded by the zygote, or b) not loaded until absolutely
     * necessary.
     */
    private static class HtmlParser {
        private static final HTMLSchema schema = new HTMLSchema();
    }
}
