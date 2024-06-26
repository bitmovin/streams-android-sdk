# Bitmovin Streams on Android 

This library provides BitmovinStream, which is a Compose component to play [Bitmovin Streams](https://bitmovin.com/streams) natively on Android.

The component is built on top of the [Bitmovin Player Android SDK](https://bitmovin.com/docs/player/android-sdk/).

It's properties are highly customizable and can be used in a variety of ways, from simple video playback to more complex use cases.

__Note:__ If you are planning really advanced/deep use cases, we recommend using the Bitmovin Player Android SDK directly and building your own `StreamPlayer` on top of it to have full control over the player and suite your needs better. This project still can be a reference on how to build for Compose and Streams.  

## Getting Started

TODO WHEN PUBLISHED /!\

Each activity that uses the BitmovinStream component must be declared in the AndroidManifest.xml file with the following configuration:

```xml
<manifest
    <.../>
    <application
        <.../>
         <activity
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

## Properties

It has the following parameters:  

| Parameter        | Default Value              | Description                                                                                                                                      |
|------------------|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| streamId         | None                       | The stream id that you want to play. This is the only required property.                                                                         |
| modifier         | Modifier.aspectRatio(16/9) | The modifier to be applied to the Stream component.                                                                                              |
| jwToken          | null                       | The auth. token to be used for the stream.                                                                                                       |
| autoPlay         | false                      | If true, the stream will start playing as soon as the component is loaded. Default is true.                                                      |
| muted            | false                      | If true, the stream will be muted. Default is false.                                                                                             |
| loop             | false                      | If true, the video will repeat itself when the end is reached. It's not recommended on long format videos. Always false when the stream is Live. |
| poster           | null                       | The URL to a preview image displayed until the video starts. The property have the priority over the stream preview image if it is set.          |
| start            | 0.0                        | The starting playback of the Stream in seconds.                                                                                                  |
| subtitles        | Empty List                 | Specifies an list of external subtitles to be used in the player. The values have to be of the type SubtitleTrack                                |
| streamListener   | null                       | The listener for the player events.                                                                                                              |
| enableAds        | true                       | If true, ads will be enabled.                                                                                                                    |
| fullscreenConfig | true                       | The configuration for the fullscreen mode.                                                                                                       |
| styleConfig      | None                       | The style configuration for the player.                                                                                                          |
| allLogs          | false                      | If true, all logs will be printed.                                                                                                               |

There is an alternative way to setup the component using Configuration object:

| Parameter | Default Value              | Description                                                                                                                  |
|-----------|----------------------------|------------------------------------------------------------------------------------------------------------------------------|
| config    | None                       | The configuration object you want to pass. Configuration object own all the properties written above aside from the modifier |
| modifier  | Modifier.aspectRatio(16/9) | The modifier to be applied to the Stream component.                                                                          |


__Note:__ The parameters only have effect when the component is first loaded. Changing the properties after the component is created will not have any effect. If you want to change the properties on the fly, we recommend using the `key` compose keyword on the top of your component to get controls over the loading. However, it will force everything to be reloaded (a lot quicker because of the cache).

## Configurations classes

### StreamConfig
*[StreamConfig.kt](streamplayer%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbitmovin%2Fstreams%2Fconfig%2FStreamConfig.kt)*

Meant to be used as a configuration object for the BitmovinStream component. It has the same properties as the BitmovinStream component.


### FullscreenConfig
*[FullscreenConfig.kt](streamplayer%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbitmovin%2Fstreams%2Fconfig%2FFullscreenConfig.kt)*

The FullscreenConfig is a configuration object that allows you to customize the behavior of the player when it enters/exits fullscreen mode.

### StyleConfig
*[StyleConfig.kt](streamplayer%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbitmovin%2Fstreams%2Fconfig%2FStyleConfig.kt)*

The StyleConfig is a configuration object that allows you to customize the appearance of the player. 
It has the priority over the style configuration set in the Bitmovin Stream dashboard.

You can find some predefined styles in the StreamThemes class.

### StreamListener
*[StreamListener.kt](streamplayer%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbitmovin%2Fstreams%2Fconfig%2FStreamListener.kt)*

The StreamListener is an interface that allows you to listen to the events of the player.
Caution: The listener only handle it's own Events. to handle the player events, you can use the onStreamReady event to get the player instance and add your own listener with player.on(...).

## Usage

### Simplest way to use the BitmovinStream component
```kotlin
BitmovinStream(
    streamId = "<YOUR-STREAM-ID>"
)
```

### Using the Configuration object
```kotlin
val config = BitmovinStreamConfig(
    streamId = "<YOUR-STREAM-ID>"
)
BitmovinStream(config)
```

### Gif-like stream player
```kotlin
val reusableGifVidConfig = StreamConfig(
    streamId = "<YOUR-STREAM-ID>",
    autoPlay = true,
    muted = true,
    loop = true,
    fullscreenConfig = FullscreenConfig(
        enable = false
    ),
    onStreamReady = { _, playerView ->
        playerView.isUiVisible = false
    }
)
BitmovinStream(config = reusableGifVidConfig)
```

### Using the StyleConfig
```kotlin
import java.awt.Color

val myStyleConfig = StyleConfigStream(
    playbackMarkerBorderColor = Color(0xFF01295F),
    playbackMarkerBgColor = Color(0xAF01295F),
    playbackTrackBufferedColor = Color(0x8FC5FFFD),
    textColor = Color(0xFF88D9E6),
    playbackTrackBgColor = Color(0xAF8B8BAE),
    playbackTrackPlayedColor = Color(0xFF526760),
    backgroundColor = Color.BLACK
)

BitmovinStream(
    streamId = "<YOUR-STREAM-ID>",
    poster = "really_cool_img.jpg",
    styleConfig = myStyleConfig
)
```

### More examples 
- [ScrollingExampleActivity.kt](testapp%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbitmovin%2Ftestapp%2FScrollingExampleActivity.kt)
- [VideoLibraryApp.kt](testapp%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbitmovin%2Ftestapp%2FVideoLibraryApp.kt)