package com.riiablo.excel;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectIntMap;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class TxtParser implements Closeable {
  private static final Logger log = LogManager.getLogger(TxtParser.class);

  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_ROWS = DEBUG && true;

  private static final boolean FORCE_BOOL = true; // logs error if boolean is not 0 or 1
  private static final boolean FORCE_COLS = true; // ignores row if token count != columns count

  private static final String EXPANSION = "Expansion";

  public static TxtParser parse(FileHandle handle) throws IOException {
    return parse(handle.read());
  }

  public static TxtParser parse(InputStream in) throws IOException {
    BufferedReader reader = null;
    reader = IOUtils.buffer(new InputStreamReader(in, StandardCharsets.US_ASCII));
    return new TxtParser(reader);
  }

  final BufferedReader reader;
  final ObjectIntMap<String> columnIds;
  final String columnNames[];
  int index;

  String line;
  String tokens[];

  private TxtParser(BufferedReader reader) throws IOException {
    this.reader = reader;
    line = reader.readLine();
    columnNames = StringUtils.splitPreserveAllTokens(line, '\t');
    log.debug("columnNames: {}", columnNames);

    columnIds = new ObjectIntMap<>();
    for (int i = 0; i < columnNames.length; i++) {
      String key = columnNames[i].toLowerCase();
      if (!columnIds.containsKey(key)) columnIds.put(key, i);
    }
    log.debug("columnIds: {}", columnIds);
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  public String[] getColumnNames() {
    return columnNames;
  }

  public int getNumColumns() {
    return columnNames.length;
  }

  public String getColumnName(int i) {
    return columnNames[i];
  }

  public String[] getTokens() {
    return tokens;
  }

  public int getNumTokens() {
    return tokens.length;
  }

  public int getColumnId(String s) {
    return columnIds.get(s.toLowerCase(), -1);
  }

  public int[] getColumnId(String[] s) {
    int[] columnIds = new int[s.length];
    for (int i = 0; i < s.length; i++) columnIds[i] = getColumnId(s[i]);
    return columnIds;
  }

  public String nextLine() throws IOException {
    index++;
    line = reader.readLine();
    if (line == null) {
      return null;
    } else if (line.equalsIgnoreCase(EXPANSION)) {
      return nextLine();
    }

    tokens = StringUtils.splitPreserveAllTokens(line, '\t');
    if (log.traceEnabled()) log.trace("{}: {}", (index - 1), Arrays.toString(tokens));
    if (FORCE_COLS && tokens.length != columnNames.length) {
      log.warn("skipping row {}: contains {} tokens, expected {}; tokens: {}",
          index, tokens.length, columnNames.length, Arrays.toString(tokens));
      return nextLine();
    }

    return line;
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
    if (FORCE_BOOL && (value & 1) != value) {
      log.warn("boolean value != 0 or 1 at {}:{} (\"{}\", \"{}\"): {}",
          index, i, getString(0), getColumnName(i), value);
    }
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
