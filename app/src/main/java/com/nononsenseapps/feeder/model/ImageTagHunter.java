package com.nononsenseapps.feeder.model;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

public class ImageTagHunter {

    public static void getImages(final Document doc,
            HashMap<String, Image> images) {
        //Get all elements with img tag
        Elements imgtags = doc.getElementsByTag("img");

        for (Element imgtag : imgtags) {
            Image img = getImageAttributes(imgtag);
            images.put(img.src, img);
        }
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

    public static class Image {
        public String src = null;
        public String width = null;
        public String height = null;
        public String alt = null;

        /**
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
