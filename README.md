# LibGDX Diablo II
This is my attempt at rebuilding Diablo II from scratch. There is still a long
ways to go, but a lot of the core is there. The game itself uses 100% original
Diablo II assets which are not (and will never) be provided, i.e., you should
already own a copy of the game to play this. I am going to do my best to remain
faithful to the original game, however I am planning on changing and adding
additional features as I think they become necessary. I will write articles in
the future explaining how I was able to accomplish certain things, and how
some systems work.

Some important changes that I will note are that this version supports a 16:9
aspect ratio (the original was 640x480 and expansion was 800x600), and will
eventually support wider. Because I'm limited to the original game assets, the
game will be upscaled to the desired resolution, and 480p seems to work well on
a smart phone. This does run on Android, and I have been using a Galaxy Note 5
as the min spec when testing, but older phones may work as well. I can already
play Diablo II on PC, my goal is to be able to sit back and play it casually
with my friends while also supporting cross-platform play. This game will one
day support local play that can then be taken online (similar to Open
Battle.net), with a more secure option being far beyond that.

*NOTE: This is not playable yet, but the game runs and you can load save files,
walk around a bit and look at your characters.* Game saves are not modified
yet, and 1.13c+ saves are supported (support for some other versions may be
added in the future, but it isn't a priority, and I expect most people to
create new characters anyways). I do not plan on, or want to make this game
compatible with playing with users using the original game client.

# Features
- Written using Java + LibGDX + OpenGL
- Runs on PC, Android and eventually more (IOS, Linux, etc.)
- Full console, including CVAR support and key bindings
- TODO Controller support
- TODO Platform-specific features (Android touch, PC mouse, etc)

# Screenshots
![In-Game](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-1.png)
![Create Character](https://raw.githubusercontent.com/collinsmith/diablo/master/screenshots/Clipboard-2.png)

# MPQ Viewer
Still a work in progress, but this allows you to look at the game's assets and
is used to test my MPQ library implementation. Currently this does not allow
for viewing all files that the game can load, but those should hopefully come
in the future. This should be a replacement for DR Test in the future, at least
in a general sense, since the UI I made is much more helpful when writing the
game code.

# Map Builder
Used to test the map building algorithm. This is very basic at the moment.