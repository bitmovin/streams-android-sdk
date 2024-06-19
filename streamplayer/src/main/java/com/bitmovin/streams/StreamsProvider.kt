package com.bitmovin.streams

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.bitmovin.streams.pipmode.PiPChangesObserver
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

internal class StreamsProvider : ContentProvider() {

    val pipChangesObserver = PiPChangesObserver()
    companion object {
        private val instance = StreamsProvider()
        private lateinit var applicationContext: Context
        lateinit var okHttpClient: OkHttpClient


        fun init(application: Context) {
            applicationContext = application
            okHttpClient = OkHttpClient.Builder()
                .cache(
                    Cache(
                        directory = File(applicationContext.cacheDir, "http_cache_streams"),
                        maxSize = 1L * 1024L * 1024L // 1 MiB (we don't expect to cache much data here)
                    )
                ).build()
        }

        fun getInstance(): StreamsProvider {
            return instance
        }
        val appContext: Context
            get() = try {
                applicationContext
            } catch (e: UninitializedPropertyAccessException) {
                throw IllegalStateException("StreamsProvider not initialized. THAT SHOULD NOT HAPPEN. However, you can initialize it manually by calling StreamsProvider.init(<ApplicationContext>) in your Application class.")
            }

    }

    private val streams = mutableMapOf<String, Stream>()

    fun getStream(psid: String): Stream {
        return streams.getOrPut(psid) {
            Stream(psid)
        }
    }

    fun removeStream(psid: String) {
        streams.remove(psid)
    }

    /**
     * Called when the application starts (due to the ContentProvider being registered in the manifest)
     */
    override fun onCreate(): Boolean {
        init(context!!.applicationContext)
        Log.i(Tag.STREAM, "Streams Pool initialized successfully")
        return false
    }


    // IGNORE THE FOLLOWING METHODS, NO NEED TO IMPLEMENT THEM
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
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}
