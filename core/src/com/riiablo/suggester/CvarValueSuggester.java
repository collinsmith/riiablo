package com.riiablo.suggester;

import java.util.Collection;

import com.riiablo.Riiablo;
import com.riiablo.command.ParameterException;
import com.riiablo.console.Console;
import com.riiablo.console.ConsoleUtils;
import com.riiablo.cvar.Cvar;
import com.riiablo.util.StringUtils;

public enum CvarValueSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int targetArg) {
    String alias = args[targetArg - 1];
    Cvar cvar = Riiablo.cvars.get(alias);
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
