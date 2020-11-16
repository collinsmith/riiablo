package com.riiablo.suggester;

import java.util.Set;
import java.util.SortedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import com.riiablo.console.Console;
import com.riiablo.console.ConsoleUtils;
import com.riiablo.util.StringUtils;

public enum LoggerSuggester implements Console.SuggestionProvider {
  INSTANCE;

  // TODO: generate pre scanned metadata
  // https://github.com/ronmamo/reflections/blob/gh-pages/UseCases.md#collect-pre-scanned-metadata
  private static final Trie<String, String> riiabloClasspath;
  static {
    riiabloClasspath = new PatriciaTrie<>();
    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
      final String RIIABLO_PACKAGE = "com.riiablo";
      ConfigurationBuilder reflectionsConfig = new ConfigurationBuilder()
          .setUrls(ClasspathHelper.forPackage(RIIABLO_PACKAGE))
          .setScanners(
              new SubTypesScanner(false))
          .filterInputsBy(new FilterBuilder().includePackage(RIIABLO_PACKAGE));
      Reflections reflections = new Reflections(reflectionsConfig);
      for (String str : reflections.getAllTypes()) {
        str = org.apache.commons.lang3.StringUtils
            .substringBefore(str, ClassUtils.INNER_CLASS_SEPARATOR);
        riiabloClasspath.put(str, org.apache.commons.lang3.StringUtils.EMPTY);
      }
    }
  }

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int targetArg) {
    String arg = targetArg == args.length ? "" : args[targetArg];
    SortedMap<String, ?> keys = riiabloClasspath.prefixMap(arg);
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
