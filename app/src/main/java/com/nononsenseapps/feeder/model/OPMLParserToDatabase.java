package com.nononsenseapps.feeder.model;

import android.support.annotation.NonNull;
import com.nononsenseapps.feeder.db.FeedSQL;

public interface OPMLParserToDatabase {
    @NonNull
    FeedSQL getFeed(@NonNull final String url);
    void saveFeed(@NonNull final FeedSQL feed);
}
