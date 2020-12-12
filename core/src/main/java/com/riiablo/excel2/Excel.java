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
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
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
      TxtParser parser = TxtParser.parse(in);
      return loadTxt(excel, parser);
    } catch (IllegalAccessException|ParseException t) {
      log.fatal("Unable to load {} as {}: {}",
          handle,
          excel.getClass().getCanonicalName(),
          ExceptionUtils.getRootCauseMessage(t),
          t);
      return ExceptionUtils.rethrow(t);
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

  public Class<? extends Excel> excelClass() {
    return getClass();
  }

  public Class<E> entryClass() {
    return entryClass;
  }

  public abstract E newEntry();

  public abstract S newSerializer();

  @Override
  public Iterator<E> iterator() {
    throw new UnsupportedOperationException();
  }
}
