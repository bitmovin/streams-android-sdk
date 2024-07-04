package com.bitmovin.streams

internal val tearOfSteel =
    """
    {
      "key": "_________________________",
      "sources": {
        "title": "Tears of Steel",
        "hls": "https://streams.bitmovin.com/cot1gjsd1q7m56d4ch3g/manifest.m3u8",
        "poster": "https://streams.bitmovin.com/cot1gjsd1q7m56d4ch3g/poster",
        "thumbnailTrack": {
          "url": "https://cdn.bitmovin.com/content/assets/streams-sample-video/tos/sprites/low/sprite.vtt"
        }
      },
      "analytics": {
        "key": "_________________________",
        "videoId": "cot1gjsd1q7m56d4ch3g",
        "videoTitle": "Tears of Steel"
      },
      "type": "VIDEO",
      "styleConfig": {
        "playerStyle": {
          "playbackMarkerBgColor": "rgba(248, 250, 252, 1)",
          "playbackMarkerBorderColor": "rgba(47, 66, 74, 1)",
          "playbackTrackPlayedColor": "rgba(140, 28, 113, 1)",
          "playbackTrackBufferedColor": "rgba(168, 255, 171, 1)",
          "playbackTrackBgColor": "rgba(255, 150, 150, 0.35)",
          "textColor": "rgba(61, 219, 255, 1)",
          "backgroundColor": "rgba(176, 40, 40, 1)"
        },
        "watermarkUrl": "https://streams-resources.s3.eu-west-1.amazonaws.com/watermark/017906b0-1031-4457-9edb-e3d12df645b9.jpg",
        "watermarkTargetLink": "https://dashboard.bitmovin.com/streams/video/cp7jaalsrjnnig8jcdj0?tabId=2&orgId=59c5fb25-6901-4354-a37d-1dfb9bf9b17c&userId=9ebfa014-8812-43d6-8c21-9dcf2e980552"
      },
      "adConfig": {
        "ads": [
          {
            "position": "post",
            "url": "https://bitmovin-a.akamaihd.net/content/testing/ads/testad2s.mp4",
            "type": "vast"
          },
          {
            "position": "pre",
            "url": "https://file-examples.com/storage/fed5266c9966708dcaeaea6/2018/04/file_example_MOV_480_700kB.mov",
            "type": "vast"
          },
          {
            "position": "50%",
            "url": "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=",
            "type": "vast"
          }
        ]
      }
    }
    """.trimIndent()
