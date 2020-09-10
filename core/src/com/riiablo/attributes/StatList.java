package com.riiablo.attributes;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.math.Fixed;

public final class StatList {
  private static final Logger log = LogManager.getLogger(StatList.class);

  public static StatListRef obtain() {
    return new StatList().reset(1).buildList();
  }

  static final int MAX_LISTS = Byte.SIZE;
  static final int DEFAULT_SIZE = 32;
  static final int MAX_SIZE = 1 << Byte.SIZE;

  /** @see #encodeFlags */
  private static final int ENCODING_MASK = (1 << 3) - 1;
  private static final int FLAG_PARAMS = 1 << 3;
  private static final int FLAG_FIXED = 1 << 4;
  private static final int FLAG_LONG = 1 << 5;
  private static final int FLAG_MODIFIED = 1 << 6;

  private static final long UINT_MAX_VALUE = (1L << Integer.SIZE) - 1;

  private final byte[] offsets = new byte[index(MAX_LISTS)];
  private final short[] ids;
  private final int[] params;
  private final int[] values;
  private final byte[] flags;

  private final int maxSize;
  private int maxLists;
  private int numLists;
  private int tail;
  private byte immutable;
  private int listsMask;

  private IndexIterator INDEX_ITERATOR;
  private StatIterator STAT_ITERATOR;
  private StatListIterator STAT_LIST_ITERATOR;

  StatList() {
    this(DEFAULT_SIZE);
  }

  StatList(int maxSize) {
    assert maxSize >= 0 && maxSize <= MAX_SIZE;
    this.maxSize = maxSize;
    ids = new short[maxSize];
    params = new int[maxSize];
    values = new int[maxSize];
    flags = new byte[maxSize];
  }

  StatList reset(final int maxLists) {
    log.traceEntry("reset(maxLists: {})", maxLists);
    assertMutable();
    assert maxLists > 0 : "maxLists(" + maxLists + ") <= " + 0;
    assert maxLists <= MAX_LISTS : "maxLists(" + maxLists + ") > MAX_LISTS(" + MAX_LISTS + ")";
    this.maxLists = maxLists;
    listsMask = index(maxLists) - 1;
    clear();
    return this;
  }

  StatList truncate(final int maxLists) {
    log.traceEntry("truncate(maxLists: {})", maxLists);
    assert maxLists > 0 : "maxLists(" + maxLists + ") <= " + 0;
    assert maxLists <= this.maxLists : "maxLists(" + maxLists + ") > this.maxLists(" + this.maxLists + ")";
    for (int i = maxLists, s = this.maxLists; i < s; i++) {
      assertMutable(i); // TODO: replace with bitmask check
    }

    immutable &= (listsMask = index(maxLists) - 1);
    if (tail >= index(maxLists)) {
      tail = endingOffset(maxLists);
    }
    return this;
  }

  StatList freeze() {
    immutable = (byte) listsMask;
    return this;
  }

  StatList freeze(final int list) {
    immutable |= index(list);
    return this;
  }

  private boolean isMutable(final int list) {
    return (immutable & index(list)) == 0;
  }

  private void assertMutable() {
    if (!isMutable(listsMask)) {
      throw new UnsupportedOperationException("Stat list has been frozen");
    }
  }

  private void assertMutable(final int list) {
    if (!isMutable(list)) {
      throw new UnsupportedOperationException("Stat list(" + list + ") has been frozen");
    }
  }

  StatList forceClear() {
    immutable = 0;
    return clear();
  }

  StatList clear() {
    log.traceEntry("clear()");
    assertMutable();
    numLists = 0;
    tail = 0;
    return this;
  }

  StatList clear(final int list) {
    log.traceEntry("clear(list: {})", list);
    assertMutable(list);
    setEndingOffset(list, startingOffset(list));
    return this;
  }

  int size(final int list) {
    return endingOffset(list) - startingOffset(list);
  }

  boolean isEmpty(final int list) {
    return size(list) == 0;
  }

  int numLists() {
    return numLists;
  }

  int maxLists() {
    return maxLists;
  }

  int maxSize() {
    return maxSize;
  }

  int newList() {
    return newList(0);
  }

  int newList(final int capacity) {
    log.traceEntry("newList(capacity: {})", capacity);
    assertMutable(numLists);
    if (numLists >= maxLists) {
      throw new IndexOutOfBoundsException("Max number of lists has already been created: maxLists(" + maxLists + ")");
    }

    if (tail + capacity > maxSize) {
      throw new IndexOutOfBoundsException("capacity(" + capacity + ") would exceed maxSize(" + maxSize + ")");
    }

    final int list = numLists++;
    setEndingOffset(list, setStartingOffset(list, tail));
    ensureCapacity(list, tail, capacity);
    return list;
  }

