package com.riiablo.excel;

import android.support.annotation.CallSuper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.io.ByteInput;
import com.riiablo.util.ClassUtils;

public abstract class Excel<T extends Excel.Entry, U extends Serializer<T>> implements Iterable<T> {
  public static final String TAG = "Excel";

  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_COLS    = DEBUG && !true;
  private static final boolean DEBUG_COL_IDS = DEBUG && !true;
  private static final boolean DEBUG_IGNORED = DEBUG && true;
  private static final boolean DEBUG_ENTRIES = DEBUG && true;
  private static final boolean DEBUG_INDEXES = DEBUG && true;
  private static final boolean DEBUG_KEY     = DEBUG && true;
  private static final boolean DEBUG_BIN     = DEBUG && true;
  private static final boolean DEBUG_TIME    = DEBUG && true;

  private static final boolean FORCE_PRIMARY_KEY = !true;
  private static final boolean FORCE_TXT = !true;

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Index {}

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Bin {}

  public static class Entry {
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Column {
      /** Used to index format */
      int     startIndex()  default 0;
      /** Used to index format */
      int     endIndex()    default 0;
      /** Format of column name, if not set, then field name is used */
      String  format()      default "";
      /** Used to index format not non-numerical indexes */
      String  values()[]    default {};
      /** Sets index of column of value (used for cases like weapons.txt where one column has no name) */
      int     columnIndex() default -1;
      /** Whether or not to read/write value in bin codec */
      boolean bin()         default true;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Key {}
  }

  private static final ObjectIntMap EMPTY_MAP = new ObjectIntMap();

  @SuppressWarnings("unchecked")
  public static <T> ObjectIntMap<T> emptyMap() {
    return (ObjectIntMap<T>) EMPTY_MAP;
  }

  public static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T load(T excel, FileHandle txt) {
    return load(excel, txt, null);
  }

  public static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T load(T excel, FileHandle txt, FileHandle bin) {
    long start = System.currentTimeMillis();

    FileHandle handle;
    if (!FORCE_TXT && bin != null && bin.exists()) {
      if (DEBUG_BIN) Gdx.app.debug(TAG, "Loading bin " + bin);
      loadBin(excel, bin);
      handle = bin;
    } else {
      if (DEBUG_BIN) Gdx.app.debug(TAG, "Loading txt " + txt);
      loadTxt(excel, txt);
      handle = txt;
    }

    long end = System.currentTimeMillis();
    if (DEBUG_TIME) Gdx.app.debug(TAG, "Loaded " + handle + " in " + (end - start) + "ms");
    return excel;
  }

  private static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadTxt(T excel, FileHandle handle) {
    TxtParser parser = null;
    try {
      parser = TxtParser.loadFromFile(handle);
      return loadTxt(excel, parser);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load excel: " + handle, t);
    } finally {
      IOUtils.closeQuietly(parser);
    }
  }


