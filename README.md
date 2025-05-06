


# Android project

## Port forwarding

There's a few utilities that can be used to help development:

`socat`
`nc`
`adb reverse`

## Exoplayer and MJPEG

Exoplayer cannot natively play MJPEG, which is the format used by FlashForge Adventurer 3.


This functionality is supplied by the Media3Avi library from dburckh: https://github.com/dburckh/Media3Avi

The aar file is added manually to the project at `appAndroid/lib/media3-avi-x.x.x.aar`

Unfortunately it doesn't support many formats, such as described here

https://github.com/dburckh/Media3Avi/issues/4
