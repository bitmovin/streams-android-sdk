package com.bitmovin.streams.config

/**
 * Enum class for the different errors that can occur during the stream setup.
 *
 * @see StreamListener
 */
public enum class StreamError(private var message: String) {
    NO_INTERNET("No internet connection"),
    UNAUTHORIZED("Unauthorized access to stream. This stream may require a token."),
    FORBIDDEN_ACCESS("Forbidden access to stream. The token may be invalid or expired."),
    STREAM_NOT_FOUND("Stream not found. Please check that the streamId is correct."),
    INTERNAL_SERVER_ERROR("Internal server error. Please try again later."),
    SERVICE_UNAVAILABLE("Service unavailable. Please try again later."),
    SOURCE_ERROR("Error while loading the source."),
    UNKNOWN_FETCHING_ERROR("An unknown error occurred during FETCHING."),
    UNKNOWN_ERROR("An unknown error occurred."),
    ;

    @Override
    override fun toString(): String {
        return message
    }

    internal companion object {
        fun fromHttpCode(httpCode: Int): StreamError {
            return when (httpCode) {
                0 -> NO_INTERNET
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN_ACCESS
                404 -> STREAM_NOT_FOUND
                500 -> INTERNAL_SERVER_ERROR
                503 -> SERVICE_UNAVAILABLE
                else -> UNKNOWN_FETCHING_ERROR.apply { message = "Error $httpCode : $message" }
            }
        }
    }
}