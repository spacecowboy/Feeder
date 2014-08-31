package com.nononsenseapps.feeder.model;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

public class ImageTagHunter {

    public static void getImages(final String text,
            HashMap<String, Image> images) {
        Document doc = Jsoup.parse(text);
        //Get all elements with img tag
        Elements imgtags = doc.getElementsByTag("img");

        for (Element imgtag: imgtags) {
            Image img = getImageAttributes(imgtag);
            images.put(img.src, img);
        }


//        xpp.setInput(new StringReader(text));
//        int eventType = xpp.getEventType();
//        while (eventType != XmlPullParser.END_DOCUMENT) {
//            Log.d("JONAS3", "start");
//            if (eventType == XmlPullParser.START_DOCUMENT) {
//                Log.d("JONAS2", "Start document");
//            } else if (eventType == XmlPullParser.START_TAG) {
//                Log.d("JONAS2", "Start tag " + xpp.getName());
//                if ("img".equalsIgnoreCase(xpp.getName())) {
//                    Image img = getImageAttributes(xpp);
//                    images.put(img.src, img);
//                }
//            } else if (eventType == XmlPullParser.END_TAG) {
//                //Log.d("JONAS2", "End tag " + xpp.getName());
//            } else if (eventType == XmlPullParser.TEXT) {
//                //Log.d("JONAS2", "Text " + xpp.getText());
//            }
//            Log.d("JONAS3", "pre end");
//            try {
//                eventType = xpp.next();
//            } catch (XmlPullParserException e) {
//                Log.e("JONAS3", "" + e.getMessage());
//            } catch (ArrayIndexOutOfBoundsException e) {
//                Log.d("JONAS3", "Forcing end: " + e.getMessage());
//                // End of document
//                eventType = XmlPullParser.END_DOCUMENT;
//            }
//            Log.d("JONAS3", "post end");
//        }
    }
    private static Image getImageAttributes(final Element tag) {
        Image img = new Image();
        img.src = tag.absUrl("src");
        Log.d("JONAS2", "img src " + img.src);
        if (tag.hasAttr("alt")) {
            img.alt = tag.attr("alt");
            Log.d("JONAS2", "img alt " + img.alt);
        }
        if (tag.hasAttr("width")) {
            img.width = tag.attr("width");
            Log.d("JONAS2", "img width " + img.width);
        }
        if (tag.hasAttr("height")) {
            img.height = tag.attr("height");
            Log.d("JONAS2", "img height " + img.height);
        }
        return img;
    }
//    private static Image getImageAttributes(final XmlPullParser xpp) {
//        Image img = new Image();
//        String attrName;
//        int count = 0;
//        for (int i = 0; i < xpp.getAttributeCount() && count < 4; i++) {
//            attrName = xpp.getAttributeName(i);
//            Log.d("JONAS2", attrName + " " + xpp.getAttributeValue(i));
//
//            if ("src".equals(attrName)) {
//                img.src = xpp.getAttributeValue(null, "src");
//                count++;
//            } else if ("width".equals(attrName)) {
//                img.width = xpp.getAttributeValue(null, "width");
//                count++;
//            } else if ("height".equals(attrName)) {
//                img.height = xpp.getAttributeValue(null, "height");
//                count++;
//            } else if ("alt".equals(attrName)) {
//                img.alt = xpp.getAttributeValue(null, "alt");
//                count++;
//            }
//        }
//
//        return img;
//    }

    public static class Image {
        public String src = null;
        public String width = null;
        public String height = null;
        public String alt = null;

        /**
         *
         * @return true if image has any size specified in percent
         */
        public boolean hasPercentSize() {
            boolean result = false;
            if (width != null) {
                result = width.contains("%");
            }
            if (height != null) {
                result |= height.contains("%");
            }

            return result;
        }

        /**
         *
         * @return true if image has width and height specified in pixels
         */
        public boolean hasSize() {
            if (width != null && height != null) {
                try {
                    Integer.parseInt(width);
                    Integer.parseInt(height);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            return false;
        }

        public int getIntWidth() {
            return Integer.parseInt(width);
        }

        public int getIntHeight() {
            return Integer.parseInt(height);
        }
    }
}
