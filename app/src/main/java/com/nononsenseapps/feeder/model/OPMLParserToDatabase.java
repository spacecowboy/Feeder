package com.nononsenseapps.feeder.model;

import com.nononsenseapps.feeder.db.room.Feed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OPMLParserToDatabase {
    @Nullable
    Feed getFeed(@NonNull final String url);

    void saveFeed(@NonNull final Feed feed);
}
