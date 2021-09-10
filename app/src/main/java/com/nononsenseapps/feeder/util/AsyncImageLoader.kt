package com.nononsenseapps.feeder.util

import android.content.Context
import android.util.Log
import androidx.collection.ArrayMap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class AsyncImageLoader(override val di: DI) : DIAware {
    private val coroutineScope: CoroutineScope by instance()
    private val imageLoader: ImageLoader by instance()
    private val context: Context by instance()
    private val cache: ArrayMap<String, Deferred<ImageResult>> = ArrayMap()
    private val cacheMutex = Mutex()

    /**
     * Initiates a download of the image - keeping the result for later
     */
    fun downloadImageInBackground(
        imgLink: String,
        block: ImageRequest.Builder.() -> ImageRequest.Builder = { this }
    ) {
        runBlocking {
            cacheMutex.withLock {
                if (imgLink !in cache) {
                    cache[imgLink] = getImageAsync(imgLink, block)
                }
            }
        }
    }

    /**
     * Initiates an asynchronous download of the image
     */
    private fun getImageAsync(
        imgLink: String,
        block: ImageRequest.Builder.() -> ImageRequest.Builder = { this }
    ): Deferred<ImageResult> = coroutineScope.async {
        Log.d("FeederAsyncImageLoader", "Getting $imgLink")
        // Want to return null on errors - no fallback image
        val imgRequest = ImageRequest.Builder(context)
            .data(imgLink)
            .block()
            .build()

        imageLoader.execute(imgRequest).also {
            Log.d("FeederAsyncImageLoader", "Done with $imgLink")
        }
    }

    /**
     * Return a cache download the image or download it now
     */
    operator fun get(imgLink: String): ImageResult = runBlocking {
        Log.d("FeederAsyncImageLoader", "Removing $imgLink")
        // Some blogs (ahem, Android Developers Blog, ahem) have duplicate images in them.
        // In those cases, we need to read the image in again since it's gone from the cache.
        // However, in that case the image should be in the OkHttp cache so still being fast

        val result =
            cacheMutex.withLock {
                cache.remove(imgLink)
            } ?: getImageAsync(imgLink)

        result.await()
    }
}
