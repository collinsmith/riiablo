package com.riiablo.table;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Stores a table of records.
 *
 * @param <R> record type
 */
public abstract class Table<R> implements Iterable<R> {
  public static final int DEFAULT_INITIAL_CAPACITY = 53;
  public static final float DEFAULT_LOAD_FACTOR = 0.8f;

  protected final Class<R> recordClass;
  protected ObjectIntMap<String> lookup;
  protected IntMap<R> records;
  protected Array<R> ordered;

  protected Table(Class<R> recordClass) {
    this(recordClass, DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  protected Table(Class<R> recordClass, int initialCapacity) {
    this(recordClass, initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  protected Table(Class<R> recordClass, int initialCapacity, float loadFactor) {
    this.recordClass = recordClass;
    records = new IntMap<>(initialCapacity, loadFactor);
    ordered = new Array<>(true, (int) (initialCapacity * loadFactor), recordClass);
    lookup = null;
  }

  protected abstract R newRecord();
  protected abstract Serializer<R> newSerializer();

  public Class<R> recordClass() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<R> iterator() {
    return ordered.iterator();
  }

  protected void initialize() {}

  protected void put(int id, R record) {
    records.put(id, record);
    ordered.add(record);
  }

  protected int offset() {
    return 0;
  }

  protected boolean indexed() {
    return false;
  }

  protected String primaryKey() {
    return null;
  }

  public R get(int id) {
    return records.get(id);
  }

  public int index(String id) {
    return lookup == null ? -1 : lookup.get(id, -1);
  }

  public R get(String id) {
    return lookup == null ? null : get(lookup.get(id, -1));
  }

  public int size() {
    return records.size;
  }
}
