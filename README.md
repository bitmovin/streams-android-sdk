# streams-android-sdk
Bitmovin Streams Android 

## Setup

Here is what should be included in the manifest.

```xml
<manifest
    <.../>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        <.../>
   ∏     <activity
            android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"
            android:resizeableActivity="true"
            android:supportsPictureInPicture="true"
            <.../>
        >
        <.../>
    >
    <.../>
>
```
Put the component your Compose View :
```kotlin
BitmovinStream(
    streamId = "<YOUR-STREAM-ID>"
)
```

Component properties:

| Property  | Default Value | Description                                                                                                                                            |
|-----------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| streamId  | None          | The stream id that you want to play. This is the only required property.                                                                               |
| autoPlay  | False         | If true, the stream will start playing as soon as the component is loaded. Default is true.                                                            |
| muted     | False         | If true, the stream will be muted. Default is false.                                                                                                   |
| poster    | null          | The URL to a preview image displayed until the video starts. The property have the priority over the stream preview image if it is set.                |
| start     | 0.0f          | A float value specifying an offset for the playback starting position.                                                                                 |
| subtitles | Empty List    | Specifies an list of external subtitles to be used in the player. The values have to be of the type SubtitleTrack |
| jwToken   | null          | The auth. token to be used for the stream.                                                                                                                   |
| modifier  | Modifier      | The modifier to be applied to the player.                                                                                                                |
| immersiveFullScreen | True | If true, the player will be in immersive full screen mode. Should only be true if edgeToEdge is enabled. Default is false.                           |
| bitmovinStreamEventListener | null | The listener for the stream player events. Let the user obtain the player and the playerView to init some advanced feature.                                                                                                                     |
| screenOrientationOnFullscreenEscape | null | The screen orientation to be set when the player exits full screen. Default it will use the screenOrientation that preceeded the fullscreen switch. |
| enableAds | True | If true, ads defined of the Stream dashboard will be enabled.