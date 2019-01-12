package gdx.diablo;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;

import gdx.diablo.command.Action;
import gdx.diablo.command.Command;
import gdx.diablo.command.CommandManager;
import gdx.diablo.command.Parameter;
import gdx.diablo.command.ParameterException;
import gdx.diablo.cvar.Cvar;
import gdx.diablo.serializer.SerializeException;
import gdx.diablo.serializer.StringSerializer;
import gdx.diablo.validator.ValidationException;

public class Commands {
  public static Collection<Throwable> addTo(CommandManager commandManager) {
    return addTo(commandManager, Commands.class, new ArrayList<Throwable>());
  }

  private static Collection<Throwable> addTo(CommandManager commandManager, Class<?> clazz, Collection<Throwable> throwables) {
    for (Field field : ClassReflection.getFields(clazz)) {
      if (Command.class.isAssignableFrom(field.getType())) {
        try {
          commandManager.add((Command) field.get(null));
        } catch (Throwable t) {
          throwables.add(t);
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(commandManager, subclass, throwables);
    }

    return throwables;
  }

  private Commands() {}

  public static final Command help = Command.builder()
      .alias("help").alias("?")
      .description("Displays this message")
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          Diablo.console.out.println("<> indicates required, [] indicates optional");
          for (Command cmd : Diablo.commands.getCommands()) {
            Diablo.console.out.println(cmd + " : " + cmd.getDescription());
          }
        }
      })
      .build();


  public static final Command clear = Command.builder()
      .alias("clear").alias("cls")
      .description("Clears the console output")
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          Diablo.console.clear();
        }
      })
      .build();

  public static final Command exit = Command.builder()
      .alias("exit")
      .description("Closes the game")
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          Gdx.app.exit();
        }
      })
      .build();

  public static final Command cvars = Command.builder()
      .alias("cvars")
      .description("Prints the descriptions of all cvars")
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          Collection<Cvar> cvars = Diablo.cvars.getCvars();
          for (Cvar cvar : cvars) {
            Diablo.console.out.format("%s \"%s\"; %s (Default: \"%s\")%n",
                cvar.getAlias(), cvar.get(), cvar.getDescription(), cvar.getDefault());
          }
        }
      })
      .build();

  public static final Command get = Command.builder()
      .alias("get")
      .description("Prints the value of the specified cvar")
      .params(Parameter.of(Cvar.class).suggester(CvarSuggester.INSTANCE))
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          String alias = instance.getArg(0);
          Cvar cvar = Diablo.cvars.get(alias);
          if (cvar == null) {
            throw new ParameterException("Failed to find cvar by alias: %s. For a list of cvars type \"%s\"", alias, cvars.getAlias());
          }

          Diablo.console.out.format("%s = %s%n", cvar.getAlias(), cvar.get());
        }
      })
      .build();

  public static final Command set = Command.builder()
      .alias("set")
      .description("Sets the value of the specified cvar")
      .params(
          Parameter.of(Cvar.class).suggester(CvarSuggester.INSTANCE),
          Parameter.of(String.class).suggester(CvarValueSuggester.INSTANCE))
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          String alias = instance.getArg(0);
          String value = instance.getArg(1);
          Cvar cvar = Diablo.cvars.get(alias);
          if (cvar == null) {
            throw new ParameterException("Failed to find cvar by alias: " + alias);
          }

          StringSerializer serializer = Diablo.cvars.getSerializer(cvar);
          try {
            cvar.set(value, serializer);
          } catch (SerializeException e) {
            throw new ParameterException("Invalid value specified: \"%s\". Expected type: %s", value, cvar.getType().getName());
          } catch (ValidationException e) {
            throw new ParameterException("Invalid value specified: \"%s\". %s", value, e.getMessage());
          }
        }
      })
      .build();

  public static final Command assets = Command.builder()
      .alias("assets")
      .description("Prints a list of all loaded assets")
      .action(new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          Array<String> assets = Diablo.assets.getAssetNames();
          for (String fileName : assets) {
            Diablo.console.out.println(fileName);
          }
        }
      })
      .build();

  public static final Command print = Command.builder()
      .alias("string")
      .description("Prints the specified string using its lookup index")
      .params(Parameter.of(Integer.class))
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          int index = NumberUtils.toInt(instance.getArg(0));
          Diablo.console.out.println(Diablo.string.lookup(index));
        }
      })
      .build();

  public static final Command glversion = Command.builder()
      .alias("gl.version")
      .description("Prints devices OpenGL version")
      .action(new Action() {
        @Override
        public void onExecuted(Command.Instance instance) {
          Diablo.console.out.println(Gdx.gl.glGetString(GL20.GL_VERSION));
        }
      })
      .build();
}
