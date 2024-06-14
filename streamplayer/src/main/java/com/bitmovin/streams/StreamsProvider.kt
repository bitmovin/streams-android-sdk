package com.bitmovin.streams

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

internal class StreamsAccessPool {
    companion object {
        private val instance = StreamsAccessPool()
        lateinit var appContext: Context
        lateinit var okHttpClient: OkHttpClient

        fun init(application: Context) {
            Log.e("StreamsAccessPool", "init...!!")
            appContext = application
            okHttpClient = OkHttpClient.Builder()
                .cache(Cache(
                    directory = File(appContext.cacheDir, "http_cache_streams"),
                    maxSize = 1L * 1024L * 1024L // 1 MiB
                )).build()
        }

        fun getInstance(): StreamsAccessPool {
            return instance
        }

    }

    private val streams = mutableMapOf<String, Stream>()

    fun getStream(streamId: String): Stream {
        return streams.getOrPut(streamId) {
            Stream()
        }
    }
}

internal class AppContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        Log.e("DummyProvider","creating...!!")
        //Initialize your library and other components here.
        StreamsAccessPool.init(context!!.applicationContext)
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0;
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0;
    }
}