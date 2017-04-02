package com.nononsenseapps.feeder.util;

import android.content.Context;
import android.graphics.Bitmap;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Makes sure proper caching is enabled for all image requests and that the cache can be used in offline scenarios also.
 */
public class GlideUtils {
    public static DrawableRequestBuilder<String> glide(Context context, String imgUrl, boolean allowDownload) {
        if (allowDownload) {
            return Glide.with(context)
                    .load(imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        } else {
            return Glide.with(context)
                    .using(new StreamModelLoader<String>() {
                        @Override
                        public DataFetcher<InputStream> getResourceFetcher(final String s, int i, int i1) {
                            return new DataFetcher<InputStream>() {
                                @Override
                                public InputStream loadData(Priority priority) throws Exception {
                                    throw new IOException("Download not allowed");
                                }

                                @Override
                                public void cleanup() {

                                }

                                @Override
                                public String getId() {
                                    return s;
                                }

                                @Override
                                public void cancel() {

                                }
                            };
                        }
                    })
                    .load(imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        }
    }

    public static BitmapRequestBuilder<String, Bitmap> glideAsBitmap(Context context, String imgUrl, boolean allowDownload) {
        if (allowDownload) {
            return Glide.with(context)
                    .load(imgUrl)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        } else {
            return Glide.with(context)
                    .using(new StreamModelLoader<String>() {
                        @Override
                        public DataFetcher<InputStream> getResourceFetcher(final String s, int i, int i1) {
                            return new DataFetcher<InputStream>() {
                                @Override
                                public InputStream loadData(Priority priority) throws Exception {
                                    throw new IOException("Download not allowed");
                                }

                                @Override
                                public void cleanup() {

                                }

                                @Override
                                public String getId() {
                                    return s;
                                }

                                @Override
                                public void cancel() {

                                }
                            };
                        }
                    })
                    .load(imgUrl)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        }
    }
}
