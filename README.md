# Riiablo
This is my attempt at rebuilding Diablo II from scratch. There is still a long
ways to go, but a lot of the core is there. The game itself uses 100% original
Diablo II assets which are not (and will never be) provided, i.e., you should
already own a copy of the game to play this. I am going to do my best to remain
faithful to the original game, however I am planning on changing and adding
additional features as I think they become necessary (e.g., auto gold pickup).
I will write articles in the future explaining how I was able to accomplish
certain things, and how some core systems work.

Unlike the original game, this version supports just about any reasonable
aspect ratio (4:3, 16:9, 18:9, 21:9), however by design I'm locking the game to
480px in height because the original game assets are all geared for that (i.e.,
the width is dynamic). The expansion did introduce 800x600 support, and I may
end up supporting that for the desktop version in the future (specifically for
some of the multiplayer lobby stuff), but it's not necessary for the core
functionality. The mobile version currently uses 360px height to make selecting
UI elements easier, however since much of the UI panels are 480px, in-game is
locked at 480px, but I will change this in the future or at least provide
scaling on a per-component basis (text). This does run on Android, and I have
been using a Galaxy Note 5 as the min spec when testing, but older phones may
work as well, especially after optimizations have been made. I can already play
Diablo II on PC, my goal is to be able to sit back and play it casually with my
friends while also supporting cross-platform play. This game supports local
play that can then be taken online (similar to Open Battle.net), with a more
secure option being far beyond that.

*NOTE: This is not playable yet, but the game runs and you can load save files,
walk around a bit and look at your characters.* Game saves are not modified
yet, and 1.13c+ saves are supported (support for some other versions may be
added in the future, but it isn't a priority, and I expect most people to
create new characters anyways). I do not plan on, or want to make this game
compatible with playing with users using the original game client.

# Features
- Written using Java + LibGDX + OpenGL
- Runs on PC, Android and eventually more (IOS, Linux, etc.)
- Cross-platform multiplayer
- Dedicated servers, TCP/IP (listen servers) connections, and single player
- Full console, including CVAR support and key bindings
- Controller support
- Platform-specific features (Android touch, PC mouse, etc)
- Scalable UI (Partial for now)

# Screenshots
![In-Game](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-1.png)
![Create Character](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-2.png)

# Setup
The Android SDK is required to build the project by default (even if you want
the desktop module only), however you can circumvent this by following
[this post](https://github.com/collinsmith/riiablo/issues/6#issuecomment-465661949)
which explains which files to modify and references to delete.

#### Windows
```$etc
git clone https://github.com/collinsmith/riiablo.git
cd riiablo
gradlew desktop:dist
java -jar desktop/build/libs/desktop-1.0.jar -home "C:\diablo"
```
Sometimes it may be necessary to also run `gradlew --refresh-dependencies`

#### Linux / MacOS
I have not tested support on any Linux distros yet -- but it should work. Since
this project is developed using the MPQ format from the Windows installation,
it's likely that only the Windows MPQs will work. If there are any additional
steps required, let me know and I can add them until I get a chance to test
this myself.

#### Android
Steps will be provided when the project is further along and the configuration
process can be cleaned up a bit. There are Android-specific implementations of
some core gameplay systems, but everything is validated to work on Android.

#### IntelliJ
- [Importing into Intellij/Android Studio](https://libgdx.badlogicgames.com/documentation/gettingstarted/Importing%20into%20IDE.html#intellij)
- Default run configurations are included within [.idea/runConfigurations](https://github.com/collinsmith/riiablo/tree/master/.idea/runConfigurations),
however the program args must be changed to point to _your_ Diablo 2 directory.
The default resolution is 854x480, other configurations are provided to ensure
a wide range of support `-w` arg can be used to start in windowed mode, while
`F12` can be used in-game to disable the debug UI.

# MPQ Viewer
Still a work in progress, but this allows you to look at the game's assets and
is used to test my MPQ library implementation. Currently this does not allow
for viewing all files that the game can load, but those should hopefully come
in the future. This should be a replacement for DR Test in the future, at least
in a general sense, since the UI I made is much more helpful when writing the
game code.
<details>
	<summary>Screenshot</summary>
	<img src="https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-3.png" alt="MPQ Viewer">
</details>

# Map Builder
Used to test the map building algorithm/renderer. This is very basic at the
moment.
<details>
	<summary>Screenshot</summary>
	<img src="https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-4.png" alt="Map Builder">
</details>