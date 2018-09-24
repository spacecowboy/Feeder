package com.nononsenseapps.feeder.util;

import androidx.annotation.NonNull;

public interface Consumer<T> {
    void accept(@NonNull T t);
}
