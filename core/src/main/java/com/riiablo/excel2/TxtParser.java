package com.riiablo.excel2;

import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectIntMap;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class TxtParser {
  private static final Logger log = LogManager.getLogger(TxtParser.class);

  private static final int HT = '\t';
  private static final int CR = '\r';
  private static final int LF = '\n';

  private static final byte[] TO_UPPER;
  static {
    TO_UPPER = new byte[1 << Byte.SIZE];
    for (int i = 0; i < TO_UPPER.length; i++) {
      TO_UPPER[i] = (byte) i;
    }

    for (int i = 'a'; i <= 'z'; i++) {
      TO_UPPER[i] &= ~0x20;
    }
  }

  private static final AsciiString EXPANSION = AsciiString.cached("EXPANSION");

  public static TxtParser parse(InputStream in) throws IOException {
    return parse(in, 8192);
  }

  public static TxtParser parse(InputStream in, int bufferSize) throws IOException {
    return new TxtParser(in, bufferSize);
  }

  final int numColumns;
  final Array<String> columnNames;
  final ObjectIntMap<String> columnIds;

  BufferedInputStream in;
  AsciiString line;
  int index;
  final ByteArray cache;
  final IntArray tokenOffsets;
  int[] tokenOffsetsCache;
  int numTokens;

  TxtParser(InputStream in, int bufferSize) throws IOException {
    this.in = IOUtils.buffer(in, bufferSize);

    cache = new ByteArray(512);
    columnNames = new Array<>();
    columnIds = new ObjectIntMap<>();
    numColumns = parseColumnNames();

    log.info("numColumns: {}", numColumns);
    log.debug("columnNames: {}", columnNames);
    log.trace("columnIds: {}", columnIds);

    tokenOffsets = new IntArray();
  }

  private static String toString(ByteArray array) {
    String stringValue = new String(array.items, 0, array.size, CharsetUtil.US_ASCII);
    array.clear();
    return stringValue;
  }

  private int parseColumnNames() throws IOException {
    for (int i; (i = in.read()) != -1;) {
      switch (i) {
        case HT:
          putColumnName(toString(cache));
          break;
        case CR:
          in.skip(1);
        case LF:
          putColumnName(toString(cache));
          return columnNames.size;
        default:
          cache.add((byte) Character.toUpperCase(i));
      }
    }

    throw new IOException("Unexpected end of file while parsing column names");
  }

  private void putColumnName(String columnName) {
    if (!columnIds.containsKey(columnName)) {
      columnIds.put(columnName, columnNames.size);
    }

    columnNames.add(columnName);
  }

  public int cacheLine() throws IOException {
    cache.clear();
    tokenOffsets.clear();
    tokenOffsets.add(0);
    index++;
copy:
    for (int i; (i = in.read()) != -1;) {
      switch (i) {
        case HT:
          tokenOffsets.add(cache.size);
          break;
        case CR:
          in.skip(1);
        case LF:
          tokenOffsets.add(cache.size);
          break copy;
        default:
          cache.add(TO_UPPER[i]);
      }
    }

    numTokens = tokenOffsets.size - 1;
    tokenOffsetsCache = tokenOffsets.items;
    line = new AsciiString(cache.items, 0, cache.size, false);
    log.debug("line: {}", line);
    if (line.contentEqualsIgnoreCase(EXPANSION)) {
      log.trace("skipping row {}: {} is an ignored symbol", index, EXPANSION);
      return cacheLine();
    }

    if (numTokens != numColumns) {
      log.warn("skipping row {}: contains {} tokens, expected {}; tokens: {}",
          index, numTokens, numColumns, getTokens());
      return cacheLine();
    }

    if (log.traceEnabled()) {
      final int[] tokenOffsets = this.tokenOffsets.items;
      for (int i = 1, j = tokenOffsets[i - 1], s = this.tokenOffsets.size; i < s; i++) {
        final int tokenOffset = tokenOffsets[i];
        log.trace("{}={}", getColumnName(i - 1), line.subSequence(j, tokenOffset, false));
        j = tokenOffset;
      }
    }

    return tokenOffsets.size - 1;
  }

  public int getNumColumns() {
    return numColumns;
  }

  public String[] getColumnNames() {
    final String[] columnNames = new String[numColumns];
    for (int i = 0; i < numColumns; i++) columnNames[i] = getColumnName(i);
    return columnNames;
  }

  public String getColumnName(int i) {
    return columnNames.get(i).toString();
  }

  public String getRowName() {
    return parseString(0);
  }

  public int getColumnId(String columnName) {
    return columnIds.get(columnName.toUpperCase(), -1);
  }

  public int[] getColumnId(String[] columnNames) {
    final int numColumns = columnNames.length;
    final int[] columnIds = new int[numColumns];
    for (int i = 0; i < numColumns; i++) columnIds[i] = getColumnId(columnNames[i]);
    return columnIds;
  }

  public int getNumTokens() {
    return numTokens;
  }

  public AsciiString[] getTokens() {
    final int numTokens = getNumTokens();
    final AsciiString[] tokens = new AsciiString[numTokens];
    for (int i = 0; i < numTokens; i++) tokens[i] = getToken(i);
    return tokens;
  }

  public AsciiString getToken(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.subSequence(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public byte parseByte(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int intValue = line.parseInt(tokenOffsets[i], tokenOffsets[i + 1]);
    final byte result = (byte) intValue;
    if (result != intValue) {
      throw new NumberFormatException(line.subSequence(tokenOffsets[i], tokenOffsets[i + 1], false).toString());
    }

    return result;
  }

  public short parseShort(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.parseShort(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public int parseInt(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.parseInt(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public long parseLong(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.parseLong(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public boolean parseBoolean(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int intValue = line.parseInt(tokenOffsets[i], tokenOffsets[i + 1]);
    if ((intValue & 1) != intValue) {
      log.warn("boolean exceeds boolean radix at {}:{} (\"{}\", \"{}\"): {}",
          index, i, getRowName(), getColumnName(i), intValue);
    }

    return intValue != 0;
  }

  public float parseFloat(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.parseFloat(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public double parseDouble(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.parseDouble(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public String parseString(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.toString(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public byte[] parseByte(int[] columns) {
    final int length = columns.length;
    byte[] values = new byte[length];
    for (int i = 0; i < length; i++) values[i] = parseByte(columns[i]);
    return values;
  }

  public short[] parseShort(int[] columns) {
    final int length = columns.length;
    short[] values = new short[length];
    for (int i = 0; i < length; i++) values[i] = parseShort(columns[i]);
    return values;
  }

  public long[] parseLong(int[] columns) {
    final int length = columns.length;
    long[] values = new long[length];
    for (int i = 0; i < length; i++) values[i] = parseLong(columns[i]);
    return values;
  }

  public boolean[] parseBoolean(int[] columns) {
    final int length = columns.length;
    boolean[] values = new boolean[length];
    for (int i = 0; i < length; i++) values[i] = parseBoolean(columns[i]);
    return values;
  }

  public float[] parseFloat(int[] columns) {
    final int length = columns.length;
    float[] values = new float[length];
    for (int i = 0; i < length; i++) values[i] = parseFloat(columns[i]);
    return values;
  }

  public double[] parseDouble(int[] columns) {
    final int length = columns.length;
    double[] values = new double[length];
    for (int i = 0; i < length; i++) values[i] = parseDouble(columns[i]);
    return values;
  }

  public String[] parseString(int[] columns) {
    final int length = columns.length;
    String[] values = new String[length];
    for (int i = 0; i < length; i++) values[i] = parseString(columns[i]);
    return values;
  }
}
