package com.riiablo.excel2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.util.ClassUtils;

/**
 * Root class of an excel table.
 */
public abstract class Excel<
    E extends Excel.Entry,
    S extends Serializer<E>
>
    implements Iterable<E>
{
  private static final Logger log = LogManager.getLogger(Excel.class);

  /** Forces excels to either have a {@link PrimaryKey} set or be {@link Indexed} */
  private static final boolean FORCE_PRIMARY_KEY = !true;

  private static final ObjectIntMap EMPTY_OBJECT_INT_MAP = new ObjectIntMap();

  @SuppressWarnings("unchecked") // doesn't store anything
  static <T> ObjectIntMap<T> emptyMap() {
    return (ObjectIntMap<T>) EMPTY_OBJECT_INT_MAP;
  }

  /**
   * Root class of an excel entry.
   */
  public static abstract class Entry {
    /**
     * Tags a specified field as a column within the excel table.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Column {
      /**
       * Start index of {@link #format()} (inclusive)
       */
      int startIndex() default 0;

      /**
       * End index of {@link #format()} (exclusive)
       */
      int endIndex() default 0;

      /**
       * String format of column name, {@code ""} to use field name
       * <p>
       * <p>Examples:
       * <ul>
       * <li>{@code "class"}
       * <li>{@code "Transform Color"}
       * <li>{@code "Level%s"}
       * <li>{@code "Skill %d"}
       */
      String format() default "";

      /**
       * Index values of format in the case of non-numerical indexes.
       * <p>
       * <p>Examples:
       * <ul>
       * <li>{@code {"", "(N)", "(H)"}}
       * <li>{@code {"r", "g", "b"}}
       * <li>{@code {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}}
       */
      String[] values() default {};

      /**
       * Manually sets the column index. This property overrides all other
       * properties.
       */
      int columnIndex() default -1;
    }
  }

  public static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T load(T excel, FileHandle txt) throws IOException {
    return load(excel, txt, null);
  }

  public static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T load(T excel, FileHandle txt, FileHandle bin) throws IOException {
    throw null;
  }

  static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadTxt(T excel, FileHandle handle) throws IOException {
    InputStream in = handle.read();
    try {
      MDC.put("excel", handle.path());
      TxtParser parser = TxtParser.parse(in);
      return loadTxt(excel, parser);
    } catch (Throwable t) {
      log.fatal("Unable to load {} as {}: {}",
          handle,
          excel.getClass().getCanonicalName(),
          ExceptionUtils.getRootCauseMessage(t),
          t);
      return ExceptionUtils.rethrow(t);
    } finally {
      MDC.remove("excel");
      IOUtils.closeQuietly(in);
    }
  }

  static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadTxt(T excel, TxtParser parser)
      throws IOException, ParseException, IllegalAccessException
  {
    final Class<E> entryClass = excel.entryClass();
    final boolean indexed = ClassUtils.hasAnnotation(entryClass, Indexed.class);

    final String[] TMP = new String[1];
    Field primaryKey = null, firstKey = null;
    Array<Triple<Field, int[], String[]>> columns = new Array<>(true, parser.numColumns(), Triple.class);
    for (Field field : entryClass.getFields()) {
      Entry.Column column = field.getAnnotation(Entry.Column.class);
      if (column == null) {
        log.warn("{} is not tagged with {}", field, Entry.Column.class.getCanonicalName());
        continue;
      }

      PrimaryKey key = field.getAnnotation(PrimaryKey.class);
      if (key != null) {
        if (!ArrayUtils.contains(PrimaryKey.SUPPORTED_TYPES, field.getType())) {
          throw new ParseException(field, "%s must be one of %s",
              field, Arrays.toString(PrimaryKey.SUPPORTED_TYPES));
        }

        if (indexed) {
          // Indexed excels have their primary key assigned automatically based on row index
          log.warn("{} has {} set to the primary key, but class is tagged with {}",
              entryClass, field, Indexed.class.getCanonicalName());
        } else if (primaryKey != null) {
          // Allow declared field tagged as a primary key to override inherited ones
          boolean primaryDeclared = ClassUtils.isDeclaredField(entryClass, primaryKey);
          boolean fieldDeclared = ClassUtils.isDeclaredField(entryClass, field);
          if (primaryDeclared != fieldDeclared) {
            if (fieldDeclared) {
              log.debug("primary key {} changed to {}", primaryKey, field);
              primaryKey = field;
            }
          } else {
            log.warn("multiple primary keys set within {}: {} and {}",
                entryClass, primaryKey.getName(), field.getName());
          }
        } else {
          primaryKey = field;
        }
      }

      if (firstKey == null) firstKey = field;
      populateColumnIndexes(columns, parser, column, field, TMP);
    }

    if (primaryKey == null && !indexed) {
      if (FORCE_PRIMARY_KEY) {
        throw new ParseException(entryClass, "%s does not have a %s set!",
            entryClass, PrimaryKey.class.getCanonicalName());
      } else {
        log.warn("{} does not have a {} set! Defaulting to first key: {}",
            entryClass, PrimaryKey.class.getCanonicalName(), firstKey);
        primaryKey = firstKey;
      }
    }

    // Locate the column index of the primary key
    // TODO: this operation can be cleaned up, but this is only an identity test
    int[] primaryKeyColumnIds = null;
    final Triple<Field, int[], String[]>[] columnTriples = columns.items;
    for (int i = 0, s = columnTriples.length; i < s; i++) {
      if (columnTriples[i].getLeft() == primaryKey) {
        primaryKeyColumnIds = columnTriples[i].getMiddle();
        break;
      }
    }

    int nonzeroIndex = -1;
    if (!indexed) {
      for (int i = 0, s = primaryKeyColumnIds.length; i < s; i++) {
        if (primaryKeyColumnIds[i] >= 0) {
          nonzeroIndex = i;
          break;
        }
      }

      if (nonzeroIndex == -1) {
        throw new ParseException(primaryKey,
            "primary key %s does not have any columns associated with it",
            primaryKey);
      }
    }

    final int primaryKeyColumnId = indexed ? -1 : primaryKeyColumnIds[nonzeroIndex];
    final Class primaryKeyType = indexed ? null : primaryKey.getType();
    for (int i = excel.offset(); parser.cacheLine() != -1; i++) {
      E entry = excel.newEntry();
      String name = indexed ? null : parser.parseString(primaryKeyColumnId, "");
      try {
        MDC.put("entry", indexed || StringUtils.isBlank(name) ? "" + i : name);
        parseColumns(excel, entry, name, columns, parser);
      } finally {
        MDC.remove("entry");
      }
      putIndex(primaryKey, primaryKeyType, i++, indexed, excel, entry);
    }

    return excel;
  }

  static void
  catchParseException(
      Throwable t,
      Field field,
      Class type,
      String key,
      String columnName,
      CharSequence token
  ) {
    ParseException parseException = new ParseException(t, field,
        "error parsing field %s row: '%s' column: '%s': '%s' as %s",
        field, key, columnName, token.toString(),
        type.isArray() ? type.getComponentType().getCanonicalName() : type.getCanonicalName());
    log.warn(parseException.getMessage(), parseException);
  }

  static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  void parseColumns(
      T excel,
      E entry,
      String key,
      Array<Triple<Field, int[], String[]>> columns,
      TxtParser parser
  )
      throws IllegalAccessException, ParseException
  {
    for (Triple<Field, int[], String[]> column : columns) {
      final Field field = column.getLeft();
      final int[] columnIds = column.getMiddle();
      final int numColumns = columnIds.length;
      final String[] columnNames = column.getRight();
      final Class type = field.getType();
      try {
        if (type == String.class) {
          try {
            field.set(entry, parser.parseString(columnIds[0], ""));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == String[].class) {
          final String[] value = new String[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseString(columnIds[i], "");
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == byte.class) {
          try {
            field.setByte(entry, parser.parseByte(columnIds[0], (byte) 0));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == byte[].class) {
          final byte[] value = new byte[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseByte(columnIds[i], (byte) 0);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == short.class) {
          try {
            field.setShort(entry, parser.parseShort(columnIds[0], (short) 0));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == short[].class) {
          final short[] value = new short[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseShort(columnIds[i], (short) 0);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == int.class) {
          try {
            field.setInt(entry, parser.parseInt(columnIds[0], 0));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == int[].class) {
          final int[] value = new int[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseInt(columnIds[i], 0);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == long.class) {
          try {
            field.setLong(entry, parser.parseLong(columnIds[0], 0L));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == long[].class) {
          final long[] value = new long[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseLong(columnIds[i], 0L);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == boolean.class) {
          try {
            field.setBoolean(entry, parser.parseBoolean(columnIds[0], false));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == boolean[].class) {
          final boolean[] value = new boolean[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseBoolean(columnIds[i], false);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == float.class) {
          try {
            field.setFloat(entry, parser.parseFloat(columnIds[0], 0f));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == float[].class) {
          final float[] value = new float[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseFloat(columnIds[i], 0f);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else if (type == double.class) {
          try {
            field.setDouble(entry, parser.parseDouble(columnIds[0], 0d));
          } catch (Throwable t) {
            catchParseException(t, field, type, key, columnNames[0], parser.token(columnIds[0]));
          }
        } else if (type == double[].class) {
          final double[] value = new double[numColumns];
          for (int i = 0; i < numColumns; i++) {
            try {
              value[i] = parser.parseDouble(columnIds[i], 0d);
            } catch (Throwable t) {
              catchParseException(t, field, type, key, columnNames[i], parser.token(columnIds[i]));
            }
          }
          field.set(entry, value);
        }

        else {
          throw new ParseException(field, "Cannot parse fields of type %s",
              org.apache.commons.lang3.ClassUtils.getCanonicalName(type));
        }
      } catch (ColumnFormat t) {
        ParseException parseException = new ParseException(field,
            "error parsing field %s row: '%s' column: '%s': '%s' as %s",
            field, key, columnNames[t.columnIndex()], t.columnText(),
            type.isArray() ? type.getComponentType().getCanonicalName() : type.getCanonicalName());
        parseException.initCause(t);
        throw parseException;
      }
    }
  }

  /**
   * Parses the specified field using it's column definition annotation to
   * generate a list of column names and indexes associated with them. These
   * indexes are then stored as a mapping from field to associated column
   * indexes which can be used to retrieve data from the backing excel.
   */
  static void populateColumnIndexes(
      final Array<Triple<Field, int[], String[]>> columns,
      final TxtParser parser,
      final Entry.Column column,
      final Field field,
      final String[] TMP
  ) throws ParseException {
      final String format = column.format();
      final String[] values = column.values();
      final int startIndex = column.startIndex();
      final int endIndex = column.endIndex();
      final int columnIndex = column.columnIndex();
    if (columnIndex >= 0) {
      final int[] columnIndexes = new int[] { columnIndex };
      final String[] columnNames = new String[] { null };
      columns.add(Triple.of(field, columnIndexes, columnNames));
      log.trace("pushing column <{}>->{}", field, columnIndexes);
    } else if (format.isEmpty()) {
      final String fieldName = field.getName();
      if (values.length > 0) {
        // values[] used as literal column names
        checkArrayColumns(field, values.length);
        String[] columnNames = new String[values.length];
        for (int i = 0; i < values.length; i++) {
          columnNames[i] = values[i];
        }

        putColumns(columns, parser, field, columnNames);
      } else if (startIndex == 0 && endIndex == 0) {
        // field name used as literal column name
        TMP[0] = fieldName;
        putColumns(columns, parser, field, TMP);
      } else {
        // field name + indexes used as column names
        checkArrayColumns(field, endIndex - startIndex);
        String[] columnNames = new String[endIndex - startIndex];
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
          columnNames[j] = fieldName + i;
        }

        putColumns(columns, parser, field, columnNames);
      }
    } else {
      if (startIndex == 0 && endIndex == 0) {
        // format used as literal column name
        TMP[0] = format;
        putColumns(columns, parser, field, TMP);
      } else {
        checkArrayColumns(field, endIndex - startIndex);
        String[] columnNames = new String[endIndex - startIndex];
        if (values.length == 0) {
          // format used in conjunction with indexes as column names
          // format must contain %d within it, replaced with indexes
          for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            columnNames[j] = String.format(format, i);
          }
        } else {
          // format used in conjunction with values as column names
          // format must contain as many values as indexes
          for (int i = 0, s = values.length; i < s; i++) {
            columnNames[i] = String.format(format, values[i]);
          }
        }

        putColumns(columns, parser, field, columnNames);
      }
    }

    if (log.debugEnabled()) {
      StringBuilder builder = new StringBuilder(256);
      builder.append('{');
      for (Triple<Field, int[], String[]> pair : columns) {
        builder
            .append(pair.getLeft().getName())
            .append('=')
            .append(Arrays.toString(pair.getMiddle()))
            .append(", ");
      }
      if (columns.size > 0) builder.setLength(builder.length() - 2);
      builder.append('}');
      log.debug("columns: {}", builder.toString());
    }
  }

  static void checkArrayColumns(Field field, int length) throws ParseException {
    if (!field.getType().isArray() && length > 1) {
      throw new ParseException(field, ""
          + "field %s corresponds to multiple columns. "
          + "is it supposed to be an array type?", field);
    }
  }

  static int putColumns(
      Array<Triple<Field, int[], String[]>> columns,
      TxtParser parser,
      Field field,
      String[] columnNames
  ) {
    final int index = columns.size;
    final int[] columnIndexes = parser.columnId(columnNames);
    columns.add(Triple.of(field, columnIndexes, columnNames));
    log.trace("pushing columns {}->{}", columnNames, columnIndexes);
    if (log.warnEnabled()) {
      for (int i = 0, s = columnIndexes.length; i < s; i++) {
        if (columnIndexes[i] == -1) {
          log.warn("Unable to parse column named '{}'", columnNames[i]);
        }
      }
    }

    return index;
  }

  static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadBin(T excel, FileHandle handle) {
    throw null;
  }

  static <E extends Entry, T extends Excel<E, ?>>
  void putIndex(
      Field primaryKey,
      Class primaryKeyType,
      int i,
      boolean indexed,
      T excel,
      E entry
  ) throws IllegalAccessException {
    if (indexed) {
      excel.put(i, entry);
    } else if (primaryKeyType == int.class) {
      int id = primaryKey.getInt(entry);
      excel.put(id, entry);
    } else if (primaryKeyType == String.class) {
      String id = (String) primaryKey.get(entry);
      excel.put(i, entry);

      if (excel.stringToIndex == EMPTY_OBJECT_INT_MAP) excel.stringToIndex = new ObjectIntMap<>();
      if (!excel.stringToIndex.containsKey(id)) excel.stringToIndex.put(id, i);
    }
  }

  protected final Class<E> entryClass;
  protected ObjectIntMap<String> stringToIndex;
  protected IntMap<E> entries;
  protected Array<Entry> ordered;

  protected Excel(Class<E> entryClass) {
    this(entryClass, 53);
  }

  protected Excel(Class<E> entryClass, int initialCapacity) {
    this(entryClass, initialCapacity, 0.8f);
  }

  protected Excel(Class<E> entryClass, int initialCapacity, float loadFactor) {
    this.entryClass = entryClass;
    this.stringToIndex = emptyMap();
    this.entries = new IntMap<>(initialCapacity, loadFactor);
    this.ordered = new Array<>(true, (int) (initialCapacity * loadFactor), Entry.class);
  }

  public Class<? extends Excel> excelClass() {
    return getClass();
  }

  public Class<E> entryClass() {
    return entryClass;
  }

  protected void put(int id, E value) {}

  protected int offset() {
    return 0;
  }

  protected void init() {}

  public E get(String id) {
    return get(index(id));
  }

  public E get(int id) {
    return entries.get(id);
  }

  public int index(String id) {
    return stringToIndex.get(id, -1);
  }

  public int size() {
    return entries.size;
  }

  public abstract E newEntry();

  public abstract S newSerializer();

  @Override
  public Iterator<E> iterator() {
    return entries.values().iterator();
  }
}
