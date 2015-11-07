package com.google.collinsmith70.diablo.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.google.collinsmith70.diablo.asset.loader.TextureAtlasedBitmapFontLoader;

public class Assets {

private Assets() {
    //...
}

public static class Client {

    private Client() {
        //...
    }

    public static class Sound {

        private Sound() {
            //...
        }

        public static final AssetDescriptor<com.badlogic.gdx.audio.Sound> SELECT
                = new AssetDescriptor("audio/cursor/select.wav", com.badlogic.gdx.audio.Sound.class);

    }

    public static class Font {

        private Font() {
            //...
        }

        public static class Exocet {

            private Exocet() {
                //...
            }

            private static final String EXOCET_PACK_PATH = "font/exocet.pack";

            public static final AssetDescriptor<TextureAtlasedBitmapFont> _16;
            public static final AssetDescriptor<TextureAtlasedBitmapFont> _24;
            public static final AssetDescriptor<TextureAtlasedBitmapFont> _32;
            public static final AssetDescriptor<TextureAtlasedBitmapFont> _42;

            static {
                TextureAtlasedBitmapFontLoader.TextureAtlasedBitmapFontParameter params;

                params = new TextureAtlasedBitmapFontLoader.TextureAtlasedBitmapFontParameter();
                params.atlasPath = EXOCET_PACK_PATH;
                params.regionName = "exocet16";
                _16 = new AssetDescriptor<TextureAtlasedBitmapFont>(
                        "font/exocet16.fnt",
                        TextureAtlasedBitmapFont.class, params);

                params = new TextureAtlasedBitmapFontLoader.TextureAtlasedBitmapFontParameter();
                params.atlasPath = EXOCET_PACK_PATH;
                params.regionName = "exocet24";
                _24 = new AssetDescriptor<TextureAtlasedBitmapFont>(
                        "font/exocet24.fnt",
                        TextureAtlasedBitmapFont.class, params);

                params = new TextureAtlasedBitmapFontLoader.TextureAtlasedBitmapFontParameter();
                params.atlasPath = EXOCET_PACK_PATH;
                params.regionName = "exocet32";
                _32 = new AssetDescriptor<TextureAtlasedBitmapFont>(
                        "font/exocet32.fnt",
                        TextureAtlasedBitmapFont.class, params);

                params = new TextureAtlasedBitmapFontLoader.TextureAtlasedBitmapFontParameter();
                params.atlasPath = EXOCET_PACK_PATH;
                params.regionName = "exocet42";
                _42 = new AssetDescriptor<TextureAtlasedBitmapFont>(
                        "font/exocet42.fnt",
                        TextureAtlasedBitmapFont.class, params);
            }

        }

    }

}

}
