package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.validator.NonNullSubclassValidator;

import java.lang.reflect.Field;
import java.util.Locale;

public class Cvars {

public static void addTo(CvarManager cvarManager) {
    addTo(cvarManager, Cvars.class);
}

private static void addTo(CvarManager cvarManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
        if (Cvar.class.isAssignableFrom(field.getType())) {
            try {
                cvarManager.add((Cvar)field.get(null));
            } catch (IllegalAccessException e) {
                Gdx.app.log(Cvars.class.getSimpleName(), "Unable to access cvar: " + e.getMessage());
            }
        }
    }

    for (Class<?> subclass : clazz.getClasses()) {
        addTo(cvarManager, subclass);
    }
}

private Cvars() {
    //...
}

public static class Client {

    private Client() {
        //...
    }

    public static final Cvar<Locale> Locale = new Cvar<Locale>(
            "Client.Locale",
            "Locale for the game client",
            Locale.class,
            java.util.Locale.getDefault(),
            new NonNullSubclassValidator<Locale>(Locale.class));

    public static class Console {

        private Console() {
            //...
        }

        public static final Cvar<String> Prefix = new Cvar<String>(
                "Client.Console.Prefix",
                "String which precedes console commands within the GUI",
                String.class,
                ">",
                Validator.ACCEPT_NON_NULL_NON_EMPTY_STRING);

    }

    public static class Sound {

        private Sound() {
            //...
        }

        public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                "Client.Sound.Enabled",
                "Controls whether or not sounds will play",
                Boolean.class,
                Boolean.TRUE);

        public static class Effects {

            private Effects() {
                //...
            }

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Effects.Volume",
                    "Controls the volume level for sound effects",
                    Float.class,
                    1.0f);

        }

        public static class Environment {

            private Environment() {
                //...
            }

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Environment.Volume",
                    "Controls the volume level for environment effects",
                    Float.class,
                    1.0f);

        }

        public static class Voice {

            private Voice() {
                //...
            }

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Voice.Volume",
                    "Controls the volume level for NPC voice dialog",
                    Float.class,
                    1.0f);

        }

        public static class Music {

            private Music() {
                //...
            }

            public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                    "Client.Sound.Music.Enabled",
                    "Controls whether or not music will play",
                    Boolean.class,
                    Boolean.TRUE);

            public static final Cvar<Float> Volume = new Cvar<Float>(
                    "Client.Sound.Music.Volume",
                    "Controls the volume level for music tracks",
                    Float.class,
                    1.0f);

        }
    }
}

}
