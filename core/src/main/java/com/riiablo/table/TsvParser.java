package com.riiablo.table;

import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectIntMap;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

/**
 * Written under the assumptions that the tab-separated value file format is
 * consistent as:
 *
 * C00\tC01\tC02\r\n <--- defines column names
 * c10\tc11\tc12\r\n
 * EXPANSION\t\t\r\n <--- may or may not be present
 * c30\tc31\tc32\r\n
 */
public class TsvParser implements ParserInput {
  private static final Logger log = LogManager.getLogger(TsvParser.class);

  /** Log warnings if {@link #parseBoolean} parses non-binary radixes */
  private static final boolean CHECK_BINARY_RADIX = true;

  private static final byte HT = '\t';
  private static final byte CR = '\r';
  private static final byte LF = '\n';

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

  private static AsciiString toUpper(AsciiString string) {
    final byte[] bytes = string.array();
    for (int i = string.arrayOffset(), s = i + string.length(); i < s; i++) {
      bytes[i] = TO_UPPER[bytes[i]];
    }

    string.arrayChanged();
    return string;
  }

  private static String toUpper(final AsciiString buffer, final int start, final int end) {
    final byte[] bytes = buffer.array();
    for (int i = start; i < end; i++) {
      bytes[i] = TO_UPPER[bytes[i]];
    }

    return buffer.toString(start, end);
  }

  private static final byte[] EXPANSION = "\nEXPANSION".getBytes(CharsetUtil.US_ASCII);

  /**
   * Consumes the regexp {@code \nEXPANSION\t*\r}
   */
  static int skipExpansion(final byte[] bytes, final int offset) {
    final byte[] EXPANSION = TsvParser.EXPANSION;
    final int length = EXPANSION.length;
    if (offset + length >= bytes.length) return offset;
    int i = offset;
    for (int j = 0; j < length; i++, j++) {
      if (TO_UPPER[bytes[i]] != EXPANSION[j]) {
        return offset;
      }
    }

    while (bytes[i] != CR) i++;
    return i + 1;
  }

  public static TsvParser parse(byte[] bytes) {
    return new TsvParser(bytes);
  }

  final byte[] bytes;
  final AsciiString buffer;

  final int numRecords;
  final ObjectIntMap<String> recordIds = new ObjectIntMap<>(389);
  final IntArray lineOffsets = new IntArray(256);

  final int numFields;
  final Array<String> fieldNames = new Array<>(16);
  final ObjectIntMap<String> fieldIds = new ObjectIntMap<>(53);
  final IntArray tokenOffsets = new IntArray(256 * 16);

  int primaryKeyFieldId = -1;

  TsvParser(byte[] bytes) {
    this.bytes = bytes;
    buffer = new AsciiString(bytes, false);
    final int firstRecordOffset = preprocessFieldNames();
    numFields = parseFieldNames();
    numRecords = preprocess(firstRecordOffset);
    log.debug("{} records, {} fields, {} tokens",
        lineOffsets.size, numFields, tokenOffsets.size);
  }

  private int preprocessFieldNames() {
    lineOffsets.add(0);
    tokenOffsets.add(0);
    final byte[] bytes = this.bytes;
    final int length = bytes.length;
    for (int i = 0; i < length; i++) {
      switch (bytes[i]) {
        case HT:
          tokenOffsets.add(i);
          tokenOffsets.add(i + 1);
          break;
        case CR:
          tokenOffsets.add(i);
          lineOffsets.add(tokenOffsets.size);
          break;
        case LF:
          return i + 1;
      }
    }

    return -1;
  }

  private int parseFieldNames() {
    final int numFields = lineOffsets.get(1);
    final int[] tokenOffsets = this.tokenOffsets.items;
    for (int i = 0; i < numFields;) {
      putFieldName(toUpper(buffer, tokenOffsets[i++], tokenOffsets[i++]));
    }

    return numFields >> 1;
  }

  private int preprocess(int offset) {
    lineOffsets.clear();
    lineOffsets.add(0);
    tokenOffsets.clear();
    tokenOffsets.add(offset);
    final byte[] bytes = this.bytes;
    final int length = bytes.length;
    for (int i = offset; i < length; i++) {
      switch (bytes[i]) {
        case HT:
          tokenOffsets.add(i);
          tokenOffsets.add(i + 1);
          break;
        case CR:
          tokenOffsets.add(i);
          lineOffsets.add(tokenOffsets.size);
          break;
        case LF:
          i = skipExpansion(bytes, i);
          tokenOffsets.add(i + 1);
          break;
      }
    }

    return --lineOffsets.size;
  }

  private void putFieldName(String fieldName) {
    if (!fieldIds.containsKey(fieldName)) {
      fieldIds.put(fieldName, fieldNames.size);
    }

    fieldNames.add(fieldName);
  }

  @Override
  public int numFields() {
    return numFields;
  }

  public Iterable<String> fieldNames() {
    return fieldNames;
  }

  public String fieldName(int fieldId) {
    return fieldNames.get(fieldId);
  }

  @Override
  public int numRecords() {
    return numRecords;
  }

  public Iterable<String> recordNames() {
    List<String> recordNames = new ArrayList<>(numRecords());
    for (int i = 0, s = numRecords(); i < s; i++) {
      recordNames.add(recordName(i));
    }

    return recordNames;
  }

