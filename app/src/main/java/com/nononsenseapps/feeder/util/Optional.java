package com.nononsenseapps.feeder.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Optional<T> {
    public static <T> Optional<T> of(@NonNull T t) {
        return new Optional<>(t);
    }

    public static <T> Optional<T> ofNullable(@Nullable T t) {
        return new Optional<>(t);
    }

    public static <T> Optional<T> empty() {
        return new Optional<>(null);
    }

    private final T item;

    private Optional(@Nullable T item) {
        this.item = item;
    }

    public boolean isPresent() {
        return item != null;
    }

    @NonNull
    public T get() {
        if (item != null) {
            return item;
        }

        throw new NullPointerException("Tried to get optional value which wasn't present");
    }

    public void ifPresent(Consumer<T> consumer) {
        if (item != null) {
            consumer.accept(item);
        }
    }
}
