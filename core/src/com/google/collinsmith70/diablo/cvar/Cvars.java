package com.google.collinsmith70.diablo.cvar;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.util.Locale;

public class Cvars {

private Cvars() {
    //...
}

public static void loadAll() {
    Client.Locale.load();

    Client.Sound.Enabled.load();
    Client.Sound.Sfx.Enabled.load();
    Client.Sound.Sfx.Volume.load();
    Client.Sound.Music.Enabled.load();
    Client.Sound.Music.Volume.load();

    Client.Overlay.ShowFps.load();
    Client.Overlay.ConsoleFont.load();
    Client.Overlay.ConsoleFontColor.a.load();
    Client.Overlay.ConsoleFontColor.r.load();
    Client.Overlay.ConsoleFontColor.g.load();
    Client.Overlay.ConsoleFontColor.b.load();

    Client.Render.Windowed.load();
    Client.Render.VSync.load();
    Client.Render.AnimationBounds.load();

    Client.Input.Vibrations.load();
}

public static class Client {

    private Client() {
        //...
    }

    public static final Cvar<java.util.Locale> Locale = new Cvar<java.util.Locale>("Client.Locale",
            java.util.Locale.class,
            java.util.Locale.getDefault(),
            new CvarLoadListener<java.util.Locale>() {
                @Override
                public Locale onCvarLoaded(String value) {
                    return java.util.Locale.forLanguageTag(value);
                }

                @Override
                public String toString(Locale locale) {
                    return locale.toLanguageTag();
                }
            });

    public static class Sound {
        private Sound() {
            //...
        }

        public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                "Client.Sound.Enabled",
                Boolean.class, Boolean.TRUE);

        public static class Sfx {
            private Sfx() {
                //...
            }

            public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                    "Client.Sound.Sfx.Enabled",
                    Boolean.class, Boolean.TRUE);

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Sfx.Volume",
                    Float.class, 1.0f);
        }

        public static class Music {
            private Music() {
                //...
            }

            public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                    "Client.Sound.Music.Enabled",
                    Boolean.class, Boolean.TRUE);

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Music.Volume",
                    Float.class, 1.0f);
        }
    }

    public static class Render {
        private Render() {
            //...
        }

        public static final Cvar<Boolean> Windowed = new Cvar<Boolean>(
                "Client.Render.Windowed",
                Boolean.class, Boolean.FALSE);

        public static final Cvar<Boolean> VSync = new Cvar<Boolean>(
                "Client.Render.VSync",
                Boolean.class, Boolean.FALSE);

        public static final Cvar<Boolean> AnimationBounds = new Cvar<Boolean>(
                "Client.Render.AnimationBounds",
                Boolean.class, Boolean.FALSE);

        public static final Cvar<Float> Scale = new Cvar<Float>(
                "Client.Render.Scale",
                Float.class, 1.0f);
    }

    public static class Overlay {
        private Overlay() {
            //...
        }

        public static final Cvar<String> CommandPrefix
                = new Cvar<String>(
                "Client.Overlay.CommandPrefix",
                String.class, ">");

        public static final Cvar<AssetDescriptor<BitmapFont>> ConsoleFont
                = new Cvar<AssetDescriptor<BitmapFont>>(
                "Client.Overlay.ConsoleFont",
                null,
                new AssetDescriptor<BitmapFont>("default.fnt", BitmapFont.class),
                new CvarLoadListener<AssetDescriptor<BitmapFont>>() {
                    @Override
                    public AssetDescriptor<BitmapFont> onCvarLoaded(String value) {
                        return new AssetDescriptor<BitmapFont>(value, BitmapFont.class);
                    }

                    @Override
                    public String toString(AssetDescriptor<BitmapFont> assetDescriptor) {
                        return assetDescriptor.fileName;
                    }
                });

        public static final Cvar<Boolean> ShowFps = new Cvar<Boolean>(
                "Client.Overlay.ShowFps",
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

    public static class Input {
        public static final Cvar<Boolean> Vibrations
                = new Cvar<Boolean>(
                "Client.Input.Vibrations",
                Boolean.class, Boolean.TRUE);
    }
}
}
