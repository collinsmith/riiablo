package com.riiablo.table;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class Tables {
  private static final Logger log = LogManager.getLogger(Tables.class);

  private Tables() {}

  static <R, T extends Table<R>>
  T loadTsv(T table, TsvParser parser) {
    parser.primaryKey(table.primaryKey());
    return table;
  }
}
