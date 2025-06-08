
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

# Development

A quick way to check if the project is healthy is:

```bash
./gradlew :appDesktop:build :appAndroid:assembleDebug    

```

A more complete and longer way is to run all tests:

```bash
./gradlew :appDesktop:check :appAndroid:check
```


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


# Desktop project

Note: assume for all instructions that you run `alias gw=./gradlew`


## Running

The gradle way is `gw :appDesktop:run`

It assumes you're forwarding to your printer with socat, so the desktop code only points to localhost

Forward the TCP traffic with the following (replace xxx with your correct address):

Data
```shell
socat -d -d TCP-LISTEN:8899,mss=1024,fork TCP:192.168.0.xxx:8899,mss=1024
```

Camera streaming
```shell
socat -d -d TCP-LISTEN:9090,mss=1024,fork TCP:192.168.0.xxx:8080,mss=1024
```

### Fat jar

You can build a fat jar with 

`gw :appDesktop:packageUberJarForCurrentOS`

and then run it with 

`java -jar ./appDesktop/build/compose/jars/net.amazingdomain.octo_flashforge-linux-x64-1.0.0.jar`

## Distribution

To distribute the project you should run one of the following task:

`gw :appDesktop:packageDeb`

The file will be created at  `./appDesktop/build/compose/binaries/main/deb/octo-flashforge_1.0.0-1_amd64.deb`

### Troubleshooting

It is possible that this command will fail with this message

```
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':appDesktop:checkRuntime'.
> Failed to check JDK distribution: 'jpackage' is missing
  JDK distribution path: /home/xxxxxx/android-studio/jbr


```

For some reason, Jetbrains java distribution does not include the jpackage tool, which is required to create the deb package. The solution: 

#### the manual way
- download a full jdk such as Zulu or your preferred one; select the latest version 17.
- unzip it in any folder you want (just don't add it to your git repo); let's call it ZULU_HOME
- set the environment variable `JAVA_HOME` to point to the unzipped folder; such as `export JAVA_HOME=$ZULU_HOME`
- set PATH to your java folder; such as `export PATH=$JAVA_HOME/bin:$PATH`
- verify it with `which java` and `java -version`

Zulu: https://www.azul.com/downloads/#zulu
OpenJdk: https://jdk.java.net/archive/


#### Mac OS

Use `brew` and `jbr`.

