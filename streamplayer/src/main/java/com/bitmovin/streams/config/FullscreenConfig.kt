package com.bitmovin.streams.config

/**
 * Configuration for the fullscreen behavior of the player.

 * @property immersive Whether the player should be in immersive mode.
 * @property autoOrientation Whether the player should automatically change orientation.
 * @property minAspectRatioForLandScapeForce The minimum aspect ratio for forcing landscape orientation.
 * @property maxAspectRatioForPortraitForce The maximum aspect ratio for forcing portrait orientation.
 * @property screenDefaultOrientation The default orientation of the screen. Will be forced after fullscreen escape if non-null. If null, the screen orientation will automatically reset to the state after exiting fullscreen.
 * @constructor Creates a new instance of FullscreenConfig.
 */
data class FullscreenConfig(
    val immersive: Boolean = true,
    val autoOrientation: Boolean = true,
    val minAspectRatioForLandScapeForce: Float = 1.2f,
    val maxAspectRatioForPortraitForce: Float = 0.8f,
    val screenDefaultOrientation: Int? = null
)