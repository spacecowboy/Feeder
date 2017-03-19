package com.nononsenseapps.feeder.util;

import android.support.annotation.NonNull;

public interface Consumer<T> {
    void accept(@NonNull T t);
}
