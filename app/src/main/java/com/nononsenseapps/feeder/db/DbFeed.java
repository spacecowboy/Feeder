package com.nononsenseapps.feeder.db;

// Temporary for tests
public interface DbFeed {
    long getId();
    int getNotify();
    String getTag();
    String getTitle();
    String getCustomTitle();
    int getUnreadCount();
    String getUrl();
}