  StatListRef buildList() {
    return buildList(0);
  }

  StatListRef buildList(final int capacity) {
    assertMutable(numLists + 1);
    return new StatListRef(this, newList(capacity));
  }

  StatListRef first() {
    return get(0);
  }

  StatListRef get(final int list) {
    if (!contains(list)) throw new IndexOutOfBoundsException("StatList does not contain list(" + list + "): numLists(" + numLists + ")");
    return new StatListRef(this, list);
  }

  boolean contains(final int list) {
    return list >= 0 && list < numLists;
  }

  boolean contains(final int list, final int index) {
    return contains(list) && index >= startingOffset(list) && index < endingOffset(list);
  }

  boolean containsAny(final int list, final short stat) {
    return contains(list) && Arrays.binarySearch(ids, startingOffset(list), endingOffset(list), stat) >= 0;
  }

  boolean containsEncoded(final int list, final short stat, final int encodedParams) {
    return contains(list) && indexOfEncoded(list, stat, encodedParams) >= 0;
  }

  boolean contains(final int list, final StatRef ref) {
    return containsEncoded(list, ref.id(), ref.encodedParams());
  }

  int putEncoded(final int list, final short stat, final int encodedParams, final int encodedValues) {
    final ItemStatCost.Entry entry = entry(stat);
    if (log.traceEnabled()) log.tracefEntry(
        "putEncoded(stat: %d (%s), encodedParams: %d (0x%3$x), encodedValues: %d (0x%4$x))",
        stat, entry, encodedParams, encodedValues);
    assertMutable(list);

    final int encoding = entry.Encode;
    if (log.warnEnabled() && !Stat.encodingSupported(encoding)) log.warn(
        "stat: {} ({}) has unsupported encoding({})", stat, entry, encoding);

    final int index = indexOfEncoded(list, stat, encodedParams);
    if (index >= 0 && equalsEncoded(index, stat, encodedParams)) {
      return setEncoded(list, index, stat, entry, encodedParams, encodedValues);
    } else {
      return insertEncodedAt(list, ~index, stat, entry, encodedParams, encodedValues);
    }
  }

  int putEncoded(final int list, final short stat, final int encodedValues) {
    assertSimple(stat);
    return putEncoded(list, stat, 0, encodedValues);
  }

  int put(
      final int list, final short stat,
      final int param0, final int param1,
      final int value0, final int value1, final int value2) {
    final ItemStatCost.Entry entry = entry(stat);
    if (log.traceEnabled()) log.traceEntry(
        "put(list: {}, stat: {}, param0: {}, param1: {}, value0: {}, value1: {}, value2: {})",
        list, stat, param0, param1, value0, value1, value2);
    final int encoding = entry.Encode;
    return putEncoded(list, stat,
        Stat.encodeParams(encoding, param0, param1),
        Stat.encodeValues(encoding, value0, value1, value2));
  }

  int put(final int list, final short stat, final int value) {
    assertSimple(stat);
    return putEncoded(list, stat, 0, Stat.encode(stat, value));
  }

  int put(final int list, final short stat, final long value) {
    assertSimple(stat);
    assert value <= UINT_MAX_VALUE : "value(" + value + ") > " + UINT_MAX_VALUE;
    return putEncoded(list, stat, 0, asInt(value));
  }

  int put(final int list, final short stat, final float value) {
    assertSimple(stat);
    return putEncoded(list, stat, 0, asInt(stat, value));
  }

  int setEncoded(final int list, final int index, final int encodedValues) {
    final short stat = ids[index];
    assertSimple(stat);
    assert equalsEncoded(index, stat, 0);
    return setEncoded(list, index, stat, entry(stat), 0, encodedValues);
  }

  int set(final int list, final int index, final int value) {
    final short stat = ids[index];
    assertSimple(stat);
    assert equalsEncoded(index, stat, 0);
    return setEncoded(list, index, stat, entry(stat), 0, Stat.encode(stat, value));
  }

  int set(final int list, final int index, final long value) {
    final short stat = ids[index];
    assertSimple(stat);
    assert value <= UINT_MAX_VALUE : "value(" + value + ") > " + UINT_MAX_VALUE;
    return setEncoded(list, index, stat, entry(stat), 0, asInt(value));
  }

