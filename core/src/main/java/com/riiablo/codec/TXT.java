package com.riiablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StringBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TXT {
  private static final String TAG = "TXT";
  private static final boolean DEBUG      = !true;
  private static final boolean DEBUG_COLS = DEBUG && true;
  private static final boolean DEBUG_ROWS = DEBUG && true;
  private static final boolean DEBUG_BOOL = true;

  protected final ObjectIntMap<String> columns;
  protected final Array<String[]>      data;

  private TXT(ObjectIntMap<String> columns, Array<String[]> data) {
    this.columns = columns;
    this.data = data;
  }

  protected TXT(TXT src) {
    this.columns = src.columns;
    //this.rows = src.rows;
    this.data = src.data;
  }

  public int getColumns() {
    return columns.size;
  }

  public int getRows() {
    return data.size;
  }

  public String getColumnName(int col) {
    return columns.findKey(col);
  }

  public String getRowName(int row) {
    return data.items[row][0];
  }

  public int getColumnId(String text) {
    return columns.get(text.toLowerCase(), -1);
  }

  public int[] getColumnId(String[] text) {
    int[] columnIds = new int[text.length];
    for (int i = 0; i < text.length; i++) columnIds[i] = getColumnId(text[i]);
    return columnIds;
  }

  public IntArray getColumnId(String[] text, IntArray dst) {
    dst.clear();
    for (int i = 0; i < text.length; i++) dst.add(getColumnId(text[i]));
    return dst;
  }

  public String getString(int row, int col) {
    if (row == -1 || col == -1) return null;
    return data.items[row][col];
  }

  public byte getByte(int row, int col) {
    String value = getString(row, col);
    return NumberUtils.toByte(value, (byte) 0);
  }

  public short getShort(int row, int col) {
    String value = getString(row, col);
    return NumberUtils.toShort(value, (short) 0);
  }

  public int getInt(int row, int col) {
    String value = getString(row, col);
    return NumberUtils.toInt(value, 0);
  }

  public long getLong(int row, int col) {
    String value = getString(row, col);
    return NumberUtils.toLong(value, 0L);
  }

  public boolean getBoolean(int row, int col) {
    int value = getInt(row, col);
    if (DEBUG_BOOL && value > 1) Gdx.app.error(TAG, String.format("boolean value > 1 at row %d col %d (\"%s\", \"%s\"): %d", row, col, getRowName(row), getColumnName(col), value));
    return value > 0;
  }

  public String[] getString(int row, int[] cols) {
    String[] data = new String[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getString(row, cols[i]);
    return data;
  }

  public byte[] getByte(int row, int[] cols) {
    byte[] data = new byte[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getByte(row, cols[i]);
    return data;
  }

  public short[] getShort(int row, int[] cols) {
    short[] data = new short[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getShort(row, cols[i]);
    return data;
  }

  public int[] getInt(int row, int[] cols) {
    int[] data = new int[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getInt(row, cols[i]);
    return data;
  }

  public long[] getLong(int row, int[] cols) {
    long[] data = new long[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getLong(row, cols[i]);
    return data;
  }

  public boolean[] getBoolean(int row, int[] cols) {
    boolean[] data = new boolean[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getBoolean(row, cols[i]);
    return data;
  }

  public static TXT loadFromFile(FileHandle handle) {
    return loadFromStream2(handle.read());
  }

  // FIXME: Problem loading WeaponClass.txt
  public static TXT loadFromStream(InputStream in) {
    BufferedReader reader = null;
    try {
      reader = IOUtils.buffer(new InputStreamReader(in, "US-ASCII"));
      ObjectIntMap<String> columns = new ObjectIntMap<>();
      String[] columnNames = StringUtils.splitPreserveAllTokens(reader.readLine(), '\t');
      for (int i = 0; i < columnNames.length; i++) {
        String key = columnNames[i].toLowerCase();
        if (!columns.containsKey(key)) columns.put(key, i);
      }
      if (DEBUG_COLS) {
        ObjectSet<String> duplicates = new ObjectSet<>();
        String[] colNames = new String[columns.size];
        for (int i = 0, j = 0; i < columnNames.length; i++) {
          String columnName = columnNames[i];
          if (!duplicates.add(columnName)) continue;
          colNames[j++] = columnName;
        }
        Gdx.app.debug(TAG, "cols=" + Arrays.toString(colNames));
      }

      Array<String[]> data = new Array<>(String[].class);
      for (String line; (line = reader.readLine()) != null;) {
        String[] tmp = split(columnNames.length, line, '\t');
        if (tmp.length != columnNames.length) {
          if (DEBUG_ROWS) Gdx.app.debug(TAG, "Skipping row " + Arrays.toString(tmp));
          continue;
        }

        data.add(tmp);
        if (DEBUG_ROWS) Gdx.app.debug(TAG, data.size - 1 + ": " + Arrays.toString(tmp));
      }

      return new TXT(columns, data);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read TXT", t);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  private static String[] split(int len, String str, char token) {
    StringBuilder builder = new StringBuilder(32);
    String[] tokens = new String[len];
    char c;
    int numTokens = 0;
    final int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      c = str.charAt(i);
      if (c == token) {
        tokens[numTokens++] = builder.toString();
        builder.setLength(0);
      } else {
        builder.append(c);
      }
    }
    if (numTokens < len) Arrays.fill(tokens, numTokens, len, StringUtils.EMPTY);
    return tokens;
  }

  // Older method
  @Deprecated
  public static TXT loadFromStream2(InputStream in) {
    BufferedReader reader = null;
    try {
      reader = IOUtils.buffer(new InputStreamReader(in, "US-ASCII"));
      ObjectIntMap<String> columns = new ObjectIntMap<>();
      String[] columnNames = reader.readLine().split("\t");
      for (int i = 0; i < columnNames.length; i++) {
        String key = columnNames[i].toLowerCase();
        if (!columns.containsKey(key)) columns.put(key, i);
      }
      if (DEBUG_COLS) {
        ObjectSet<String> duplicates = new ObjectSet<>();
        String[] colNames = new String[columns.size];
        for (int i = 0, j = 0; i < columnNames.length; i++) {
          String columnName = columnNames[i];
          if (!duplicates.add(columnName)) {
            continue;
          }

          colNames[j++] = columnName;
        }
        Gdx.app.debug(TAG, "cols=" + Arrays.toString(colNames));
      }

      Array<String[]> data = new Array<>(String[].class);
      for (String line; (line = reader.readLine()) != null;) {
        String[] tmp = line.split("\t", -1);
        if (tmp.length != columnNames.length) {
          if (DEBUG_ROWS) Gdx.app.debug(TAG, "Skipping row " + Arrays.toString(tmp));
          continue;
        }

        data.add(tmp);
        if (DEBUG_ROWS) Gdx.app.debug(TAG, data.size - 1 + ": " + Arrays.toString(tmp));
      }

      return new TXT(columns, data);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read TXT", t);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }
}
