
This is the Octo FlashForge project, loosely inspired by what OctoPrint does. However I never used OctoPrint, or ever owned a printer that supports it.

The goal of this project is to provide a way to monitor and control the FlashForge Adventurer 3 printer, which does not have any official API or support for third party software.

## Links

Related project is https://github.com/Slugger2k/FlashForgePrinterApi, done in Java and communicating over TCP/IP, which is what FlashPrint does.



# Features

| Feature                                                                       | Android             | Desktop       | 
|-------------------------------------------------------------------------------|---------------------|---------------|
| Store and load ip:port                                                        | ✅                   |               |
| Scan network for printers                                                     |                     |               |
| Video streaming                                                               | ✅ (through webview) |               |
| Monitor printer basic info (progress and temperature)                         |                     |               |
| Proxy printer so FlashPrint can connect through this app                      | N/A                 |               |
| Monitor and display all FlashPrint traffic                                    |                     |
| Save gcode sent to printer through proxy                                      | N/A                 |               |
| Send gcode to printer                                                         |                     |               |
| Post process gcode from other slicers to make it compatible with Adventurer 3 |                     | Extruder temp |


# Android project

## Port forwarding

There are a few utilities that can be used to help development:

`socat`
`nc`
`adb reverse`

## Exoplayer and MJPEG

Exoplayer is the official way of playing video over Android. Unfortunately, it cannot natively play MJPEG, which is the format used by FlashForge Adventurer 3.


This functionality is supplied by the Media3Avi library from dburckh: https://github.com/dburckh/Media3Avi

The aar file is added manually to the project at `appAndroid/lib/media3-avi-x.x.x.aar`

It does support MJPEG over AVI, but not MJPEG over the format wrapping it by FlashPrint, which is still to be determined by this project.

See https://github.com/dburckh/Media3Avi/issues/4

### Live transcoding with ffmpeg 

`ffmpeg -i video.mp4 -r 30 -vcodec libx264 -b:v 2000k -maxrate 3000k -bufsize 5000k -threads 2 -vf scale=-2:720 -acodec libfaac -ab 192k -ar 44100 -ac 1 -f flv rtmp://localhost/live`


`ffmpeg -i video.mp4 -r 30 -vcodec libx264  -bufsize 5000k -threads 2 -vf scale=-2:720 -acodec copy -f flv rtmp://localhost/live`



`ffmpeg -i http://127.0.0.1:9090/?action=stream output.mp4 -r 30 -vcodec libmp4 -b:v 2000k -maxrate 3000k -bufsize 5000k -threads 8 -acodec copy -ab 192k -ar 44100 -ac 1`  