  int set(final int list, final int index, final float value) {
    final short stat = ids[index];
    assertSimple(stat);
    return setEncoded(list, index, stat, entry(stat), 0, asInt(stat, value));
  }

  int set(final int list, final int index, final StatRef src) {
    assert equalsEncoded(index, src.id(), src.encodedParams());
    return setEncoded(list, index, src.id(), src.entry(), src.encodedParams(), src.encodedValues());
  }

  void setAll(final StatList src) {
    if (log.traceEnabled()) log.traceEntry("setAll(src: {})", src);
    if (maxSize < src.tail) {
      throw new IndexOutOfBoundsException("maxSize(" + maxSize + ") cannot fit src.tail(" + src.tail + ")");
    }

    assertMutable();
    clear();
    System.arraycopy(src.offsets, 0, this.offsets, 0, index(src.numLists));
    arraycopy(src, 0, this, 0, src.tail);
    maxLists = src.maxLists;
    numLists = src.numLists;
    tail = src.tail;
  }

  void setAll(final int list, final StatListRef src) {
    if (log.traceEnabled()) log.traceEntry("setAll(list: {}, src: {})", list, src);
    final int srcSize = src.size();
    if (maxSize < srcSize) {
      throw new IndexOutOfBoundsException("maxSize(" + maxSize + ") cannot fit src.size(" + srcSize + ")");
    }

    assertMutable(list);
    clear(list);
    final int startOffset = startingOffset(list);
    ensureCapacity(list, startOffset, src.size());
    final StatList srcParent = src.parent();
    arraycopy(srcParent, srcParent.startingOffset(src.list), this, startOffset, srcSize);
  }

  int addEncoded(final int list, final int index, final int encodedValues) {
    assert contains(list, index);
    assert encoding(index) <= 2 : "#add() unsupported for encoding(" + encoding(index) + ")";
    final short stat = ids[index];
    assertSimple(stat);
    assert equalsEncoded(index, stat, 0);
    if (log.traceEnabled()) log.tracefEntry(
        "addEncoded(stat: %d (%s), encodedValues: %d (0x%3$x))",
        stat, entry(index), encodedValues);
    values[index] += encodedValues;
    flags[index] |= FLAG_MODIFIED;
    if (log.debugEnabled()) log.debug(indexDebugString(index));
    return index;
  }

  int add(final int list, final int index, final int value) {
    return addEncoded(list, index, Stat.encode(ids[index], value));
  }

  int add(final int list, final int index, final long value) {
    return addEncoded(list, index, asInt(value));
  }

  int add(final int list, final int index, final float value) {
    return addEncoded(list, index, asInt(ids[index], value));
  }

  private static int asInt(final long value) {
    return (int) value;
  }

  private static int asInt(final short stat, final float value) {
    assert entry(stat).ValShift == 8 : "entry.ValShift(" + entry(stat).ValShift + ") != " + 8;
    return Fixed.floatToIntBits(value, 8);
  }

  int asInt(final int index) {
    assert Stat.numEncodedValues(encoding(index)) == 1;
    return (flags[index] & FLAG_FIXED) == FLAG_FIXED
        ? encodedValues(index) >> entry(index).ValShift
        : encodedValues(index);
  }

  long asLong(final int index) {
    assert Stat.numEncodedValues(encoding(index)) == 1;
    return (flags[index] & FLAG_FIXED) == FLAG_FIXED
        ? encodedValues(index) >> entry(index).ValShift
        : encodedValues(index);
  }

  float asFixed(final int index) {
    assert Stat.numEncodedValues(encoding(index)) == 1;
    assert entry(ids[index]).ValShift == 8 : "entry.ValShift(" + entry(ids[index]).ValShift + ") != " + 8;
    return Fixed.intBitsToFloat(encodedValues(index), 8);
  }

  String asString(final int index) {
    final byte flags = this.flags[index];
    return (flags & FLAG_FIXED) == 0
        ? (flags & FLAG_LONG) == 0
            ? String.valueOf(asLong(index))
            : String.valueOf(asInt(index))
        : String.valueOf(asFixed(index));
  }

