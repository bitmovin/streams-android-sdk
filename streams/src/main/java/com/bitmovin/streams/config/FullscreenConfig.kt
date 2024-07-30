package com.bitmovin.streams.config

/**
 * Configuration for the fullscreen behavior of the player.
 */
public class FullscreenConfig(
    /**
     * Whether the fullscreen behavior should be enabled.
     */
    public var enable: Boolean = true,
    /**
     * Whether the player should be in immersive mode. Recommended to be false if the _EdgeToEdge_ is disabled on the Activity (may break on some very specific devices).
     */
    public var immersive: Boolean = true,
    /**
     * Whether the player should automatically change orientation.
     */
    public var autoRotate: Boolean = true,
    /**
     * The minimum aspect ratio for forcing landscape orientation. Does not apply if autoRotate is false.
     */
    public var minAspectRatioForLandscapeForce: Float = 1.2f,
    /**
     * The maximum aspect ratio for forcing portrait orientation. Does not apply if autoRotate is false.
     */
    public var maxAspectRatioForPortraitForce: Float = 0.8f,
    /**
     * The default orientation of the screen.Will be forced after fullscreen escape if non-null. If null, the screen orientation will automatically reset to the state after exiting fullscreen. Should correspond to one of the ActivityInfo.SCREEN_ORIENTATION_* constants.
     */
    public var screenDefaultOrientation: Int? = null,
    /**
     * Whether the player should automatically enter picture-in-picture mode when the app goes to the background while a stream being in fullscreen.
     */
    public var autoPiPOnBackground: Boolean = true,
) {
    public companion object {
        public val DEFAULT: FullscreenConfig = FullscreenConfig()
        public val DISABLED: FullscreenConfig = FullscreenConfig(enable = false)
    }
}
