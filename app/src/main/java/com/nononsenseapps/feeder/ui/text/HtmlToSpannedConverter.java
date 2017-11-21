/*
 * Copyright (c) 2014 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.ui.text;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.nononsenseapps.feeder.R;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

import static com.nononsenseapps.feeder.util.LinkUtilsKt.relativeLinkIntoAbsolute;

/**
 * Convert an HTML document into a spannable string.
 */
public class HtmlToSpannedConverter implements ContentHandler {

    protected static final float[] HEADER_SIZES =
            {1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,};
    private final Context mContext;
    protected int mAccentColor;
    protected int mQuoteGapWidth;
    protected int mQuoteStripeWidth;
    protected int ignoreCount = 0;

    protected String mSource;
    protected String mSiteUrl;
    protected XMLReader mReader;
    protected SpannableStringBuilder mSpannableStringBuilder;

    public HtmlToSpannedConverter(String source, String siteUrl, Parser parser, Context context) {
        mContext = context;
        mSource = source;
        mSiteUrl = siteUrl;
        mSpannableStringBuilder = new SpannableStringBuilder();
        mReader = parser;
        mAccentColor = context.getResources().getColor(R.color.accent);
        mQuoteGapWidth = Math.round(context.getResources().getDimension(R.dimen.reader_quote_gap_width));
        mQuoteStripeWidth = Math.round(context.getResources().getDimension(R.dimen.reader_quote_stripe_width));
    }

    public Spanned convert() {

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

        // Fix flags and range for paragraph-type markup.
        Object[] obj = mSpannableStringBuilder
                .getSpans(0, mSpannableStringBuilder.length(),
                        ParagraphStyle.class);
        for (Object anObj : obj) {
            int start = mSpannableStringBuilder.getSpanStart(anObj);
            int end = mSpannableStringBuilder.getSpanEnd(anObj);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (mSpannableStringBuilder.charAt(end - 1) == '\n' &&
                        mSpannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                mSpannableStringBuilder.removeSpan(anObj);
            }
//            else {
//                mSpannableStringBuilder
//                        .setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH);
//            }
        }

        return mSpannableStringBuilder;
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
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        handleStartTag(localName, attributes);
    }

