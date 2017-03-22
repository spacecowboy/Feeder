package com.nononsenseapps.text;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VideoTagHunter {

    // Example strings
    // www.youtube.com/embed/cjxnVO9RpaQ
    // www.youtube.com/embed/cjxnVO9RpaQ?feature=oembed
    // www.youtube.com/embed/cjxnVO9RpaQ/theoretical_crap
    // www.youtube.com/embed/cjxnVO9RpaQ/crap?feature=oembed
    static final Pattern YoutubeIdPattern = Pattern.compile("youtube" +
                                                            ".com/embed/" +
                                                            "([^?/]*)");

    public static Video getVideo(final String src, final String width,
            final String height) {
        Video video = new Video();
        video.src = src;
        video.width = width;
        video.height = height;

        Matcher m = YoutubeIdPattern.matcher(video.src);
        if (m.find()) {
            video.imageurl = "http://img.youtube.com/vi/" + m.group(1) + "/hqdefault.jpg";
            video.link = "https://www.youtube.com/watch?v=" + m.group(1);
        }
        Log.d("JONASYOUTUBE", "image: " + video.imageurl);

        return video;
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
