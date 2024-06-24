package com.bitmovin.streams.config

/**
 * Configuration for the fullscreen behavior of the player.
 *
 * @property enable Whether the fullscreen behavior should be enabled.
 *  When disabled, the player will also not be able to enter picture-in-picture mode.
 * @property immersive Whether the player should be in immersive mode. Recommended to be false if the EdgeToEdge is disabled on the Activity (may break on some very specific devices).
 * @property autoOrientation Whether the player should automatically change orientation.
 * @property minAspectRatioForLandScapeForce The minimum aspect ratio for forcing landscape orientation. Does not apply if autoOrientation is false.
 * @property maxAspectRatioForPortraitForce The maximum aspect ratio for forcing portrait orientation. Does not apply if autoOrientation is false.
 * @property screenDefaultOrientation The default orientation of the screen. Will be forced after fullscreen escape if non-null. If null, the screen orientation will automatically reset to the state after exiting fullscreen. Should correspond to one of the ActivityInfo.SCREEN_ORIENTATION_* constants.
 * @property autoPiPOnBackground Whether the player should automatically enter picture-in-picture mode when the app goes to the background while a stream being in fullscreen.
 */
data class FullscreenConfig(
    val enable: Boolean = true,
    val immersive: Boolean = true,
    val autoOrientation: Boolean = true,
    val minAspectRatioForLandScapeForce: Float = 1.2f,
    val maxAspectRatioForPortraitForce: Float = 0.8f,
    val screenDefaultOrientation: Int? = null,
    val autoPiPOnBackground: Boolean = true
)