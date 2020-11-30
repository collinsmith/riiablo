package com.riiablo.excel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectIntMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TxtParser implements Closeable {
  private static final String TAG = "TxtParser";

  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_COLS = DEBUG && true;
  private static final boolean DEBUG_ROWS = DEBUG && true;

  private static final boolean FORCE_BOOL = true; // logs error if boolean is not 0 or 1
  private static final boolean FORCE_COLS = true; // ignores row if token count != columns count

  private static final String EXPANSION = "Expansion";

  public static TxtParser loadFromFile(FileHandle handle) {
    return loadFromStream(handle.read());
  }

  public static TxtParser loadFromStream(InputStream in) {
    BufferedReader reader = null;
    try {
      reader = IOUtils.buffer(new InputStreamReader(in, "US-ASCII"));
      return new TxtParser(reader);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read excel file", t);
    }
  }

  final BufferedReader reader;
  final ObjectIntMap<String> ids;
  final String columns[];
  int index;

  String line;
  String tokens[];

  private TxtParser(BufferedReader reader) {
    this.reader = reader;

    try {
      line = reader.readLine();
      columns = StringUtils.splitPreserveAllTokens(line, '\t');
      if (DEBUG_COLS) Gdx.app.debug(TAG, "cols=" + Arrays.toString(columns));

      ids = new ObjectIntMap<>();
      for (int i = 0; i < columns.length; i++) {
        String key = columns[i].toLowerCase();
        if (!ids.containsKey(key)) ids.put(key, i);
      }
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read TXT", t);
    }
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  public String[] getColumns() {
    return columns;
  }

  public int getNumColumns() {
    return columns.length;
  }

  public String getColumnName(int i) {
    return columns[i];
  }

  public String[] getTokens() {
    return tokens;
  }

  public int getNumTokens() {
    return tokens.length;
  }

  public int getColumnId(String s) {
    return ids.get(s.toLowerCase(), -1);
  }

  public int[] getColumnId(String[] s) {
    int[] columnIds = new int[s.length];
    for (int i = 0; i < s.length; i++) columnIds[i] = getColumnId(s[i]);
    return columnIds;
  }

  public String nextLine() {
    try {
      index++;
      line = reader.readLine();
      if (line == null) {
        return null;
      } else if (line.equalsIgnoreCase(EXPANSION)) {
        return nextLine();
      }

      tokens = StringUtils.splitPreserveAllTokens(line, '\t');
      if (DEBUG_ROWS) Gdx.app.debug(TAG, (index - 1) + ": " + Arrays.toString(tokens));
      if (FORCE_COLS && tokens.length != columns.length) {
        Gdx.app.error(TAG, "Skipping row " + Arrays.toString(tokens));
        return nextLine();
      }

      return line;
    } catch (IOException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
      return null;
    }
  }

  public String getString(int i) {
    return tokens[i];
  }

  public byte getByte(int i) {
    return NumberUtils.toByte(tokens[i]);
  }

  public short getShort(int i) {
    return NumberUtils.toShort(tokens[i]);
  }

  public int getInt(int i) {
    return NumberUtils.toInt(tokens[i]);
  }

  public long getLong(int i) {
    return NumberUtils.toLong(tokens[i]);
  }

  public boolean getBoolean(int i) {
    int value = getInt(i);
    if (FORCE_BOOL && (value & 1) != value) Gdx.app.error(TAG, String.format("boolean value != 0 or 1 at row %d col %d (\"%s\", \"%s\"): %d", index, i, getString(0), getColumnName(i), value));
    return value != 0;
  }

  public String[] getString(int[] cols) {
    String[] data = new String[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getString(cols[i]);
    return data;
  }

  public byte[] getByte(int[] cols) {
    byte[] data = new byte[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getByte(cols[i]);
    return data;
  }

  public short[] getShort(int[] cols) {
    short[] data = new short[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getShort(cols[i]);
    return data;
  }

  public int[] getInt(int[] cols) {
    int[] data = new int[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getInt(cols[i]);
    return data;
  }

  public long[] getLong(int[] cols) {
    long[] data = new long[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getLong(cols[i]);
    return data;
  }

  public boolean[] getBoolean(int[] cols) {
    boolean[] data = new boolean[cols.length];
    for (int i = 0; i < cols.length; i++) data[i] = getBoolean(cols[i]);
    return data;
  }
}
