package com.google.collinsmith70.diablo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import com.google.collinsmith70.diablo.asset.AtlasedBitmapFont;
import com.google.collinsmith70.diablo.asset.loader.AtlasedBitmapFontLoader;

public final class Assets {
private Assets() {
    //...
}

public static final class Fonts {
    private Fonts() {
        //...
    }

    public static final String FONTS = "font/";

    public static final String EXOCET = FONTS + "exocet.pack";
    public static final String EXOCET16 = FONTS + "exocet16.fnt";
    public static final String EXOCET24 = FONTS + "exocet24.fnt";
    public static final String EXOCET32 = FONTS + "exocet32.fnt";
    public static final String EXOCET42 = FONTS + "exocet42.fnt";

    public static final AssetDescriptor<AtlasedBitmapFont> ASSET_EXOCET16;
    public static final AssetDescriptor<AtlasedBitmapFont> ASSET_EXOCET24;
    public static final AssetDescriptor<AtlasedBitmapFont> ASSET_EXOCET32;
    public static final AssetDescriptor<AtlasedBitmapFont> ASSET_EXOCET42;

    static {
        AtlasedBitmapFontLoader.AtlasedBitmapFontParameter params;

        params = new AtlasedBitmapFontLoader.AtlasedBitmapFontParameter();
        params.atlasPath = EXOCET.toString();
        params.regionName = "exocet16";
        ASSET_EXOCET16 = new AssetDescriptor<AtlasedBitmapFont>(EXOCET16.toString(), AtlasedBitmapFont.class, params);

        params = new AtlasedBitmapFontLoader.AtlasedBitmapFontParameter();
        params.atlasPath = EXOCET.toString();
        params.regionName = "exocet24";
        ASSET_EXOCET24 = new AssetDescriptor<AtlasedBitmapFont>(EXOCET24.toString(), AtlasedBitmapFont.class, params);

        params = new AtlasedBitmapFontLoader.AtlasedBitmapFontParameter();
        params.atlasPath = EXOCET.toString();
        params.regionName = "exocet32";
        ASSET_EXOCET32 = new AssetDescriptor<AtlasedBitmapFont>(EXOCET32.toString(), AtlasedBitmapFont.class, params);

        params = new AtlasedBitmapFontLoader.AtlasedBitmapFontParameter();
        params.atlasPath = EXOCET.toString();
        params.regionName = "exocet42";
        ASSET_EXOCET42 = new AssetDescriptor<AtlasedBitmapFont>(EXOCET42.toString(), AtlasedBitmapFont.class, params);
    }
}

public static final class Textures {
    private Textures() {
        //...
    }

    public static final String TEXTURES = "textures/";

    public static final class Backgrounds {
        private Backgrounds() {
            //...
        }

        public static final String BACKGROUNDS = TEXTURES + "backgrounds/";

        public static final String SPLASH = BACKGROUNDS + "splash.png";
        public static final AssetDescriptor<Texture> ASSET_SPLASH = new AssetDescriptor<Texture>(SPLASH.toString(), Texture.class);

        public static final String HOME = BACKGROUNDS + "home.png";
        public static final AssetDescriptor<Texture> ASSET_HOME = new AssetDescriptor<Texture>(HOME.toString(), Texture.class);
    }

    public static final String BUTTONS = TEXTURES + "buttons.pack";
    public static final AssetDescriptor<TextureAtlas> ASSET_BUTTONS = new AssetDescriptor<TextureAtlas>(BUTTONS.toString(), TextureAtlas.class);

    public static final String OPTIONS_PANEL = TEXTURES + "options_panel.pack";
    public static final AssetDescriptor<TextureAtlas> ASSET_OPTIONS_PANEL = new AssetDescriptor<TextureAtlas>(OPTIONS_PANEL.toString(), TextureAtlas.class);
}

public static final class Animations {
    private Animations() {
        //...
    }

    public static final String ANIMATIONS = "animations/";

    public static final String LOGO = ANIMATIONS + "logo.png";
    public static final AssetDescriptor<Texture> ASSET_LOGO = new AssetDescriptor<Texture>(LOGO.toString(), Texture.class);
}

public static final class Audio {
    private Audio() {
        //...
    }

    public static final String AUDIO = "audio/";

    public static final class Musics {
        public static final String MUSICS = AUDIO + "music/";

        public static final String INTRO = MUSICS + "intro.ogg";
        public static final AssetDescriptor<Music> ASSET_INTRO = new AssetDescriptor<Music>(INTRO.toString(), Music.class);
    }

    public static final class Cursor {
        private Cursor() {
            //...
        }

        public static final String CURSOR = AUDIO + "cursor/";

        public static final String SELECT = CURSOR + "select.wav";
        public static final AssetDescriptor<Sound> ASSET_SELECT = new AssetDescriptor<Sound>(SELECT.toString(), Sound.class);

        public static final String BUTTON = CURSOR + "button.wav";
        public static final AssetDescriptor<Sound> ASSET_BUTTON = new AssetDescriptor<Sound>(BUTTON.toString(), Sound.class);
    }


    public static final class Sounds {
        private Sounds() {
            //...
        }

        public static final String SOUNDS = AUDIO + "sounds";
    }
}
}
