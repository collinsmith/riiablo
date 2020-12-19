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

  protected Injector<R, ?> injector;
  protected Parser<R> parser;

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
  protected abstract Parser<R> newParser(ParserInput parser);
  protected abstract Serializer<R> newSerializer();

  protected Injector<R, ?> newInjector() {
    return null;
  }

  public Class<R> recordClass() {
    return recordClass;
  }

  @Override
  public Iterator<R> iterator() {
    return ordered.iterator();
  }

  /**
   * Called when this table has been constructed and initialized. Used to
   * set-up table-specific configurations.
   */
  protected void initialize() {}

  /**
   * Assigns a record to a specified id. Implementations can override this and
   * call this super function to re-map indexes.
   */
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

  protected R inject(R record) {
    if (injector == null) injector = newInjector();
    if (injector != null) return injector.inject(null, record);
    return record;
  }

  protected Parser<R> parser() {
    return parser;
  }

  /**
   * Initializes this table for loading records via a
   * {@link ParserInput parser}.
   */
  protected final void initialize(ParserInput in) {
    if (parser != null) throw new IllegalStateException("parser already set");
    this.parser = newParser(in).parseFields();
    initialize();
  }

  public R get(int id) {
    R record = records.get(id);
    if (record == null && parser != null) {
      record = parser.parseRecord(id, newRecord());
      record = inject(record);
      records.put(id, record);
    }

    return record;
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
