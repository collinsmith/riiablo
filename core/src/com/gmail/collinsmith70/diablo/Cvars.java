package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.cvar.ValidatableCvar;
import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.validator.NonNullSubclassValidator;
import com.gmail.collinsmith70.util.validator.NumberRangeValidator;

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
          cvarManager.add((Cvar) field.get(null));
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
  }

  public static class Client {

    private Client() {
    }

    public static final Cvar<Locale> Locale = new ValidatableCvar<Locale>(
            "Client.Locale",
            "Locale for the game client",
            Locale.class,
            java.util.Locale.getDefault(),
            new NonNullSubclassValidator<Locale>(Locale.class));

    public static final Cvar<Boolean> Windowed = new ValidatableCvar<Boolean>(
            "Client.Windowed",
            "Whether or not the client is in windowed mode. " +
                    "Note: This cvar is ignored when the client " +
                    "is started with the -windowed option",
            Boolean.class,
            Boolean.FALSE);

    public static class Console {

      private Console() {
      }

      public static final Cvar<String> Prefix = new ValidatableCvar<String>(
              "Client.Console.Prefix",
              "String which precedes console commands within the GUI",
              String.class,
              ">",
              Validator.ACCEPT_NON_NULL_NON_EMPTY_STRING);

      public static final Cvar<String> Font = new ValidatableCvar<String>(
              "Client.Console.Font",
              "Font for the in-game console output stream",
              String.class,
              "default.fnt",
              Validator.ACCEPT_NON_NULL_NON_EMPTY_STRING);

    }

    public static class Sound {

      private Sound() {
        //...
      }

      public static final Cvar<Boolean> Enabled = new ValidatableCvar<Boolean>(
              "Client.Sound.Enabled",
              "Controls whether or not sounds will play",
              Boolean.class,
              Boolean.TRUE);

      public static class Effects {

        private Effects() {
          //...
        }

        public static final Cvar<Float> Volume = new ValidatableCvar<Float>(
                "Client.Sound.Effects.Volume",
                "Controls the volume level for sound effects",
                Float.class,
                1.0f,
                new NumberRangeValidator<Float>(0.0f, 1.0f));

      }

      public static class Environment {

        private Environment() {
          //...
        }

        public static final Cvar<Float> Volume = new ValidatableCvar<Float>(
                "Client.Sound.Environment.Volume",
                "Controls the volume level for environment effects",
                Float.class,
                1.0f,
                new NumberRangeValidator<Float>(0.0f, 1.0f));

      }

      public static class Voice {

        private Voice() {
          //...
        }

        public static final Cvar<Float> Volume = new ValidatableCvar<Float>(
                "Client.Sound.Voice.Volume",
                "Controls the volume level for NPC voice dialog",
                Float.class,
                1.0f,
                new NumberRangeValidator<Float>(0.0f, 1.0f));

      }

      public static class Music {

        private Music() {
          //...
        }

        public static final Cvar<Boolean> Enabled = new ValidatableCvar<Boolean>(
                "Client.Sound.Music.Enabled",
                "Controls whether or not music will play",
                Boolean.class,
                Boolean.TRUE);

        public static final Cvar<Float> Volume = new ValidatableCvar<Float>(
                "Client.Sound.Music.Volume",
                "Controls the volume level for music tracks",
                Float.class,
                1.0f,
                new NumberRangeValidator<Float>(0.0f, 1.0f));

      }
    }

    public static class Render {

      private Render() {
      }

      public static final Cvar<Byte> ShowFPS = new ValidatableCvar<Byte>(
              "Client.Render.ShowFPS",
              "Whether or not to draw the current FPS. " +
                      "0-Off, 1-Top Left, 2-Top Right, 3-Bottom Left, 4-Bottom Right",
              Byte.class,
              (byte) 0,
              new NumberRangeValidator<Byte>((byte) 0, (byte) 4));

    }
  }

}
