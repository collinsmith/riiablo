package gdx.diablo;

import java.util.Set;
import java.util.SortedMap;

import gdx.diablo.console.Console;
import gdx.diablo.console.ConsoleUtils;
import gdx.diablo.cvar.Cvar;
import gdx.diablo.util.StringUtils;

public enum CvarSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int targetArg) {
    String arg = targetArg == args.length ? "" : args[targetArg];
    SortedMap<String, Cvar> cvars = Diablo.cvars.prefixMap(arg);
    switch (cvars.size()) {
      case 0:
        return 0;
      case 1:
        String alias = cvars.firstKey();
        console.in.append(alias, arg.length());
        return 1;
      default:
        Set<String> aliases = cvars.keySet();
        String commonPrefix = StringUtils.commonPrefix(aliases);
        if (commonPrefix.length() > arg.length()) {
          console.in.append(commonPrefix, arg.length());
        } else {
          ConsoleUtils.printList(console, aliases, 4, 36);
        }

        return aliases.size();
    }
  }
}
