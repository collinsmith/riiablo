package com.google.collinsmith70.diablo.cvar;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.util.Locale;

public class Cvars {
public static class Client {
    private Client() {
        //...
    }

    public static final Cvar<java.util.Locale> Locale = new Cvar<java.util.Locale>("Client.Locale",
            java.util.Locale.class,
            java.util.Locale.getDefault(),
            java.util.Locale.getDefault().toLanguageTag(),
            new CvarLoadListener<java.util.Locale>() {
        @Override
        public Locale onCvarLoaded(String value) {
            return java.util.Locale.forLanguageTag(value);
        }
    });

    public static class Audio {
        private Audio() {
            //...
        }

        public static class Volume {
            private Volume() {
                //...
            }

            public static final Cvar<Float> Sfx = new Cvar<Float>(
                    "Client.Audio.Volume.Sfx",
                    Float.class, 1.0f);

            public static final Cvar<Float> Music = new Cvar<Float>(
                    "Client.Audio.Volume.Music",
                    Float.class, 1.0f);
        }
    }

    public static class Render {
        private Render() {
            //...
        }

        public static final Cvar<Boolean> Windowed = new Cvar<Boolean>(
                "Client.Render.Windowed",
                Boolean.class, Boolean.TRUE);
    }

    public static class Overlay {
        private Overlay() {
            //...
        }

        public static final Cvar<AssetDescriptor<BitmapFont>> ConsoleFont
                = new Cvar<AssetDescriptor<BitmapFont>>(
                "Client.Overlay.ConsoleFont",
                null,
                new AssetDescriptor<BitmapFont>("default.fnt", BitmapFont.class),
                "default.fnt",
                new CvarLoadListener<AssetDescriptor<BitmapFont>>() {
            @Override
            public AssetDescriptor<BitmapFont> onCvarLoaded(String value) {
                return new AssetDescriptor<BitmapFont>(value, BitmapFont.class);
            }
        });

        public static final Cvar<Boolean> ShowFps = new Cvar<Boolean>(
                "Client.Overlay.ShowFps",
                Boolean.class, Boolean.FALSE);

        public static final Cvar<Boolean> VSyncEnabled = new Cvar<Boolean>(
                "Client.Overlay.VSyncEnabled",
                Boolean.class, Boolean.FALSE);

        public static class ConsoleFontColor {
            private ConsoleFontColor() {
                //...
            }

            public static final Cvar<Float> r = new Cvar<Float>("Client.Overlay.ConsoleFontColor.r",
                    Float.class, 1.0f);
            public static final Cvar<Float> g = new Cvar<Float>("Client.Overlay.ConsoleFontColor.g",
                    Float.class, 1.0f);
            public static final Cvar<Float> b = new Cvar<Float>("Client.Overlay.ConsoleFontColor.b",
                    Float.class, 1.0f);
            public static final Cvar<Float> a = new Cvar<Float>("Client.Overlay.ConsoleFontColor.a",
                    Float.class, 1.0f);
        }
    }
}
}
