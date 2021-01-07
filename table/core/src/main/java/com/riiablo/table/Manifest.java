package com.riiablo.table;

/**
 * Defines behaviors necessary to manage a collection of tables.
 */
public interface Manifest {
  <R> Table<R> inject(Table<R> table, R record);
}
