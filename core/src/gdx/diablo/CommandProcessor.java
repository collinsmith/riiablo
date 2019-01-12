package gdx.diablo;

import com.badlogic.gdx.Gdx;

import java.util.Set;
import java.util.SortedMap;

import gdx.diablo.command.Command;
import gdx.diablo.command.Parameter;
import gdx.diablo.command.ParameterException;
import gdx.diablo.console.Console;
import gdx.diablo.console.ConsoleUtils;
import gdx.diablo.serializer.SerializeException;
import gdx.diablo.util.StringUtils;
import gdx.diablo.validator.ValidationException;

public enum CommandProcessor implements Console.Processor, Console.SuggestionProvider {
  INSTANCE;

  private static final String TAG = "CommandProcessor";

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int arg) {
    switch (args.length) {
      case 0:
        return 0;
      case 1: // Command /w no args
        String arg0 = args[0];
        SortedMap<String, Command> commands = Diablo.commands.prefixMap(arg0);
        switch (commands.size()) {
          case 0:
            return 0;
          case 1:
            // check if current command is completed (' ' after it)
            char ch = buffer.charAt(buffer.length() - 1);
            if (ch == ' ') {
              // break into default (i.e., the command alias is complete, handle args)
              break;
            }

            String alias = commands.firstKey();
            console.in.append(alias, arg0.length());
            console.in.append(' ');
            return 1; // suggestion provided
          default:
            Set<String> aliases = commands.keySet();
            String commonPrefix = StringUtils.commonPrefix(aliases);
            if (commonPrefix.length() > arg0.length()) {
              console.in.append(commonPrefix, arg0.length());
            } else {
              ConsoleUtils.printList(console, aliases, 6, 12);
            }

            return commands.size();
        }
      default: // Command /w args (suggest args)
        Command command = Diablo.commands.get(args[0]);
        if (command == null) {
          return 0;
        }

        // args are offset +1, so current arg is:
        int targetParam = args.length - 2;

        // check if current param is completed (' ' after it), in which case, our target is
        // really the next param
        char ch = buffer.charAt(buffer.length() - 1);
        if (ch == ' ' && command.hasParam(targetParam + 1)) {
          targetParam += 1;
        }

        Parameter param = command.getParam(targetParam);
        if (!param.canSuggest()) {
          return 0;
        }

        // apply args[] offset (targetParam is represented by args[targetParam + 1])
        int suggestions = param.suggest(console, buffer, args, targetParam + 1);
        if (suggestions == 1) {
          console.in.append(' ');
        }

        return suggestions;
    }
  }

  @Override
  public boolean process(Console console, String buffer) {
    String[] args = StringUtils.parseArgs(buffer);
    Command cmd = Diablo.commands.get(args[0]);
    if (cmd == null) {
      return false;
    }

    try {
      cmd.newInstance(args).execute();
    } catch (SerializeException|ValidationException|ParameterException e) {
      String message = e.getMessage();
      if (message != null) {
        console.out.println(message);
      }

      //Gdx.app.error(TAG, e.getClass().getName() + ": " + e.getMessage(), e);
    } catch (Exception e) {
      Gdx.app.error(TAG, e.getClass().getName() + ": " + e.getMessage(), e);
    }

    return true;
  }

  @Override
  public void onUnprocessed(Console console, String buffer) {
    console.out.format("Unrecognized command \"%s\". To see available commands, type \"%s\"%n", buffer, Commands.help.getAlias());
  }
}
