package com.nononsenseapps.feeder.util;

import android.support.annotation.Nullable;

public class LoaderResult<A> {
    private final A result;
    private final String message;

    public LoaderResult(A result, String msg) {
        this.result = result;
        this.message = msg;
    }

    // If null, then failMessage is not null
    @Nullable public A result() {
        return result;
    }
    // if null, then result is not null
    @Nullable public String failMessage() {
        return message;
    }
}
