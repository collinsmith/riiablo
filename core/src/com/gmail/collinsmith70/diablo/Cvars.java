package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;
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

    public static class Sound {

        private Sound() {
            //...
        }

        public static final Cvar<Boolean> Enabled = new Cvar<Boolean>(
                "Client.Sound.Enabled",
                "Controls whether or not sounds will play",
                Boolean.class,
                Boolean.TRUE);

    }
}

}
