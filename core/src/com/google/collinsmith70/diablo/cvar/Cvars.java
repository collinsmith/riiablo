package com.google.collinsmith70.diablo.cvar;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.lang.reflect.Field;
import java.util.Locale;

public class Cvars {

private Cvars() {
    //...
}

public static void loadAll() {
    init(Cvars.class);
}

public static void init(Class<?> clazz) {
    // Hack to instantiate all Cvars
    for (Field field : clazz.getFields()) {
        //...
    }

    for (Class subclass : clazz.getClasses()) {
        init(subclass);
    }
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

        public static class Effects {
            private Effects() {
                //...
            }

            public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                    "Client.Sound.Effects.Enabled",
                    Boolean.class, Boolean.TRUE);

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Effects.Volume",
                    Float.class, 1.0f);
        }

        public static class Environment {
            private Environment() {
                //...
            }

            public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                    "Client.Sound.Environment.Enabled",
                    Boolean.class, Boolean.TRUE);

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Environment.Volume",
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

        public static class Bounds {
            private Bounds() {
                //...
            }

            public static final Cvar<Float> x = new Cvar<Float>(
                    "Client.Render.Bounds.x",
                    Float.class, 1.0f);

            public static final Cvar<Float> y = new Cvar<Float>(
                    "Client.Render.Bounds.y",
                    Float.class, 1.0f);

        }

        public static final Cvar<Float> Gamma = new Cvar<Float>(
                "Client.Render.Gamma",
                Float.class, 1.0f);

        public static final Cvar<Float> Brightness = new Cvar<Float>(
                "Client.Render.Brightness",
                Float.class, 1.0f);
    }

    public static class Console {
        private Console() {
            //...
        }

        public static final Cvar<String> CommandPrefix
                = new Cvar<String>(
                "Client.Console.CommandPrefix",
                String.class, ">");

        public static final Cvar<Integer> SuggestChars = new Cvar<Integer>(
                "Client.Console.SuggestChars",
                Integer.class, 2);

        public static final Cvar<Integer> HistoryBuffer = new Cvar<Integer>(
                "Client.Console.HistoryBuffer",
                Integer.class, 64);

        public static final Cvar<AssetDescriptor<BitmapFont>> Font
                = new Cvar<AssetDescriptor<BitmapFont>>(
                "Client.Console.Font",
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

        public static final Cvar<Float> Height = new Cvar<Float>(
                "Client.Console.Height",
                Float.class, 1.0f);

        public static class Color {
            private Color() {
                //...
            }

            public static final Cvar<Float> r = new Cvar<Float>(
                    "Client.Console.Color.r",
                    Float.class, 1.0f);
            public static final Cvar<Float> g = new Cvar<Float>(
                    "Client.Console.Color.g",
                    Float.class, 1.0f);
            public static final Cvar<Float> b = new Cvar<Float>(
                    "Client.Console.Color.b",
                    Float.class, 1.0f);
            public static final Cvar<Float> a = new Cvar<Float>(
                    "Client.Console.Color.a",
                    Float.class, 1.0f);
        }
    }

    public static class Overlay {
        private Overlay() {
            //...
        }

        public static final Cvar<Boolean> ShowFps = new Cvar<Boolean>(
                "Client.Overlay.ShowFps",
                Boolean.class, Boolean.FALSE);
    }

    public static class Input {
        public static final Cvar<Boolean> Vibrations
                = new Cvar<Boolean>(
                "Client.Input.Vibrations",
                Boolean.class, Boolean.TRUE);
    }
}
}
