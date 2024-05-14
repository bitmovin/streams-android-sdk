package com.bitmovin.streams

import android.util.Log
import com.bitmovin.streams.streamsjson.StreamConfigData
import com.bitmovin.streams.streamsjson.StreamConfigDataResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Fetches the stream config data from the Bitmovin API
 * @param streamId the id of the stream
 * @return the stream config data
 * @throws IOException if the request fails
 */
suspend fun getStreamConfigData(streamId: String, jwToken: String?) : StreamConfigDataResponse {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        var url = "https://streams.bitmovin.com/${streamId}/config"
        if (jwToken != null) {
            url = addURLParam(url, "token", jwToken)
        }
        val request: Request = Request.Builder()
            .url(url)
            .build()

        Log.d("Streams", "Request: $request")
        val response = client.newCall(request).execute()
        val code = response.code
        if (!response.isSuccessful) {
            return@withContext StreamConfigDataResponse(null, code)
        }
        val responseBody = response.body?.string()
        val streamConfigData = Gson().fromJson(responseBody, StreamConfigData::class.java)
        return@withContext StreamConfigDataResponse(streamConfigData, code)
    }
}

fun addURLParam(url: String, attribute: String, value: String): String {
    val separator = if (url.contains("?")) "&" else "?"
    return "$url$separator$attribute=$value"
}

