package com.riiablo.excel2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

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

  /**
   * Tags the specified excel as indexed. Indexed excels apply a 1-to-1
   * assignment of row index to entry index. Used in the case where the excel
   * does not include a primary key column.
   */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Indexed {}

  /**
   * Root class of an excel entry.
   */
  public static abstract class Entry {
    /**
     * Tags a specified field as the primary key of the excel table.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PrimaryKey {}

    /**
     * Tags the specified field as a column within the excel table.
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
       * Manually sets the column index. Used in the case where a column might
       * not be named.
       */
      @Deprecated
      int columnIndex() default -1;

      /**
       * Whether or not to store value within the generated bin.
       */
      boolean bin() default true;

      /**
       * Tags the column as a foreign key in the specified excel.
       */
      Class<? extends Excel> foreignKey() default Excel.class;

      /**
       * Tags the column as a primary key for this excel.
       */
      boolean primaryKey() default false;
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
      TxtParser parser = TxtParser.parse(in);
      return loadTxt(excel, parser);
    } catch (IllegalAccessException t) {
      log.error("Unable to load {} as {}: {}",
          handle,
          excel.getClass().getCanonicalName(),
          ExceptionUtils.getRootCauseMessage(t),
          t);
      return ExceptionUtils.wrapAndThrow(t);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadTxt(T excel, TxtParser parser) throws IOException, IllegalAccessException {
    throw null;
  }

  static <E extends Entry, S extends Serializer<E>, T extends Excel<E, S>>
  T loadBin(T excel, FileHandle handle) {
    throw null;
  }

  protected final Class<E> entryClass;

  protected Excel(Class<E> entryClass) {
    this.entryClass = entryClass;
  }

  public abstract E newEntry();

  public abstract S newSerializer();

  @Override
  public Iterator<E> iterator() {
    throw new UnsupportedOperationException();
  }
}
