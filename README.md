# Riiablo

[Join us on Discord!](https://discord.gg/qRbWYNM)

This is my attempt at rebuilding Diablo II from scratch. There is still a long
way to go, but a lot of the core is there. The game itself uses 100% original
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
end up supporting for the desktop version in the future (specifically for some
multiplayer lobby stuff), but it's not necessary for the core functionality. The
mobile version currently uses 360px height to make selecting UI elements easier,
however since much of the UI panels are 480px, in-game is locked at 480px, but I
will change this in the future or at least provide scaling on a per-component
basis (text). This does run on Android, and I have been using a Galaxy Note 5 as
the min spec when testing, but older phones may work as well, especially after
optimizations have been made. I can already play Diablo II on PC, my goal is to
be able to sit back and play it casually with my friends while also supporting
cross-platform play. This game supports local play that can then be taken online
(similar to Open Battle.net), with a more secure option being far beyond that.

*NOTE: This is not playable yet, but the game runs and you can load save files,
walk around a bit and look at your characters.* Game saves are not modified
yet, and 1.13c+ saves are supported (support for some other versions may be
added in the future, but it isn't a priority, and I expect most people to
create new characters anyways). I do not plan on, or want to make this game
compatible with playing with users using the original game client.

[![SP Test](https://media.giphy.com/media/8PoUfw52rtlACeWMbB/giphy.gif)](https://www.youtube.com/watch?v=oKYNsIPr0tY)

# Features
- Written using Java + LibGDX + OpenGL + Flatbuffers + Netty
- Runs on PC, Android and eventually more (IOS, Linux, etc.)
- Cross-platform multiplayer
- Dedicated servers, TCP/IP (listen servers), and single player
- Full console, including CVAR support and custom key bindings
- Controller support
- Platform-specific features (Android touch, PC mouse, etc)
- Scalable UI

# Screenshots
![In-Game](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-1.png)
![Create Character](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-2.png)
![Paladin](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-5.png)
![Android](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Android-1.png)
[![MP Test](https://media.giphy.com/media/U7aXAwLcaQM0lxvPVY/giphy.gif)](https://www.youtube.com/watch?v=B2XhiS_JbIA)

# Setup
The Android SDK is required to build the project by default (even if you want
the desktop module only), however you can circumvent this by following
[this post](https://github.com/collinsmith/riiablo/issues/6#issuecomment-465661949)
which explains which files to modify and references to delete.

Two environment variables can be used to automatically set the D2 installation
and save directories if they are not automatically detected, and you do not want
to use command-line arguments every time you launch the game.
```bash
D2_HOME=/Diablo2
D2_SAVE=/Diablo2/Save
```

Otherwise, using the `--help` command-line argument will show a list of all
available options -- including manually specifying your D2 installation.

#### Building
```bash
git clone https://github.com/collinsmith/riiablo.git
cd riiablo
gradlew desktop:run
```

#### Windows
Typical D2 installations should be automatically detected and configured
(including existing saved games). If a D2 installation cannot be detected (or
you would like to do something like change which saved games to use), see
the above instructions on using command-line arguments.

#### Linux / MacOS
This project was developed using the native Win32 MPQ files, so you will need
to copy them from your Windows installation (whether that be Wine or just a
copy of the files). Detection of the MPQs may be spotty and require manually
specifying them via command-line arguments or environment variables (see above).

#### Android
Debug APKs can be created with `gradlew android:assembleDebug`, however
configuring the app on a device is a bit of a pain at this time unless you can
manually copy the resources from your Win32 installation onto your device in the
app data directory. This will require having `logcat` running so that you can
see any errors it spits out about where it's looking for the files. This process
will be made easier in the future.

#### IntelliJ
- [Importing into Intellij/Android Studio](https://libgdx.badlogicgames.com/documentation/gettingstarted/Importing%20into%20IDE.html#intellij)
- Default run configurations are provided within [.idea/runConfigurations](https://github.com/collinsmith/riiablo/tree/master/.idea/runConfigurations).
The default resolution is 854x480, other configurations are provided to ensure
a wide range of support `--windowed` arg can be used to start in windowed mode,
while `F12` can be used in-game to disable the debug UI.
- Environment variables `D2_HOME` and `D2_SAVE` can be used if you are a pluggy
user.

# Tools
Aside from the main tools below, other tools are contained within `:tools`. To
view all available tools, use `gradlew :tools:projects -q`. `--help` is your
friend.

# MPQ Viewer
Allows you to look at the game's assets and debug issues with specific files.
Not all file types are supported at this time, but those should hopefully come
in the future. This is a tool used in development and is not a replacement for
tools like DR Test, but does provide the same functionality.
<details>
	<summary>Screenshot</summary>
	<img src="https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-3.png" alt="MPQ Viewer">
</details>

# Map Viewer
Tests the map building algorithm/renderer. This is very basic at the moment.
<details>
	<summary>Screenshot</summary>
	<img src="https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-4.png" alt="Map Builder">
</details>
