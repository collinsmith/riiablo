package com.google.collinsmith70.diablo.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

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

        public static final AssetDescriptor<com.badlogic.gdx.audio.Sound> BUTTON
                = new AssetDescriptor("audio/cursor/button.wav", com.badlogic.gdx.audio.Sound.class);

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

            public static final AssetDescriptor<BitmapFont> _16;
            public static final AssetDescriptor<BitmapFont> _24;
            public static final AssetDescriptor<BitmapFont> _32;
            public static final AssetDescriptor<BitmapFont> _42;

            static {
                BitmapFontLoader.BitmapFontParameter params;

                params = new BitmapFontLoader.BitmapFontParameter();
                params.atlasName = EXOCET_PACK_PATH;

                _16 = new AssetDescriptor<BitmapFont>(
                        "font/exocet16.fnt",
                        BitmapFont.class, params);

                _24 = new AssetDescriptor<BitmapFont>(
                        "font/exocet24.fnt",
                        BitmapFont.class, params);

                _32 = new AssetDescriptor<BitmapFont>(
                        "font/exocet32.fnt",
                        BitmapFont.class, params);

                _42 = new AssetDescriptor<BitmapFont>(
                        "font/exocet42.fnt",
                        BitmapFont.class, params);
            }

        }

    }

}

}
