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

package com.nononsenseapps.feeder.model;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.shirwa.simplistic_rss.RssFeed;
import com.shirwa.simplistic_rss.RssItem;
import com.shirwa.simplistic_rss.RssReader;

import java.util.ArrayList;


public class LocalRssSyncHelper {


    public static void syncFeedBatch(Context context, FeedSQL feedaf,
                                     final ArrayList<ContentProviderOperation> operations)
    {
        // This is the index of the feed, if needed for back-references
        final int feedIndex = operations.size();
        final long feedId = feedaf.id;
        // Create the insert/update feed operation first
        final ContentProviderOperation.Builder feedOp = ContentProviderOperation.newUpdate(
                Uri.withAppendedPath(FeedSQL.URI_FEEDS,
                        Long.toString(feedId)));


        // Download feed and parse it
        RssReader r = new RssReader(feedaf.url);
        RssFeed f = null;
        try {
            f = r.getFeed();
        } catch (Exception e) {
            Log.e("LOCAL", e.toString());
            return;
        }

        // Populate with values
        assert f != null;
        feedOp.withValue(FeedSQL.COL_TIMESTAMP, FeedItemSQL.getPubDateFromString(f.getPubDate() != null ? f.getPubDate().toString() : null));

        // Add to list of operations
        operations.add(feedOp.build());

        // Now the feeds
        for (RssItem item: f.getRssItems()) {
            // Always insert, have on conflict replace
            ContentProviderOperation.Builder itemOp = ContentProviderOperation
                    .newInsert(FeedItemSQL.URI_FEED_ITEMS);

            // First, reference feed's id with back ref if insert
//            if (feedId < 1) {
//                itemOp.withValueBackReference(FeedItemSQL.COL_FEED, feedIndex);
//            } else {
            // Use the actual id, because update operation will not return id
            itemOp.withValue(FeedItemSQL.COL_FEED, feedId);
//            }
            // Next all the other values. Make sure non null
            itemOp.withValue(FeedItemSQL.COL_LINK, item.getLink())
                    .withValue(FeedItemSQL.COL_FEEDTITLE, f.getTitle())
                    .withValue(FeedItemSQL.COL_TAG,
                            feedaf.tag == null ? "" : feedaf.tag)
                    .withValue(FeedItemSQL.COL_IMAGEURL, item.getImageUrl())
                    .withValue(FeedItemSQL.COL_ENCLOSURELINK, item.getEnclosure())
                    .withValue(FeedItemSQL.COL_AUTHOR, item.getAuthor())
                    .withValue(FeedItemSQL.COL_PUBDATE,
                            FeedItemSQL.getPubDateFromString(item.getPubDate() != null ? item.getPubDate().toString() : null))
                            // Make sure these are non-null
                    .withValue(FeedItemSQL.COL_TITLE,
                            item.getTitle() == null ? "" : item.getTitle())
                    .withValue(FeedItemSQL.COL_DESCRIPTION,
                            item.getDescription() == null ? "" : item.getDescription())
                    .withValue(FeedItemSQL.COL_PLAINTITLE,
                            item.getPlainTitle() == null ?
                                    "" :
                                    item.getPlainTitle())
                    .withValue(FeedItemSQL.COL_PLAINSNIPPET,
                            item.getSnippet() == null ? "" : item.getSnippet());

            // Add to list of operations
            operations.add(itemOp.build());
        }
    }
}