    protected void handleStartTag(String tag, Attributes attributes) {
        //noinspection StatementWithEmptyBody
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emit the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("em")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("cite")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("i")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("big")) {
            start(mSpannableStringBuilder, new Big());
        } else if (tag.equalsIgnoreCase("small")) {
            start(mSpannableStringBuilder, new Small());
        } else if (tag.equalsIgnoreCase("font")) {
            startFont(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(mSpannableStringBuilder);
            start(mSpannableStringBuilder, new Blockquote());
        } else if (tag.equalsIgnoreCase("tt")) {
            start(mSpannableStringBuilder, new Monospace());
        } else if (tag.equalsIgnoreCase("a")) {
            startA(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(mSpannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("sup")) {
            start(mSpannableStringBuilder, new Super());
        } else if (tag.equalsIgnoreCase("sub")) {
            start(mSpannableStringBuilder, new Sub());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(mSpannableStringBuilder);
            start(mSpannableStringBuilder, new Header(tag.charAt(1) - '1'));
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("ul")) {
            startUl(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("ol")) {
            startOl(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("li")) {
            startLi(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("pre")) {
            startPre(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("code")) {
            startCode(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("iframe")) {
            startIframe(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase( "tr" )) {
            startEndTableRow(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase( "table" )) {
            startEndTable(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("style")) {
            ignoreCount++;
        } else {
            startUnknownTag(tag, mSpannableStringBuilder, attributes);
        }
    }

    protected void handleP(SpannableStringBuilder text) {
        ensureDoubleNewline(text);
    }

    protected void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    protected void startFont(SpannableStringBuilder text,
                             Attributes attributes) {
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        int len = text.length();
        text.setSpan(new Font(color, face), len, len, Spannable.SPAN_MARK_MARK);
    }

    protected void startA(SpannableStringBuilder text,
                          Attributes attributes) {
        String href = attributes.getValue("", "href");

        int len = text.length();
        text.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
    }

    protected void startImg(SpannableStringBuilder text,
                            Attributes attributes) {
        // Override me
        String src = attributes.getValue("", "src");
        Drawable d = mContext.getResources().getDrawable(R.drawable.unknown_image);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());


        int len = text.length();
        text.append("\uFFFC");

        String imgLink = relativeLinkIntoAbsolute(mSiteUrl, src);

        text.setSpan(new ImageSpan(d, imgLink), len, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Add a line break
        text.append("\n");
    }

    protected void startUl(final SpannableStringBuilder text,
                           final Attributes attributes) {
        // Start lists with linebreak
        int len = text.length();
        if (len < 1 || text.charAt(len - 1) != '\n') {
            text.append("\n");
        }

        // Remember list type
        start(text, new Listing(false));
    }

    protected void startOl(final SpannableStringBuilder text,
                           final Attributes attributes) {
        // Start lists with linebreak
        int len = text.length();
        if (len < 1 || text.charAt(len - 1) != '\n') {
            text.append("\n");
        }

        // Remember list type
        start(text, new Listing(true));
    }

    protected void startLi(final SpannableStringBuilder text,
                           final Attributes attributes) {
        // Get type of list
        Listing list = (Listing) getLast(text, Listing.class);

        if (list.mOrdered) {
            // Numbered
            // Add number in bold
            start(text, new Bold());
            text.append("" + list.mNumber++).append(". ");
            end(text, Bold.class, new StyleSpan(Typeface.BOLD));
            // Then do a leading margin
            start(text, new CountBullet());
        } else {
            // Bullet
            start(text, new Bullet());
        }
    }

    protected void startPre(final SpannableStringBuilder text,
                            final Attributes attributes) {
        ensureDoubleNewline( text );
        start(text, new Pre());
    }

    private static void ensureDoubleNewline( SpannableStringBuilder text )
    {
        int len = text.length();
        // Make sure it has spaces before and after
        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) != '\n') {
                text.append("\n");
            }
        } else if (len != 0) {
            text.append("\n\n");
        }
    }

    protected void startCode(final SpannableStringBuilder text,
                             final Attributes attributes) {
        start(text, new Code());
    }

    protected void startIframe(final SpannableStringBuilder text,
                               final Attributes attributes) {
        // Override me
    }

    protected void startUnknownTag(String tag, SpannableStringBuilder text,
                                   Attributes attr) {
        // Override me
    }

    protected Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    protected void end(SpannableStringBuilder text, Class kind,
                       Object repl) {
        int len = text.length();

        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    protected void endQuote(SpannableStringBuilder text) {
        // Don't want end newlines inside block
        removeLastNewlines(text);

        int len = text.length();
        Object obj = getLast(text, Blockquote.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            // Set quote span
            text.setSpan(new MyQuoteSpan(mAccentColor, mQuoteGapWidth, mQuoteStripeWidth), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Be slightly smaller
            text.setSpan(new RelativeSizeSpan(0.8f), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // And have background color
//            text.setSpan(new BackgroundColorSpan(Color.DKGRAY), where, len,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        handleEndTag(localName);
    }

    protected void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(mSpannableStringBuilder, Bold.class,
                    new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("b")) {
            end(mSpannableStringBuilder, Bold.class,
                    new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("em")) {
            end(mSpannableStringBuilder, Italic.class,
                    new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("cite")) {
            end(mSpannableStringBuilder, Italic.class,
                    new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(mSpannableStringBuilder, Italic.class,
                    new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("i")) {
            end(mSpannableStringBuilder, Italic.class,
                    new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("big")) {
            end(mSpannableStringBuilder, Big.class,
                    new RelativeSizeSpan(1.25f));
        } else if (tag.equalsIgnoreCase("small")) {
            end(mSpannableStringBuilder, Small.class,
                    new RelativeSizeSpan(0.8f));
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            endQuote(mSpannableStringBuilder);
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("tt")) {
            end(mSpannableStringBuilder, Monospace.class,
                    new TypefaceSpan("monospace"));
        } else if (tag.equalsIgnoreCase("a")) {
            endA(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("u")) {
            end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            end(mSpannableStringBuilder, Super.class, new SuperscriptSpan());
        } else if (tag.equalsIgnoreCase("sub")) {
            end(mSpannableStringBuilder, Sub.class, new SubscriptSpan());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(mSpannableStringBuilder);
            endHeader(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("img")) {
            endImg(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("ul")) {
            endUl(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("ol")) {
            endOl(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("li")) {
            endLi(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("pre")) {
            endPre(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("code")) {
            endCode(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("iframe")) {
            endIframe(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase( "tr" )) {
            startEndTableRow(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase( "table" )) {
            startEndTable(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("style")) {
            ignoreCount--;
        } else {
            endUnknownTag(tag, mSpannableStringBuilder);
        }
    }

    /**
     * Remove the last newlines from the string, don't want them inside this span
     *
     * @param text spannablestringbuilder
     */
    private void removeLastNewlines(SpannableStringBuilder text) {
        int len = text.length();
        while (len >= 1 && text.charAt(len - 1) == '\n') {
            text.delete(len - 1, len);
            len = text.length();
        }
    }

    protected void handleBr(SpannableStringBuilder text) {
        ensureSingleNewline(text);
    }

    private static void ensureSingleNewline(SpannableStringBuilder text) {
        int len = text.length();
        if (len >= 1 && text.charAt(len - 1) == '\n') {
            return;
        }
        if (len != 0) {
            text.append("\n");
        }
    }

    protected void endFont(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Font.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Font f = (Font) obj;

            if (!TextUtils.isEmpty(f.mColor)) {
                if (f.mColor.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = f.mColor.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        ColorStateList colors = res.getColorStateList(colorRes);
                        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors,
                                        null), where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            if (f.mFace != null) {
                text.setSpan(new TypefaceSpan(f.mFace), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    protected void endA(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Href.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Href h = (Href) obj;

            if (h.mHref != null) {
                text.setSpan(new URLSpan(h.mHref), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    protected void endHeader(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Header.class);

        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        // Back off not to change only the text, not the blank line.
        while (len > where && text.charAt(len - 1) == '\n') {
            len--;
        }

        if (where != len) {
            Header h = (Header) obj;

            text.setSpan(new RelativeSizeSpan(HEADER_SIZES[h.mLevel]), where,
                    len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    protected void startEndTable(SpannableStringBuilder text) {
        ensureDoubleNewline( text );
    }

    protected void startEndTableRow(SpannableStringBuilder text) {
        ensureSingleNewline( text );
    }

    protected void endImg(SpannableStringBuilder text) {
        ensureDoubleNewline( text );
    }

    protected void endUl(final SpannableStringBuilder text) {
        Object obj = getLast(text, Listing.class);
        text.removeSpan(obj);
    }

    protected void endOl(final SpannableStringBuilder text) {
        Object obj = getLast(text, Listing.class);
        text.removeSpan(obj);
    }

    protected void endLi(final SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Bullet.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {

            Object span;

            final int offset = 60;
            if (obj instanceof CountBullet) {
                // Numbered
                span = new LeadingMarginSpan.Standard(offset, offset);
            } else {
                // Bullet points
                span = new BulletSpan(offset, Color.GRAY);
            }

            text.setSpan(span, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        // Add newline
        text.append("\n");
    }

    protected void endPre(final SpannableStringBuilder text) {
        // yes, take len before appending
        int len = text.length();
        ensureDoubleNewline(text);

        Object obj = getLast(text, Pre.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            // TODO
            // Make sure text does not wrap.
            // No easy solution exists for this
            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment
                            .ALIGN_NORMAL), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    protected void endCode(final SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Code.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            // Want it to be monospace
            text.setSpan(new TypefaceSpan("monospace"), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Be slightly smaller
            text.setSpan(new RelativeSizeSpan(0.8f), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // And have background color
            text.setSpan(new BackgroundColorSpan(Color.DKGRAY), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    protected void endIframe(final SpannableStringBuilder text) {

    }

    protected void endUnknownTag(String tag, SpannableStringBuilder text) {
        // Override me
    }

    @Override
    public void characters(char ch[], int start, int length)
            throws SAXException {
        if (ignoreCount > 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = mSpannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (pred != ' ' && pred != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        mSpannableStringBuilder.append(sb);
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    protected static class Bold {
    }

    protected static class Italic {
    }

    protected static class Underline {
    }

    protected static class Big {
    }

    protected static class Small {
    }

    protected static class Monospace {
    }

    protected static class Blockquote {
    }

    protected static class Super {
    }

    protected static class Sub {
    }

    protected static class Listing {
        public boolean mOrdered;
        public int mNumber;

        public Listing(boolean numbered) {
            mOrdered = numbered;
            mNumber = 1;
        }
    }

    protected static class Bullet {
    }

    protected static class CountBullet extends Bullet {
    }

    protected static class Pre {
    }

    protected static class Code {
    }

    protected static class Font {
        public String mColor;
        public String mFace;

        public Font(String color, String face) {
            mColor = color;
            mFace = face;
        }
    }

    protected static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }

    protected static class Header {
        protected int mLevel;

        public Header(int level) {
            mLevel = level;
        }
    }
}
