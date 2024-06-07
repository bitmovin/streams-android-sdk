package com.bitmovin.streams.config

/**
 * Configuration for the fullscreen behavior of the player.

 * @property immersive Whether the player should be in immersive mode. Recommended to be false if the EdgeToEdge is disabled on the Activity (may break on some very specific devices).
 * @property autoOrientation Whether the player should automatically change orientation.
 * @property minAspectRatioForLandScapeForce The minimum aspect ratio for forcing landscape orientation. Does not apply if autoOrientation is false.
 * @property maxAspectRatioForPortraitForce The maximum aspect ratio for forcing portrait orientation. Does not apply if autoOrientation is false.
 * @property screenDefaultOrientation The default orientation of the screen. Will be forced after fullscreen escape if non-null. If null, the screen orientation will automatically reset to the state after exiting fullscreen. Should correspond to one of the ActivityInfo.SCREEN_ORIENTATION_* constants.
 * @constructor Creates a new instance of FullscreenConfig.
 */
data class FullscreenConfig(
    val immersive: Boolean = true,
    val autoOrientation: Boolean = true,
    val minAspectRatioForLandScapeForce: Float = 1.2f,
    val maxAspectRatioForPortraitForce: Float = 0.8f,
    val screenDefaultOrientation: Int? = null
)