package com.google.collinsmith70.diablo.cvar;

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

    public static class Overlay {
        private Overlay() {
            //...
        }

        public static final Cvar<String> ConsoleFont = new Cvar<String>(
                "Client.Overlay.ConsoleFont",
                String.class, "default.fnt");

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
