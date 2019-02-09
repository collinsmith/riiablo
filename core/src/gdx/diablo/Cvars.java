package gdx.diablo;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import gdx.diablo.cvar.Cvar;
import gdx.diablo.cvar.CvarManager;
import gdx.diablo.cvar.GdxFileSuggester;
import gdx.diablo.serializer.LocaleStringSerializer;
import gdx.diablo.validator.GdxFileValidator;
import gdx.diablo.validator.NonNullSubclassValidator;
import gdx.diablo.validator.NumberRangeValidator;
import gdx.diablo.validator.Validator;

public class Cvars {
  public static Collection<Throwable> addTo(CvarManager cvarManager) {
    return addTo(cvarManager, Cvars.class, new ArrayList<Throwable>());
  }

  private static Collection<Throwable> addTo(CvarManager cvarManager, Class<?> clazz, Collection<Throwable> throwables) {
    for (Field field : ClassReflection.getFields(clazz)) {
      if (Cvar.class.isAssignableFrom(field.getType())) {
        try {
          cvarManager.add((Cvar) field.get(null));
        } catch (Throwable t) {
          throwables.add(t);
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(cvarManager, subclass, throwables);
    }

    return throwables;
  }

  private Cvars() {}

  public interface Client {
    Cvar<Locale> Locale = Cvar.builder(Locale.class)
        .alias("Client.Locale")
        .description("Locale of the game client")
        .defaultValue(java.util.Locale.getDefault())
        .validator(new NonNullSubclassValidator<>(Locale.class))
        .serializer(LocaleStringSerializer.INSTANCE)
        .build();

    Cvar<Boolean> Windowed = Cvar.builder(Boolean.class)
        .alias("Client.Windowed")
        .description(
            "Whether or not the client is in windowed mode. Note: This cvar is ignored when the " +
            "client is started with the -windowed option")
        .defaultValue(Boolean.FALSE)
        .validator(Validator.ACCEPT_NON_NULL)
        .build();

    Cvar<String> Realm = Cvar.builder(String.class)
        .alias("Client.Realm")
        .description("Realm to connect to.")
        .defaultValue("hydra")
        .build();

    interface Console {
      Cvar<String> Font = Cvar.builder(String.class)
          .alias("Client.Console.Font")
          .description("Font file for the console")
          .defaultValue("default.fnt")
          .validator(new GdxFileValidator(GdxFileHandleResolvers.INTERNAL, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
              return name.endsWith(".fnt");
            }
          }))
          .suggestions(new GdxFileSuggester(GdxFileHandleResolvers.INTERNAL, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
              return dir.isDirectory() || name.endsWith(".fnt");
            }
          }))
          .build();

      Cvar<Float> Height = Cvar.builder(Float.class)
          .alias("Client.Console.Height")
          .description("Height of the console in percent of screen height")
          .defaultValue(0.5f)
          .validator(NumberRangeValidator.of(Float.class, 0.25f, 1.0f))
          .build();

      interface Color {
        Cvar<Float> r = Cvar.builder(Float.class)
            .alias("Client.Console.Color.r")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();

        Cvar<Float> g = Cvar.builder(Float.class)
            .alias("Client.Console.Color.g")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();

        Cvar<Float> b = Cvar.builder(Float.class)
            .alias("Client.Console.Color.b")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();

        Cvar<Float> a = Cvar.builder(Float.class)
            .alias("Client.Console.Color.a")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();
      }
    }

    interface Display {
      Cvar<Byte> ShowFPS = Cvar.builder(Byte.class)
          .alias("Client.Display.ShowFPS")
          .description(
              "Whether or not to draw the current FPS. " +
              "0=Off, 1=Top Left, 2=Top Right, 3=Bottom Left, 4=Bottom Right")
          .defaultValue((byte) 0)
          .validator(NumberRangeValidator.of(Byte.class, (byte) 0, (byte) 4))
          .build();

      Cvar<Short> BackgroundFPSLimit = Cvar.builder(Short.class)
          .alias("Client.Display.BackgroundFPSLimit")
          .description(
              "Limits the FPS of the application when running in the background. " +
              "-1=Won't Render, 0=Unlimited")
          .defaultValue((short) 10)
          .validator(NumberRangeValidator.of(Short.class, (short) -1, null))
          .build();

      Cvar<Short> ForegroundFPSLimit = Cvar.builder(Short.class)
          .alias("Client.Display.ForegroundFPSLimit")
          .description(
              "Limits the FPS of the application when running in the foreground. " +
              "0-Unlimited")
          .defaultValue((short) 0)
          .validator(NumberRangeValidator.of(Short.class, (short) 0, null))
          .build();

      Cvar<Float> Gamma = Cvar.builder(Float.class)
          .alias("Client.Display.Gamma")
          .description(
              "Controls the gamma correction applied to the screen.")
          .defaultValue(1.0f)
          .validator(NumberRangeValidator.of(Float.class, 0.5f, 4.0f))
          .build();
    }

    interface Input {
      Cvar<Boolean> Vibration = Cvar.builder(Boolean.class)
          .alias("Client.Input.Vibration")
          .description("Whether or not haptic feedback is enabled")
          .defaultValue(Boolean.TRUE)
          .validator(Validator.ACCEPT_NON_NULL)
          .build();
    }


    interface Sound {
      Cvar<Boolean> Enabled = Cvar.builder(Boolean.class)
          .alias("Client.Sounds.Enabled")
          .description("Whether or not sound is enabled")
          .defaultValue(Boolean.TRUE)
          .validator(Validator.ACCEPT_NON_NULL)
          .build();

      interface Music {
        Cvar<Boolean> Enabled = Cvar.builder(Boolean.class)
            .alias("Client.Sounds.Music.Enabled")
            .description("Whether or not music is enabled")
            .defaultValue(Boolean.TRUE)
            .validator(Validator.ACCEPT_NON_NULL)
            .build();
        Cvar<Float> Volume = Cvar.builder(Float.class)
            .alias("Client.Sounds.Music.Volume")
            .description("Whether or not music is enabled")
            .defaultValue(0.50f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();
      }

      interface Effects {
        Cvar<Boolean> Enabled = Cvar.builder(Boolean.class)
            .alias("Client.Sounds.Effects.Enabled")
            .description("Whether or not sound effects are enabled")
            .defaultValue(Boolean.TRUE)
            .validator(Validator.ACCEPT_NON_NULL)
            .build();

        Cvar<Float> Volume = Cvar.builder(Float.class)
            .alias("Client.Sounds.Effects.Volume")
            .description("Whether or not sound effects are enabled")
            .defaultValue(0.50f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();
      }
    }
  }

}
