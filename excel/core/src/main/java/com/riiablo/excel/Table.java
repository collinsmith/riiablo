package com.riiablo.excel;

/**
 * @param <R> type of records stored in this table
 */
public abstract class Table<R> {
  public R get(String id) {
    throw new UnsupportedOperationException();
  }

  public R get(int id) {
    throw new UnsupportedOperationException();
  }

  public int index(String id) {
    throw new UnsupportedOperationException();
  }

  public int size() {
    throw new UnsupportedOperationException();
  }
}
