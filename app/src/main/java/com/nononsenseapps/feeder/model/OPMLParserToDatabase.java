package com.nononsenseapps.feeder.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.nononsenseapps.feeder.db.FeedSQL;

public interface OPMLParserToDatabase {
    @Nullable
    FeedSQL getFeed(@NonNull final String url);
    void saveFeed(@NonNull final FeedSQL feed);
}
