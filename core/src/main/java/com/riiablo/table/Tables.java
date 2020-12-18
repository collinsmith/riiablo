package com.riiablo.table;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class Tables {
  private static final Logger log = LogManager.getLogger(Tables.class);

  private static final boolean USE_TSV_ONLY = !true;

  private Tables() {}

  public static <R, T extends Table<R>>
  T load(T table, FileHandle tsv) {
    return load(table, tsv, null);
  }

  public static <R, T extends Table<R>>
  T load(T table, FileHandle tsv, FileHandle bin) {
    if (!USE_TSV_ONLY && bin != null && bin.exists()) {
      try {
        return loadBin(table, bin);
      } catch (Throwable t) {
        log.error("Failed to load {}, defaulting to tsv", bin, t);
        return loadTsv(table, tsv);
      }
    } else {
      return loadTsv(table, tsv);
    }
  }

  static <R, T extends Table<R>>
  T loadTsv(T table, FileHandle tsv) {
    log.info("Loading {}", tsv);
    TsvParser parser = TsvParser.parse(tsv.readBytes());
    return loadTsv(table, parser);
  }

  static <R, T extends Table<R>>
  T loadTsv(T table, TsvParser parser) {
    parser.primaryKey(table.primaryKey());
    table.initialize(parser);
    return table;
  }

  static <R, T extends Table<R>>
  T loadBin(T table, FileHandle bin) {
    log.info("Loading {}", bin);
    //...
    table.initialize();
    return table;
  }
}
