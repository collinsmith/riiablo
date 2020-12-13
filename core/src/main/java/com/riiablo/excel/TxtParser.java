package com.riiablo.excel;

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
    if (array.size == 0) return "";
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
          cache.add(TO_UPPER[i]);
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
lineBuilder:
    for (;;) {
      final int i = in.read();
      switch (i) {
        case -1:
          return -1;
        case HT:
          tokenOffsets.add(cache.size);
          break;
        case CR:
          in.skip(1);
        case LF:
          tokenOffsets.add(cache.size);
          break lineBuilder;
        default:
          cache.add((byte) i);
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
          index, numTokens, numColumns, tokens());
      return cacheLine();
    }

    if (log.traceEnabled()) {
      final int[] tokenOffsets = this.tokenOffsets.items;
      for (int i = 1, j = tokenOffsets[i - 1], s = this.tokenOffsets.size; i < s; i++) {
        final int tokenOffset = tokenOffsets[i];
        log.trace("{}={}", columnName(i - 1), line.subSequence(j, tokenOffset, false));
        j = tokenOffset;
      }
    }

    return tokenOffsets.size - 1;
  }

  public int numColumns() {
    return numColumns;
  }

  public String[] columnNames() {
    final String[] columnNames = new String[numColumns];
    for (int i = 0; i < numColumns; i++) columnNames[i] = columnName(i);
    return columnNames;
  }

  public String columnName(int i) {
    return columnNames.get(i);
  }

  public String rowName() {
    return parseString(0, "");
  }

  public int columnId(String columnName) {
    return columnIds.get(columnName.toUpperCase(), -1);
  }

  public int[] columnId(String[] columnNames) {
    final int numColumns = columnNames.length;
    final int[] columnIds = new int[numColumns];
    for (int i = 0; i < numColumns; i++) columnIds[i] = columnId(columnNames[i]);
    return columnIds;
  }

  public int numTokens() {
    return numTokens;
  }

  public AsciiString[] tokens() {
    final int numTokens = numTokens();
    final AsciiString[] tokens = new AsciiString[numTokens];
    for (int i = 0; i < numTokens; i++) tokens[i] = token(i);
    return tokens;
  }

  public AsciiString token(int i) {
    final int[] tokenOffsets = tokenOffsetsCache;
    return line.subSequence(tokenOffsets[i], tokenOffsets[i + 1]);
  }

  public byte parseByte(int i, byte defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    final int intValue = line.parseInt(startOffset, endOffset);
    final byte result = (byte) intValue;
    if (result != intValue) {
      throw new NumberFormatException(line.subSequence(startOffset, endOffset, false).toString());
    }
    return result;
  }

  public short parseShort(int i, short defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    return line.parseShort(startOffset, endOffset);
  }

  public int parseInt(int i, int defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    return line.parseInt(startOffset, endOffset);
  }

  public long parseLong(int i, long defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    return line.parseLong(startOffset, endOffset);
  }

  public boolean parseBoolean(int i, boolean defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    final int intValue = line.parseInt(startOffset, endOffset);
    if ((intValue & 1) != intValue) {
      log.warn("boolean exceeds boolean radix at {}:{} (\"{}\", \"{}\"): {}",
          index, i, rowName(), columnName(i), intValue);
    }

    return intValue != 0;
  }

  public float parseFloat(int i, float defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    return line.parseFloat(startOffset, endOffset);
  }

  public double parseDouble(int i, double defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    return line.parseDouble(startOffset, endOffset);
  }

  public String parseString(int i, String defaultValue) {
    final int[] tokenOffsets = tokenOffsetsCache;
    final int startOffset = tokenOffsets[i];
    final int endOffset = tokenOffsets[i + 1];
    if (startOffset >= endOffset) return defaultValue;
    return line.toString(tokenOffsets[i], tokenOffsets[i + 1]);
  }
}
