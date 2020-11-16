package com.riiablo;

import android.support.annotation.NonNull;
import org.apache.commons.collections4.Trie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.LoggerRegistry;
import com.riiablo.serializer.StringSerializer;

public class GdxLoggerManager {
  private static final Logger log = LogManager.getLogger(GdxLoggerManager.class); {
    log.level(Level.TRACE);
  }

  private static final String ROOT = "<root>";

  private final Preferences PREFERENCES = Gdx.app.getPreferences("GdxLoggerManager");
  private final StringSerializer<Level> levelSerializer;
  private final LoggerRegistry logs;
  private boolean autosave;

  public GdxLoggerManager(LoggerRegistry logs) {
    this(logs, true);
  }

  public GdxLoggerManager(LoggerRegistry logs, boolean autosave) {
    this.logs = logs;
    this.autosave = autosave;
    levelSerializer = new StringSerializer<Level>() {
      @NonNull
      @Override
      public String serialize(@NonNull Level obj) {
        return obj.name();
      }

      @NonNull
      @Override
      public Level deserialize(@NonNull String str) {
        return Level.valueOf(str, GdxLoggerManager.this.logs.getLevel(LoggerRegistry.ROOT));
      }
    };
  }

  public boolean isAutosaving() {
    return autosave;
  }

  public void setAutosave(boolean b) {
    if (b != autosave) {
      autosave = b;
      if (b) saveAll();
    }
  }

  public String getDebugName(String name) {
    return name.equals(LoggerRegistry.ROOT)
        ? ROOT
        : name;
  }

  public Logger getLogger(String name) {
    final Logger logger = logs.getLogger(name);
    return null;
  }

  public Level getLevel(String name) {
    return logs.getLevel(name);
  }

  public void setLevel(String name, Level level) {
    logs.setLevel(name, level);
    if (autosave) save(name);
  }

  public Logger getRootLogger() {
    return logs.getRoot();
  }

  public Trie<String, Level> getContexts() {
    return logs.getContexts();
  }

  public Trie<String, Logger> getLoggers() {
    return logs.getLoggers();
  }

  public void save(String name) {
    Level value = logs.getLevel(name);
    String serialization = levelSerializer.serialize(value);
    PREFERENCES.putString(name, serialization);
    PREFERENCES.flush();
    log.debug("{} saved as \"{}\" (raw: \"{}\")", getDebugName(name), value, serialization);
  }

  public Level load(String name) {
    String serialization = PREFERENCES.getString(name, null);
    if (serialization == null) return logs.getLevel(name);
    Level deserialization = levelSerializer.deserialize(serialization);
    log.debug("{} loaded as \"{}\" (raw: \"{}\")", getDebugName(name), deserialization, serialization);
    logs.setLevel(name, deserialization);
    return deserialization;
  }

  public void saveAll() {
    log.trace("Saving contexts...");
    for (String context : logs.getContexts().keySet()) {
      try {
        save(context);
      } catch (Throwable t) {
        log.warn("Failed to save {}", getDebugName(context), t);
      }
    }
  }

  public void loadAll() {
    log.trace("Loading contexts...");
    for (String context : PREFERENCES.get().keySet()) {
      try {
        load(context);
      } catch (Throwable t) {
        log.warn("Failed to load {}", getDebugName(context), t);
      }
    }
  }
}
