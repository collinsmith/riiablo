package com.riiablo.suggester;

import java.util.Set;
import java.util.SortedMap;

import com.riiablo.Riiablo;
import com.riiablo.console.Console;
import com.riiablo.console.ConsoleUtils;
import com.riiablo.util.StringUtils;

public enum LoggerSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int targetArg) {
    String arg = targetArg == args.length ? "" : args[targetArg];
    SortedMap<String, ?> keys = Riiablo.logs.prefixMap(arg);
    switch (keys.size()) {
      case 0:
        return 0;
      case 1:
        String alias = keys.firstKey();
        console.in.append(alias, arg.length());
        break;
      default:
        Set<String> aliases = keys.keySet();
        String commonPrefix = StringUtils.commonPrefix(aliases);
        if (commonPrefix.length() > arg.length()) {
          console.in.append(commonPrefix, arg.length());
        } else {
          ConsoleUtils.printList(console, aliases, 0, 0);
        }

        return aliases.size();
    }
    return 0;
  }
}
