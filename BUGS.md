# Bugs 

## on UI

### #1 Desktop app - has bad video when using JDK 21 and moving to secondary display

Reproduce

- Set terminal to Java 21,
- `./gradlew run`
- Wait until window opens, move to secondary display

Expected: Everything looks good.
Actual: VLC window moves around and obscures the data on top of window.
Workaround: use Java 17

### #2 Android - ip address change not propagated

When user changes ip address, main activity doesn't propagate it all the way


### #3 Android - when entering new ip address, keyboard covers screen

Expected: keyboard allows screen to resize and scroll

Nice to have: tab moves to next field
Nice to have: keyboard has a tick to move to next field

