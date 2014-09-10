package com.nononsenseapps.feeder.model;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This searches for iFrame tags, and in the future video,
 * embed and objects tags.
 *
 * TODO handle other tags
 */
public class VideoTagHunter {

    static final Pattern YoutubeIdPattern = Pattern.compile("youtube" +
                                                            ".com/embed/(.*)");

    public static void getVideos(final Document doc,
            ArrayList<Video> videos) {

        //Get all elements with img tag
        Elements videotags = doc.getElementsByTag("iframe");

        for (Element tag : videotags) {
            Video video = getAttributes(tag);
            setUrls(video);
            videos.add(video);
        }
    }

    private static Video getAttributes(final Element tag) {
        Video video = new Video();
        video.src = tag.absUrl("src");
        Log.d("JONAS2", "video src " + video.src);
        if (tag.hasAttr("width")) {
            video.width = tag.attr("width");
            Log.d("JONAS2", "img width " + video.width);
        }
        if (tag.hasAttr("height")) {
            video.height = tag.attr("height");
            Log.d("JONAS2", "img height " + video.height);
        }
        return video;
    }

    public static void setUrls(final Video video) {
        Matcher m = YoutubeIdPattern.matcher(video.src);
        if (m.find()) {
            video.imageurl = "http://img.youtube.com/vi/" + m.group(1) + "/hqdefault.jpg";
            video.link = "https://www.youtube.com/watch?v=" + m.group(1);
        }
        Log.d("JONASYOUTUBE", "image: " + video.imageurl);
    }

    public static class Video {
        public String src = null;
        public String width = null;
        public String height = null;
        public String imageurl = null;
        // Youtube needs a different link than embed links
        public String link = null;

        public int getIntWidth() {
            return Integer.parseInt(width);
        }

        public int getIntHeight() {
            return Integer.parseInt(height);
        }
    }
}