  int value0(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return values[index];
      case 1: return values[index];
      case 2: return values[index];
      case 3: return values[index] & 0xFF;
      case 4: return values[index] & 0x3;
    }
  }

  int value1(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return 0;
      case 3: return (values[index] >>> 8) & 0xFF;
      case 4: return (values[index] >>> 2) & 0x3FF;
    }
  }

  int value2(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return 0;
      case 3: return 0;
      case 4: return (values[index] >>> 12) & 0x3FF;
    }
  }

  int param0(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return params[index];
      case 1: return params[index];
      case 2: return params[index] & 0x3F;
      case 3: return params[index] & 0x3F;
      case 4: return params[index];
    }
  }

  int param1(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return (params[index] >>> 6) & 0x3FF;
      case 3: return (params[index] >>> 6) & 0x3FF;
      case 4: return 0;
    }
  }

  String indexDebugString(final int index) {
    final byte flags = this.flags[index];
    final StringBuilder sb = new StringBuilder(32);
    if ((flags & FLAG_MODIFIED) == FLAG_MODIFIED) {
      sb.append('*');
    }

    sb.append(entry(index))
        .append('(')
        .append(ids[index])
        .append(")=");

    final int encoding = flags & ENCODING_MASK;
    switch (Stat.numEncodedParams(encoding)) {
      case 2:
        sb.append(param0(index)).append(':').append(param1(index)).append(':');
        break;
      case 1:
        sb.append(param0(index)).append(':');
        break;
    }

    switch (Stat.numEncodedValues(encoding)) {
      case 3:
        sb.append(value0(index)).append(':').append(value1(index)).append(':').append(value2(index));
        break;
      case 2:
        sb.append(value0(index)).append(':').append(value1(index));
        break;
      case 1:
        sb.append(asString(index));
        break;
    }

    return sb.toString();
  }

  String listDebugString(final int list) {
    final int startIndex = startingOffset(list);
    final int endIndex = endingOffset(list);
    return new ToStringBuilder(this)
        .append("list", list)
        .append("immutable", !isMutable(list))
        .append("size", endIndex - startIndex)
        .append("ids", '{' + StringUtils.join(ids, ',', startIndex, endIndex) + '}')
        .append("values", '{' + StringUtils.join(values, ',', startIndex, endIndex) + '}')
        .append("params", '{' + StringUtils.join(params, ',', startIndex, endIndex) + '}')
        .append("flags", '{' + StringUtils.join(flags, ',', startIndex, endIndex) + '}')
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("numLists", numLists)
        .append("maxLists", maxLists)
        .append("immutable", StringUtils.leftPad(String.valueOf(immutable), 8, '0'))
        .append("tail", tail)
        .append("maxSize", maxSize)
        .append("offsets", '{' + StringUtils.join(offsets, ',', 0, index(numLists)) + '}')
        .append("ids", '{' + StringUtils.join(ids, ',', 0, tail) + '}')
        .append("values", '{' + StringUtils.join(values, ',', 0, tail) + '}')
        .append("params", '{' + StringUtils.join(params, ',', 0, tail) + '}')
        .append("flags", '{' + StringUtils.join(flags, ',', 0, tail) + '}')
        .build();
  }

  boolean equalsEncoded(final int index, final short stat, final int encodedParams) {
    return ids[index] == stat && params[index] == encodedParams;
  }

  short id(final int index) {
    return ids[index];
  }

  ItemStatCost.Entry entry(final int index) {
    return entry(ids[index]);
  }

  static ItemStatCost.Entry entry(final short stat) {
    return Stat.entry(stat);
  }

  int encoding(final int index) {
    return flags[index] & ENCODING_MASK;
  }

  boolean modified(final int index) {
    return (flags[index] & FLAG_MODIFIED) == FLAG_MODIFIED;
  }

  void forceUnmodified(final int index) {
    flags[index] &= ~FLAG_MODIFIED;
  }

  int encodedValues(final int index) {
    return values[index];
  }

  int encodedParams(final int index) {
    return params[index];
  }

  private final void assertSimple(final short stat) {
    final int encoding = entry(stat).Encode;
    assert Stat.numEncodedParams(encoding) == 0 && Stat.numEncodedValues(encoding) == 1
        : "stat(" + stat + ") requires " + Stat.numEncodedParams(encoding) + " params and " + Stat.numEncodedValues(encoding) + " values";
  }

  private void arraycopy(final int srcIndex, final int dstIndex, final int length) {
    arraycopy(this, srcIndex, this, dstIndex, length);
  }

  private static void arraycopy(
      final StatList src, final int srcIndex,
      final StatList dst, final int dstIndex,
      final int length) {
    if (length <= 0) return;
    if (log.traceEnabled()) log.traceEntry(
        "arraycopy(src: {}, srcIndex: {}, dst: {}, dstIndex: {}, length: {})",
        src, srcIndex, dst, dstIndex, length);
    System.arraycopy(src.ids, srcIndex, dst.ids, dstIndex, length);
    System.arraycopy(src.params, srcIndex, dst.params, dstIndex, length);
    System.arraycopy(src.values, srcIndex, dst.values, dstIndex, length);
    System.arraycopy(src.flags, srcIndex, dst.flags, dstIndex, length);
  }

  private void ensureCapacity(final int list, final int index, final int capacity) {
    log.traceEntry("ensureCapacity(list: {}, index: {}, capacity: {})", list, index, capacity);
    assertMutable();
    final int endOffset = endingOffset(list);
    assert index <= endOffset : "index(" + index + ") > list.endOffset(" + endOffset + ")";
    final int shiftLength = endOffset - index;
    final int newEndOffset = endOffset + capacity;
    assert newEndOffset <= maxSize : "capacity(" + capacity + ") would exceed maxSize(" + maxSize + ")";
    final int nextStartOffset = (list + 1) < numLists ? startingOffset(list + 1) : maxSize;
    if (shiftLength > 0 && newEndOffset <= nextStartOffset) {
      arraycopy(index, index + capacity, shiftLength);
      setEndingOffset(list, newEndOffset);
      return;
    }

    final int additionalCapacity = newEndOffset - nextStartOffset;
    final int copyLength = tail - index;
    if (copyLength > 0) arraycopy(index, index + capacity, copyLength);
    if (additionalCapacity > 0) {
      tail += additionalCapacity;
      final byte[] offsets = this.offsets;
      for (int i = index(list + 1), s = index(numLists); i < s; i++) {
        offsets[i] += additionalCapacity;
      }
      assertSorted();
    } else {
      setEndingOffset(list, newEndOffset);
    }
  }

  private int setEncoded(
      final int list,
      final int index,
      final short stat,
      final ItemStatCost.Entry entry,
      final int encodedParams,
      final int encodedValue) {
    if (log.traceEnabled()) log.tracefEntry(
        "setEncoded(index: %d, stat: %d (%s), param: %d (0x%4$x), value: %d (0x%5$x))",
        index, stat, entry, encodedParams, encodedValue);
    assert isMutable(list);
    ids[index] = stat;
    params[index] = encodedParams;
    values[index] = encodedValue;
    flags[index] = encodeFlags(entry);
    if (log.debugEnabled()) log.debug(indexDebugString(index));
    return index;
  }

  private int insertEncodedAt(
      final int list,
      final int index,
      final short stat,
      final ItemStatCost.Entry entry,
      final int encodedParams,
      final int encodedValue) {
    if (log.traceEnabled()) log.tracefEntry(
        "insertEncodedAt(index: %d, stat: %d (%s), param: %d (0x%4$x), value: %d (0x%5$x))",
        index, stat, entry, encodedParams, encodedValue);
    assert isMutable(list);
    ensureCapacity(list, index, 1);
    setEncoded(list, index, stat, entry, encodedParams, encodedValue);
    if (log.traceEnabled()) log.trace(listDebugString(list));
    return index;
  }

  private static byte encodeFlags(final ItemStatCost.Entry entry) {
    byte flags = (byte) (entry.Encode & ENCODING_MASK);
    if (entry.Save_Param_Bits > 0) flags |= FLAG_PARAMS;
    if (Stat.numEncodedParams(entry.Encode) > 0) flags |= FLAG_PARAMS;
    if (entry.Send_Bits >= Integer.SIZE) flags |= FLAG_LONG;
    if (entry.ValShift > 0) flags |= FLAG_FIXED;
    return flags;
  }

  int indexOfEncoded(final int list, final short stat, final int encodedParams) {
    return indexOfEncoded(list, stat, encodedParams, false);
  }

  int indexOfEncoded(final int list, final short stat, final int encodedParams, boolean first) {
    final int listStart = startingOffset(list);
    final int listEnd = endingOffset(list);
    final int index = Arrays.binarySearch(ids, listStart, listEnd, stat);
    if (index >= 0) {
      final int startIndex = firstIndexOf(stat, index, listStart);
      if (first) return startIndex;
      final int endIndex = lastIndexOf(stat, index, listEnd);
      return Arrays.binarySearch(params, startIndex, endIndex, encodedParams);
    } else {
      return index;
    }
  }

  int indexOf(final int list, final short stat) {
    assertSimple(stat);
    return indexOfEncoded(list, stat, 0);
  }

  int indexOf(final int list, final StatRef ref) {
    return indexOfEncoded(list, ref.id(), ref.encodedParams());
  }

  private int firstIndexOf(final short stat, final int startIndex, final int listStart) {
    int i = startIndex - 1;
    final short[] ids = this.ids;
    while (i >= listStart && ids[i] == stat) i--;
    return i + 1;
  }

  private int lastIndexOf(final short stat, final int startIndex, final int listEnd) {
    int i = startIndex + 1;
    final short[] ids = this.ids;
    for (final int s = listEnd; i < s && ids[i] == stat; i++);
    return i;
  }

  int firstIndexOf(final int list, final short stat) {
    return indexOfEncoded(list, stat, 0, true);
  }

  private static int index(final int list) {
    return list << 1;
  }

  private int startingOffset(final int list) {
    return offsets[index(list)] & 0xFF;
  }

  private int setStartingOffset(final int list, final int index) {
    offsets[index(list)] = (byte) index;
    assertSorted(-1);
    return index;
  }

  private int endingOffset(final int list) {
    return offsets[index(list) + 1] & 0xFF;
  }

  private int setEndingOffset(final int list, final int index) {
    offsets[index(list) + 1] = (byte) index;
    if (list + 1 >= numLists) tail = index;
    assertSorted();
    return index;
  }

  private void assertSorted() {
    assertSorted(0);
  }

  private void assertSorted(final int offset) {
    assert isSorted(offset) :
        "offsets({" + StringUtils.join(offsets, ',', 0, index(numLists) + offset) + "}) "
            + "tail(" + tail + ") contains property lists that are out of order";
  }

  private boolean isSorted(final int offset) {
    final int slicesLength = index(numLists) + offset;
    final byte[] offsets = this.offsets;
    int previous = offsets[0] & 0xFF;
    for (int i = 1; i < slicesLength; i++) {
      final int current = offsets[i] & 0xFF;
      if (previous > current) return false;
      previous = current;
    }
    return tail == previous;
  }

  public IndexIterator indexIterator(final int list) {
    return INDEX_ITERATOR == null
        ? INDEX_ITERATOR = new IndexIterator().reset(list)
        : INDEX_ITERATOR.reset(list);
  }

  public final class IndexIterator {
    int index;
    int startIndex;
    int endIndex;

    IndexIterator reset(final int list) {
      index = startIndex = startingOffset(list);
      endIndex = endingOffset(list);
      return this;
    }

    public boolean hasNext() {
      return index < endIndex;
    }

    public int next() {
      return index++;
    }

    int pushback(final int count) {
      index -= count;
      if (index < startIndex) {
        log.warn("index({}) < startIndex({})", index, startIndex);
        index = startIndex;
      }

      return index;
    }
  }

  public StatIterator statIterator(final int list) {
    return STAT_ITERATOR == null
        ? STAT_ITERATOR = new StatIterator().reset(list)
        : STAT_ITERATOR.reset(list);
  }

  public final class StatIterator implements Iterator<StatRef> {
    final StatRef stat = new StatRef(StatList.this);
    int list; /** used for {@link #pushback} */
    int head; /** used for {@link #pushback} */
    int index;
    int endIndex;

    StatIterator reset(final int list) {
      this.list = list;
      stat.reset(list);
      head = index = startingOffset(list);
      endIndex = endingOffset(list);
      return this;
    }

    @Override
    public boolean hasNext() {
      return index < endIndex;
    }

    @Override
    public StatRef next() {
      return stat.update(index++);
    }

    /**
     * Recycles the previously read stat by re-adding it onto a new list being
     * formed at the start of this list while iteration is occurring. This
     * method effectively breaks the old list and shrinks it to a subset of
     * entries.
     */
    void pushback() {
      assert head < index : "head(" + head + ") cannot pass index(" + index + ")";
      StatList.this.setEncoded(list, head++, stat.id(), stat.entry(), stat.encodedParams(), stat.encodedValues());
      setEndingOffset(list, head);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public StatListIterator listIterator() {
    return STAT_LIST_ITERATOR == null
        ? STAT_LIST_ITERATOR = new StatListIterator().reset()
        : STAT_LIST_ITERATOR.reset();
  }

  public final class StatListIterator implements Iterator<StatListRef>, Iterable<StatListRef> {
    int list = 0;

    StatListIterator reset() {
      list = 0;
      return this;
    }

    @Override
    public Iterator<StatListRef> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      return list < numLists;
    }

    @Override
    public StatListRef next() {
      return get(list++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
