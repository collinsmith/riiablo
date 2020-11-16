package com.riiablo.suggester;

import java.util.Collection;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import com.badlogic.gdx.Input;

import com.riiablo.console.Console;
import com.riiablo.console.ConsoleUtils;
import com.riiablo.util.StringUtils;

public enum KeyValueSuggester implements Console.SuggestionProvider {
  INSTANCE;

  private static final Trie<String, Integer> KEYS = new PatriciaTrie<>();
  static {
    for (int i = 0; i < 256; i++) {
      String key = Input.Keys.toString(i);
      if (key == null) continue;
      KEYS.put(key.toLowerCase(), i);
    }
  }

  public int get(String keyname) {
    if (keyname == null) return -1;
    Integer keycode = KEYS.get(keyname.toLowerCase());
    return keycode != null ? keycode : -1;
  }

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int targetArg) {
    String arg = targetArg == args.length ? "" : args[targetArg];
    Collection<String> suggestions = KEYS.prefixMap(arg.toLowerCase()).keySet();
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
