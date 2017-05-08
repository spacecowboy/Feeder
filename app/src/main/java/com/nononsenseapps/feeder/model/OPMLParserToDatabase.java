package com.nononsenseapps.feeder.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nononsenseapps.feeder.db.FeedSQL;

public interface OPMLParserToDatabase {
    @Nullable
    FeedSQL getFeed(@NonNull final String url);
    void saveFeed(@NonNull final FeedSQL feed);
}