  private static <E extends Entry, T extends Excel<E, ?>>
  T loadTxt(T excel, TxtParser parser) throws IllegalAccessException {
    final Class<E> entryClass = excel.getEntryClass();
    final boolean index = ClassUtils.hasAnnotation(entryClass, Index.class);

    Field primaryKey = null, firstKey = null;
    ObjectMap<Field, int[]> columns = new ObjectMap<>();
    String[] TMP = new String[1];
    for (Field field : entryClass.getFields()) {
      Entry.Column column = field.getAnnotation(Entry.Column.class);
      if (column == null) continue; // TODO: DEBUG MESSAGE WARNING

      Entry.Key key = field.getAnnotation(Entry.Key.class);
      if (key != null) {
        if (index) {
          Gdx.app.error(TAG, "primary key set in class annotated with " + com.riiablo.codec.excel.Excel.Index.class);
        } else if (primaryKey != null) {
          boolean primaryDeclared = ClassUtils.isDeclaredField(entryClass, primaryKey);
          boolean fieldDeclared = ClassUtils.isDeclaredField(entryClass, field);
          if (primaryDeclared != fieldDeclared) {
            if (fieldDeclared) {
              if (DEBUG_KEY) Gdx.app.debug(TAG, "primary key " + primaryKey.getName() + " -> " + field.getName());
              primaryKey = field;
            }
          } else {
            Gdx.app.error(TAG, "more than one primary key for " + entryClass + " " + primaryKey.getName() + " and " + field.getName());
          }
        } else {
          primaryKey = field;
        }
      }

      if (firstKey == null) firstKey = field;

      final String format      = column.format();
      final String values[]    = column.values();
      final int    startIndex  = column.startIndex();
      final int    endIndex    = column.endIndex();
      final int    columnIndex = column.columnIndex();
      if (columnIndex >= 0) {
        columns.put(field, new int[] { columnIndex });
      } else if (format.isEmpty()) {
        final String fieldName = field.getName();
        if (values.length > 0) {
          String[] columnNames = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            String name = values[i];
            if (DEBUG_COLS) Gdx.app.debug(TAG, name);
            columnNames[i] = name;
          }

          columns.put(field, parser.getColumnId(columnNames));
        } else if (startIndex == 0 && endIndex == 0) {
          if (DEBUG_COLS) Gdx.app.debug(TAG, fieldName);
          TMP[0] = fieldName;
          columns.put(field, parser.getColumnId(TMP));
        } else {
          String[] columnNames = new String[endIndex - startIndex];
          for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            String name = fieldName + i;
            if (DEBUG_COLS) Gdx.app.debug(TAG, name);
            columnNames[j] = name;
          }

          columns.put(field, parser.getColumnId(columnNames));
        }
      } else {
        if (startIndex == 0 && endIndex == 0) {
          TMP[0] = format;
          columns.put(field, parser.getColumnId(TMP));
        } else {
          String[] columnNames = new String[endIndex - startIndex];
          if (values.length == 0) {
            for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
              String name = String.format(format, i);
              if (DEBUG_COLS) Gdx.app.debug(TAG, name);
              columnNames[j] = name;
            }
          } else {
            for (int i = 0; i < values.length; i++) {
              String name = String.format(format, values[i]);
              if (DEBUG_COLS) Gdx.app.debug(TAG, name);
              columnNames[i] = name;
            }
          }

          columns.put(field, parser.getColumnId(columnNames));
        }
      }
    }

    if (primaryKey == null && !index) {
      if (FORCE_PRIMARY_KEY) {
        throw new IllegalStateException(entryClass + " does not have a " + Entry.Key.class + " set!");
      } else {
        primaryKey = firstKey;
        Gdx.app.error(TAG, entryClass + " does not have a " + Entry.Key.class + " set! Using " + firstKey.getName());
      }
    }

    if (DEBUG_COL_IDS) {
      for (ObjectMap.Entry<Field, int[]> entry : columns.entries()) {
        Gdx.app.debug(TAG, entry.key.getName() + ": " + Arrays.toString(entry.value));
      }
    }

    final int primaryKeyCol = index ? -1 : columns.get(primaryKey)[0];
    final Class primaryKeyType = index ? null : primaryKey.getType();
    for (int i = excel.offset(); parser.nextLine() != null; i++) {
      E entry = excel.newEntry();
      String name = index ? null : parser.getString(primaryKeyCol);
      for (ObjectMap.Entry<Field, int[]> row : columns.entries()) {
        Field field = row.key;
        int[] columnIds = row.value;
        Class type = field.getType();
        assert type.isArray() || columnIds.length == 1 : "field should only correspond to 1 column: " + field.getName() + ", " + columnIds.length + " columns (is it supposed to be an array?)";
        if (type == String.class) {
          String value = parser.getString(columnIds[0]);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), value));
        } else if (type == String[].class) {
          String[] value = parser.getString(columnIds);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), Arrays.toString(value)));
        } else if (type == byte.class) {
          byte value = parser.getByte(columnIds[0]);
          field.setByte(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), value));
        } else if (type == byte[].class) {
          byte[] value = parser.getByte(columnIds);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), Arrays.toString(value)));
        } else if (type == short.class) {
          short value = parser.getShort(columnIds[0]);
          field.setShort(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), value));
        } else if (type == short[].class) {
          short[] value = parser.getShort(columnIds);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), Arrays.toString(value)));
        } else if (type == int.class) {
          int value = parser.getInt(columnIds[0]);
          field.setInt(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), value));
        } else if (type == int[].class) {
          int[] value = parser.getInt(columnIds);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), Arrays.toString(value)));
        } else if (type == long.class) {
          long value = parser.getLong(columnIds[0]);
          field.setLong(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), value));
        } else if (type == long[].class) {
          long[] value = parser.getLong(columnIds);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), Arrays.toString(value)));
        } else if (type == boolean.class) {
          boolean value = parser.getBoolean(columnIds[0]);
          field.setBoolean(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), value));
        } else if (type == boolean[].class) {
          boolean[] value = parser.getBoolean(columnIds);
          field.set(entry, value);
          if (DEBUG_ENTRIES) Gdx.app.debug(TAG, String.format("Entry[%d](%s).%s=%s", i, name, field.getName(), Arrays.toString(value)));
        } else {
          throw new UnsupportedOperationException("No support for " + type + " fields");
        }
      }

      putIndex(primaryKey, primaryKeyType, i++, index, excel, entry);
    }

    return excel;
  }

  private static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadBin(T excel, FileHandle handle) {
    final Class<E> entryClass = excel.getEntryClass();
    final boolean index = ClassUtils.hasAnnotation(entryClass, Index.class);
    final S serializer = excel.newSerializer();

    try {
      Field primaryKey = ClassUtils.findField(entryClass, Entry.Key.class);
      if (primaryKey == null && !index) {
        if (FORCE_PRIMARY_KEY) {
          throw new IllegalStateException(entryClass + " does not have a " + Entry.Key.class + " set!");
        } else {
          primaryKey = entryClass.getFields()[0];
          Gdx.app.error(TAG, entryClass + " does not have a " + Entry.Key.class + " set! Using " + primaryKey.getName());
        }
      }

      Class primaryKeyType = index ? null : primaryKey.getType();

      ByteInput in = ByteInput.wrap(handle.readBytes());
      int size = in.readSafe32u();
      Gdx.app.log(TAG, "reading " + size + " entries...");
      for (int i = 0, j = excel.offset(); i < size; i++, j++) {
        E entry = excel.newEntry();
        serializer.readBin(entry, in);
        putIndex(primaryKey, primaryKeyType, j, index, excel, entry);
      }

      return excel;
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load excel: " + handle, t);
    }
  }

  private static <E extends Entry, T extends Excel<E, ?>>
  void putIndex(Field primaryKey, Class primaryKeyType, int i, boolean indexed, T excel, E entry) throws IllegalAccessException {
    if (indexed) {
      excel.put(i, entry);
    } else if (primaryKeyType == int.class) {
      int id = primaryKey.getInt(entry);
      excel.put(id, entry);
    } else if (primaryKeyType == String.class) {
      String id = (String) primaryKey.get(entry);
      excel.put(i, entry);

      if (excel.STRING_TO_ID == EMPTY_MAP) excel.STRING_TO_ID = new ObjectIntMap<>();
      if (!excel.STRING_TO_ID.containsKey(id)) excel.STRING_TO_ID.put(id, i);
    }
  }

  protected ObjectIntMap<String> STRING_TO_ID = emptyMap();
  protected IntMap<T> entries = new IntMap<>();
  protected Class<T> entryClass;

  protected Excel(Class<T> entryClass) {
    this.entryClass = entryClass;
  }

  @CallSuper
  protected void put(int id, T value) {
  }

  protected int offset() {
    return 0;
  }

  protected void init() {}

  public T get(String id) {
    return get(index(id));
  }

  public T get(int id) {
    T value = entries.get(id);
    if (DEBUG_INDEXES) Gdx.app.debug(TAG, id + " = " + value);
    return value;
  }

  public int index(String id) {
    return STRING_TO_ID.get(id, -1);
  }

  public int size() {
    return entries.size;
  }

  public Class<T> getEntryClass() {
    return entryClass;
  }

  public abstract T newEntry();

  public abstract U newSerializer();

  @Override
  public Iterator<T> iterator() {
    return entries.values().iterator();
  }
}
