package com.riiablo.excel;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * @param <R> type of records stored in this table
 */
public abstract class Table<R> implements Iterable<R> {
  protected final Class<R> recordClass;
  protected ObjectIntMap<String> lookup;
  protected IntMap<R> records;
  protected Array<R> ordered;

  protected Table(Class<R> recordClass) {
    this(recordClass, 53);
  }

  protected Table(Class<R> recordClass, int initialCapacity) {
    this(recordClass, initialCapacity, 0.8f);
  }

  protected Table(Class<R> recordClass, int initialCapacity, float loadFactor) {
    this.recordClass = recordClass;
    records = new IntMap<>(initialCapacity, loadFactor);
    ordered = new Array<>(true, (int) (initialCapacity * loadFactor), recordClass);
    lookup = null;
  }

  @SuppressWarnings("unchecked")
  public Class<? extends Table<R>> tableClass() {
    return (Class <? extends Table<R>>) getClass();
  }

  public Class<R> recordClass() {
    return recordClass;
  }

  protected void put(int id, R record) {
    records.put(id, record);
    ordered.add(record);
  }

  public R get(int id) {
    return records.get(id);
  }

  public int index(String id) {
    return lookup == null ? -1 : lookup.get(id, -1);
  }

  public R get(String id) {
    return get(index(id));
  }

  public int size() {
    return records.size;
  }

  protected int offset() {
    return 0;
  }

  protected String primaryKey() {
    return null;
  }

  protected void init() {}

  @Override
  public Iterator<R> iterator() {
    return ordered.iterator();
  }
}