  @Override
  public String recordName(int recordId) {
    return primaryKeyFieldId == -1
        ? "" + recordId
        : _recordName(recordId).toString();
  }

  private AsciiString _recordName(int recordId) {
    return token(recordId, primaryKeyFieldId);
  }

  @Override
  public int primaryKey(String fieldName) {
    Validate.validState(primaryKeyFieldId == -1, "primary key already set");
    if (fieldName == null) return primaryKeyFieldId;
    final int fieldId = primaryKeyFieldId = fieldId(fieldName);
    final int[] tokenOffsets = this.tokenOffsets.items;
    for (int i = 0, s = numRecords(); i < s; i++) {
      final int offset = lineOffset(i, fieldId);
      String recordName = toUpper(buffer, tokenOffsets[offset], tokenOffsets[offset + 1]);
      recordIds.put(recordName, i);
    }
    return primaryKeyFieldId;
  }

  @Override
  public int primaryKey() {
    return primaryKeyFieldId;
  }

  private int lineOffset(final int recordId, final int fieldId) {
    return lineOffsets.items[recordId] + (fieldId << 1);
  }

  @Override
  public AsciiString token(final int recordId, final int fieldId) {
    final int offset = lineOffset(recordId, fieldId);
    final int[] tokenOffsets = this.tokenOffsets.items;
    return buffer.subSequence(tokenOffsets[offset], tokenOffsets[offset + 1]);
  }

  public Iterable<String> tokens(final int recordId) {
    List<String> tokens = new ArrayList<>(numFields());
    for (int i = 0, s = numFields(); i < s; i++) {
      tokens.add(TsvTranslators.escapeTsv(token(recordId, i).toString()));
    }

    return tokens;
  }

  @Override
  public int fieldId(String fieldName) {
    return fieldIds.get(fieldName.toUpperCase(), -1);
  }

  @Override
  public int recordId(String recordName) {
    return recordIds.get(recordName.toUpperCase(), -1);
  }

  /**
   * @see #parseInt
   */
  @Override
  public byte parseByte(int recordId, int fieldId) {
    return (byte) parseInt(recordId, fieldId);
  }

  /**
   * @see #parseInt
   */
  @Override
  public short parseShort(int recordId, int fieldId) {
    return (short) parseInt(recordId, fieldId);
  }

  /**
   * This is an extremely optimized implementation which does no value or param
   * checking.
   *
   * @return token at {@code (recordId, fieldId)}, otherwise {@code 0}
   */
  @Override
  public int parseInt(int recordId, int fieldId) {
    if (fieldId < 0) return 0;
    final int offset = lineOffset(recordId, fieldId);
    final int[] tokenOffsets = this.tokenOffsets.items;
    final int start = tokenOffsets[offset];
    final int end = tokenOffsets[offset + 1];
    if (start >= end) return 0;
    int i = start;
    final byte[] bytes = this.bytes;
    boolean negative = bytes[i] == '-';
    if (negative) i++;
    int result = 0;
    final int l = end - start;
    for (final int s = i + l; i < s; i++) result = (result * 10) + (bytes[i] & 0xF);
    return negative ? -result : result;
  }

  /**
   * This is an extremely optimized implementation which does no value or param
   * checking.
   *
   * @return token at {@code (recordId, fieldId)}, otherwise {@code 0}
   */
  @Override
  public long parseLong(int recordId, int fieldId) {
    if (fieldId < 0) return 0L;
    final int offset = lineOffset(recordId, fieldId);
    final int[] tokenOffsets = this.tokenOffsets.items;
    final int start = tokenOffsets[offset];
    final int end = tokenOffsets[offset + 1];
    if (start >= end) return 0;
    int i = start;
    final byte[] bytes = this.bytes;
    boolean negative = bytes[i] == '-';
    if (negative) i++;
    long result = 0L;
    final int l = end - start;
    for (final int s = i + l; i < s; i++) result = (result * 10) + (bytes[i] & 0xF);
    return negative ? -result : result;
  }

  @Override
  public boolean parseBoolean(int recordId, int fieldId) {
    final int intValue = parseInt(recordId, fieldId);
    if (CHECK_BINARY_RADIX && (intValue & 1)  != intValue) {
      log.warn("boolean exceeds binary radix at {}:{} ({}, {}): {}",
          recordId, fieldId,
          TsvTranslators.escapeTsv(recordName(recordId)),
          TsvTranslators.escapeTsv(fieldName(fieldId)),
          token(recordId, fieldId));
    }

    return intValue != 0;
  }

  @Override
  public float parseFloat(int recordId, int fieldId) {
    if (fieldId < 0) return 0f;
    return Float.parseFloat(parseString(recordId, fieldId));
  }

  @Override
  public double parseDouble(int recordId, int fieldId) {
    if (fieldId < 0) return 0d;
    return Double.parseDouble(parseString(recordId, fieldId));
  }

  @Override
  public String parseString(int recordId, int fieldId) {
    if (fieldId < 0) return null;
    final int offset = lineOffset(recordId, fieldId);
    final int[] tokenOffsets = this.tokenOffsets.items;
    return buffer.toString(tokenOffsets[offset], tokenOffsets[offset + 1]);
  }
}
