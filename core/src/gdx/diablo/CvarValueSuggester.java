package gdx.diablo;

import java.util.Collection;

import gdx.diablo.command.ParameterException;
import gdx.diablo.console.Console;
import gdx.diablo.console.ConsoleUtils;
import gdx.diablo.cvar.Cvar;
import gdx.diablo.util.StringUtils;

public enum CvarValueSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int targetArg) {
    String alias = args[targetArg - 1];
    Cvar cvar = Diablo.cvars.get(alias);
    if (cvar == null) {
      throw new ParameterException("A parameter of type %s must precede a parameter using CvarValueSuggester", Cvar.class.getName());
    }

    String arg = targetArg == args.length ? "" : args[targetArg];
    @SuppressWarnings("unchecked") Collection<String> suggestions = cvar.suggest(arg);
    switch (suggestions.size()) {
      case 0:
        return 0;
      case 1:
        String suggestion = suggestions.iterator().next();
        console.in.append(suggestion, arg.length());
        return 1;
      default:
        String commonPrefix = StringUtils.commonPrefix(suggestions);
        if (commonPrefix.length() > arg.length()) {
          console.in.append(commonPrefix, arg.length());
        } else {
          ConsoleUtils.printList(console, suggestions, 6, 20);
        }

        return suggestions.size();
    }
  }
}
