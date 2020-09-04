package com.riiablo.attributes;

import io.netty.buffer.ByteBufUtil;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class StatList {
  private static final Logger log = LogManager.getLogger(StatList.class);

  public static StatList obtain() {
    return new StatList();
  }

  public static StatList obtain(int maxLists) {
    return new StatList(maxLists);
  }

  private static final StatList EMPTY_LIST = new StatList().freeze();
  public static StatList emptyList() {
    return EMPTY_LIST;
  }

  private static final int MAX_LISTS = 8;
  private static final int MAX_STATS = 32;

  /** @see #encodeFlags */
  private static final int ENCODING_MASK = (1 << 3) - 1;
  private static final int FLAG_PARAMS = 1 << 3;
  private static final int FLAG_FIXED = 1 << 4;
  private static final int FLAG_LONG = 1 << 5;

  private static final long UINT_MAX_VALUE = (1L << Integer.SIZE) - 1;

  private final byte[] offsets;
  private final short[] ids;
  private final int[] params;
  private final int[] values;
  private final byte[] flags;

  private int maxLists;
  private int size;
  private int tail;
  private int numLists;
  private boolean immutable;

  private IndexIterator INDEX_ITERATOR;
  private StatIterator STAT_ITERATOR;

  StatList() {
    this(MAX_LISTS);
  }

  StatList(int maxLists) {
    log.traceEntry("StatList(maxLists: {})", maxLists);
    assert maxLists > 0 : "maxLists(" + maxLists + ") <= " + 0;
    this.maxLists = maxLists;
    offsets = new byte[maxLists << 1];
    ids = new short[MAX_STATS];
    params = new int[MAX_STATS];
    values = new int[MAX_STATS];
    flags = new byte[MAX_STATS];
  }

  public StatList freeze() {
    immutable = true;
    return this;
  }

  public StatList clear() {
    assertMutable();
    size = 0;
    tail = 0;
    numLists = 0;
    return this;
  }

  StatList forceClear() {
    immutable = false;
    return clear();
  }

  public int size() {
    return size;
  }

  public int size(int list) {
    return endingOffset(list) - startingOffset(list);
  }

  public boolean isEmpty(int list) {
    return size(list) == 0;
  }

  public int numLists() {
    return numLists;
  }

  public int maxLists() {
    return maxLists;
  }

  public void clearList(int list) {
    assertMutable();
    size -= size(list);
    final int offset = list << 1;
    final byte[] offsets = this.offsets;
    offsets[offset + 1] = offsets[offset];
  }

  public int newList(int capacity) {
    assertMutable();
    if (numLists >= maxLists) { // should never exceed maxLists
      throw new AssertionError("numLists(" + numLists + ") >= maxLists(" + maxLists + ")");
    }

    if (tail + capacity >= MAX_STATS) {
      throw new IllegalArgumentException("capacity(" + capacity + ") would exceed MAX_STATS(" + MAX_STATS + ")");
    }

    final int list = numLists;
    final int offset = list << 1;
    final byte[] offsets = this.offsets;
    offsets[offset] = offsets[offset + 1] = (byte) tail;
    numLists++;
    ensureCapacity(list, capacity);
    return list;
  }

  public StatListBuilder buildList() {
    return buildList(0);
  }

  public StatListBuilder buildList(int capacity) {
    assertMutable();
    return new StatListBuilder(this, newList(capacity));
  }

  public void ensureCapacity(int list, int capacity) {
    assertMutable();
    final int startOffset = startingOffset(list);
    final int endOffset = endingOffset(list);
    final int nextStartOffset = (list + 1) < numLists ? startingOffset(list + 1) : MAX_STATS;
    if (nextStartOffset - startOffset > capacity) {
      if (tail == endOffset) tail += capacity;
      return;
    }

    final int additionalCapacity = capacity - (nextStartOffset - endOffset);
    if (tail + additionalCapacity >= MAX_STATS) {
      throw new IllegalArgumentException("capacity(" + capacity + ") would exceed MAX_STATS(" + MAX_STATS + ")");
    }

    final int dstOffset = nextStartOffset + additionalCapacity;
    final int length = tail - nextStartOffset;
    System.arraycopy(ids, nextStartOffset, ids, dstOffset, length);
    System.arraycopy(params, nextStartOffset, params, dstOffset, length);
    System.arraycopy(values, nextStartOffset, values, dstOffset, length);
    System.arraycopy(flags, nextStartOffset, flags, dstOffset, length);
    tail += additionalCapacity;

    final byte[] offsets = this.offsets;
    final int listsSize = numLists << 1;
    for (int i = ((list + 1) << 1); i < listsSize; i++) offsets[i] += additionalCapacity;
    assert isSorted(offsets, 0, listsSize) : "offsets(" + ByteBufUtil.hexDump(offsets, 0, listsSize) + ") contains property lists that are out of order";
  }

  public StatList copy(int list, StatList src, int srcList) {
    assertMutable();
    final int length = src.size(srcList);
    ensureCapacity(list, length);

    final int srcListStart = src.startingOffset(srcList);
    final int dstListStart = this.startingOffset(list);
    System.arraycopy(src.ids, srcListStart, this.ids, dstListStart, length);
    System.arraycopy(src.params, srcListStart, this.params, dstListStart, length);
    System.arraycopy(src.values, srcListStart, this.values, dstListStart, length);
    System.arraycopy(src.flags, srcListStart, this.flags, dstListStart, length);
    size += length;

    final byte[] offsets = this.offsets;
    offsets[(list << 1) + 1] += length;
    assert isSorted(offsets, 0, numLists << 1) : "offsets(" + ByteBufUtil.hexDump(offsets, 0, numLists << 1) + ") contains property lists that are out of order";
    return this;
  }

  public StatList set(int list, StatList src, int srcList) {
    assertMutable();
    clearList(list);
    copy(list, src, srcList);
    return this;
  }

  public StatList setAll(StatList src) {
    assertMutable();
    clear();
    final int length = src.tail;
    System.arraycopy(src.offsets, 0, this.offsets, 0, src.numLists() << 1);
    System.arraycopy(src.ids, 0, this.ids, 0, length);
    System.arraycopy(src.params, 0, this.params, 0, length);
    System.arraycopy(src.values, 0, this.values, 0, length);
    System.arraycopy(src.flags, 0, this.flags, 0, length);
    size = src.size;
    tail = src.tail;
    numLists = src.numLists;
    return this;
  }

  public StatListGetter get(int list) {
    if (list >= numLists) throw new IllegalStateException("list(" + list + ") >= numLists(" + numLists + ")");
    return new StatListGetter(this, list);
  }

  public StatListGetter first() {
    return get(0);
  }

  public int add(int index, int value) {
    assertMutable();
    log.traceEntry("add(index: {}, value: {})", index, value);
    final short stat = ids[index];
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert encoding(stat) <= 2 : "#add() unsupported for encoding(" + encoding(stat) + ")";
    if (log.debugEnabled()) log.debug(
        "add(stat: {} ({}), this: {}, src: {})",
        stat, entry(stat), asString(index), _asString(index, value));
    return values[index] += value;
  }

  public int add(int index, long value) {
    return add(index, _asInt(value));
  }

  public int add(int index, float value) {
    return add(index, _asInt(value));
  }

  public int add(int list, short stat, StatList src, int srcList) {
    assertMutable();
    log.traceEntry("add(list: {}, stat: {}, src: {}, srcList: {})", list, stat, src, srcList);
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert encoding(stat) <= 2 : "#add() unsupported for encoding(" + encoding(stat) + ")";
    final int index = indexOf(list, stat, 0);
    final int otherIndex = src.indexOf(srcList, stat, 0);
    if (log.debugEnabled()) log.debug(
        "add(stat: {} ({}), this: {}, src: {})",
        stat, entry(stat), index >= 0 ? asString(index) : "null", src.asString(otherIndex));
    if (index >= 0) {
      values[index] += src.values[otherIndex];
      return index;
    } else {
      /** TODO: possibility to speed this up with {@link #insertAt} */
      final int putIndex = put(list, stat, src.values[otherIndex]);
      assert putIndex == ~index : "index(" + putIndex + ") != expected index(" + ~index + ")";
      return putIndex;
    }
    // max
  }

  public int add(int list, StatGetter stat) {
    final int index = indexOf(list, stat.id(), stat.param());
    return add(index, stat.value());
  }

  public int addAll(int list, StatList src, int srcList) {
    assertMutable();
    final int srcStartOffset = src.startingOffset(srcList);
    final int srcEndOffset = src.endingOffset(srcList);
    int index = -1;
    for (int i = srcStartOffset, s = srcEndOffset; i < s; i++) {
      final short stat = src.id(i);
      index = add(list, stat, src, srcList);
    }

    return index;
  }

  /**
   * @deprecated undefined behavior -- stat operations should be reversible,
   *             i.e., subtracting a stat to it's default should remove it.
   *             Validate that a value of 0 indicates default for all stats.
   *             Maybe some stats allow 0 value and nonzero param?
   */
  @Deprecated
  public boolean sub(int list, short stat, StatList src, int srcList) {
    if (true) throw new UnsupportedOperationException();
    assertMutable();
    log.traceEntry("sub(list: {}, stat: {}, src: {}, srcList: {})", list, stat, src, srcList);
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert encoding(stat) <= 2 : "#sub() unsupported for encoding(" + encoding(stat) + ")";
    final int index = indexOf(list, stat, 0);
    final int otherIndex = src.indexOf(srcList, stat, 0);
    if (log.debugEnabled()) log.debug(
        "sub(stat: {} ({}), this: {}, src: {})",
        stat, entry(stat), index >= 0 ? asString(index) : "null", src.asString(otherIndex));
    assert index >= 0 : "property list does not contain stat(" + stat + ")";
    if (index < 0) return false; // indefined behavior
    values[index] -= src.values[otherIndex];
    // TODO: if values[index] <= 0, remove it? is <= 0 appropriate
    return true;
    // min
  }

  /** @deprecated see {@link #sub} */
  @Deprecated
  public void subAll(int list, StatList src, int srcList) {
    assertMutable();
    final int srcStartOffset = src.startingOffset(srcList);
    final int srcEndOffset = src.endingOffset(srcList);
    for (int i = srcStartOffset, s = srcEndOffset; i < s; i++) {
      final short stat = src.id(i);
      sub(list, stat, src, srcList);
    }
  }

  public int put(int list, short stat, int param, int value) {
    assertMutable();
    final ItemStatCost.Entry entry = entry(stat);
    final int encoding = entry.Encode;
    if (log.traceEnabled()) log.tracefEntry(
        "put(stat: %d (%s), param: %d (0x%3$x), value: %d (0x%4$x))", stat, entry, param, value);
    if (log.warnEnabled() && (encoding < 0 || encoding > 4)) log.warn(
        "stat: {} ({}) has unsupported encoding({})", stat, entry, encoding);

    final int index = indexOf(list, stat, param);
    if (index >= 0) {
      set(index, stat, param, value, entry);
      if (log.debugEnabled()) log.debug(indexDebugString(index));
      return index;
    } else {
      insertAt(list, ~index, stat, param, value, entry);
      if (log.debugEnabled()) log.debug(indexDebugString(~index));
      return ~index;
    }
  }

  public int put(int list, short stat, int param1, int param2, int value1, int value2, int value3) {
    assertMutable();
    final int encoding = entry(stat).Encode;
    return put(
        list,
        stat,
        Stat.encodeParam(encoding, param1, param2),
        Stat.encodeValue(encoding, value1, value2, value3));
  }

  public int put(int list, short stat, int value) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    return put(list, stat, 0, value);
  }

  public int put(int list, short stat, long value) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert value <= UINT_MAX_VALUE : "value(" + value + ") > " + UINT_MAX_VALUE;
    return put(list, stat, 0, _asInt(value));
  }

  public int put(int list, short stat, float value) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert entry(stat).ValShift == 8 : "entry.ValShift(" + entry(stat).ValShift + ") != " + 8;
    return put(list, stat, 0, _asInt(value));
  }

  public boolean contains(int list) {
    return list >= 0 && list < numLists;
  }

  public boolean contains(int list, short stat) {
    return Arrays.binarySearch(ids, startingOffset(list), endingOffset(list), stat) >= 0;
  }

  public boolean contains(int list, short stat, int param) {
    return indexOf(list, stat, param) >= 0;
  }

  public int indexOf(int list, short stat, int param) {
    final int listStart = startingOffset(list);
    final int listEnd = endingOffset(list);
    final int index = Arrays.binarySearch(ids, listStart, listEnd, stat);
    if (index >= 0) {
      final int startIndex = firstIndexOf(stat, index, listStart);
      final int endIndex = lastIndexOf(stat, index, listEnd);
      return Arrays.binarySearch(params, startIndex, endIndex, param);
    } else {
      return index;
    }
  }

  public int indexOf(int list, short stat) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    return indexOf(list, stat, 0);
  }

  int firstIndexOf(int list, short stat) {
    final int listStart = startingOffset(list);
    final int listEnd = endingOffset(list);
    final int index = Arrays.binarySearch(ids, listStart, listEnd, stat);
    if (index >= 0) {
      return firstIndexOf(stat, index, listStart);
    } else {
      return index;
    }
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

  int asInt(int index) {
    return values[index];
  }

  private static int _asInt(long value) {
    return (int) value;
  }

  private static long _asLong(int value) {
    return value & UINT_MAX_VALUE;
  }

  long asLong(int index) {
    assert entry(index).Send_Bits >= Integer.SIZE : "entry.Send_Bits(" + entry(index).Send_Bits + ") < " + Integer.SIZE;
    return _asLong(values[index]);
  }

  private static int _asInt(float value) {
    return Fixed.floatToIntBits(value, 8);
  }

  private static float _asFixed(int value) {
    return Fixed.intBitsToFloat(value, 8);
  }

  float asFixed(int index) {
    assert entry(index).ValShift == 8 : "entry.ValShift(" + entry(index).ValShift + ") != " + 8;
    return _asFixed(values[index]);
  }

  private String _asString(int index, int value) {
    final byte flags = this.flags[index];
    return (flags & FLAG_FIXED) == 0
        ? (flags & FLAG_LONG) == 0
            ? String.valueOf(value)
            : String.valueOf(_asLong(value))
        : String.valueOf(_asFixed(value));
  }

  String asString(int index) {
    final byte flags = this.flags[index];
    return (flags & FLAG_FIXED) == 0
        ? (flags & FLAG_LONG) == 0
            ? String.valueOf(asInt(index))
            : String.valueOf(asLong(index))
        : String.valueOf(asFixed(index));
  }

  public short id(int index) {
    return ids[index];
  }

  ItemStatCost.Entry entry(int index) {
    return entry(ids[index]);
  }

  ItemStatCost.Entry entry(short stat) {
    return Stat.entry(stat);
  }

  public int encoding(int index) {
    return flags[index] & ENCODING_MASK;
  }

  byte flags(int index) {
    return flags[index];
  }

  int value(int index) {
    return values[index];
  }

  int param(int index) {
    return params[index];
  }

  public int value1(int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return values[index];
      case 1: return values[index];
      case 2: return values[index];
      case 3: return values[index] & 0xFF;
      case 4: return values[index] & 0x3;
    }
  }

  public int value2(int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return 0;
      case 3: return (values[index] >>> 8) & 0xFF;
      case 4: return (values[index] >>> 2) & 0x3FF;
    }
  }

  public int value3(int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return 0;
      case 3: return 0;
      case 4: return (values[index] >>> 12) & 0x3FF;
    }
  }

  public int param1(int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return params[index];
      case 1: return params[index];
      case 2: return params[index] & 0x3F;
      case 3: return params[index] & 0x3F;
      case 4: return params[index];
    }
  }

  public int param2(int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return (params[index] >>> 6) & 0x3FF;
      case 3: return (params[index] >>> 6) & 0x3FF;
      case 4: return 0;
    }
  }

  private int startingOffset(int list) {
    return offsets[(list << 1)] & 0xFF;
  }

  private int endingOffset(int list) {
    return offsets[(list << 1) + 1] & 0xFF;
  }

  private static boolean isSorted(final byte[] array, final int startIndex, final int endIndex) {
    if (endIndex - startIndex < 2) return true;
    byte previous = array[startIndex];
    for (int i = startIndex + 1; i < endIndex; i++) {
      final byte current = array[i];
      if (NumberUtils.compare(previous, current) > 0) {
        return false;
      }
      previous = current;
    }
    return true;
  }

  private static byte encodeFlags(ItemStatCost.Entry entry) {
    byte flags = (byte) (entry.Encode & ENCODING_MASK);
    if (entry.Save_Param_Bits > 0) flags |= FLAG_PARAMS;
    if (entry.Send_Bits >= Integer.SIZE) flags |= FLAG_LONG;
    if (entry.ValShift > 0) flags |= FLAG_FIXED;
    return flags;
  }

  private void set(int index, short stat, int param, int value, ItemStatCost.Entry entry) {
    if (log.traceEnabled()) log.tracefEntry(
        "set(index: %d, stat: %d (%s), param: %d (0x%4$x), value: %d (0x%5$x))", index, stat, entry, param, value);
    ids[index] = stat;
    params[index] = param;
    values[index] = value;
    flags[index] = encodeFlags(entry);
  }

  private void insertAt(int list, int index, short stat, int param, int value, ItemStatCost.Entry entry) {
    if (log.traceEnabled()) log.tracefEntry(
        "insertAt(index: %d, stat: %d (%s), param: %d (0x%4$x), value: %d (0x%5$x))", index, stat, entry, param, value);

    if (size >= MAX_STATS) {
      log.warn("stat(" + stat + ") cannot be inserted, property list is full!");
      return;
    }

    ensureCapacity(list, 1);
    set(index, stat, param, value, entry);
    size++;

    final byte[] offsets = this.offsets;
    offsets[(list << 1) + 1]++;
    final int listsSize = numLists << 1;
    for (int i = ((list + 1) << 1); i < listsSize; i++) offsets[i]++;
    assert isSorted(offsets, 0, listsSize) : "offsets(" + Arrays.toString(offsets) + ") contains property lists that are out of order";
  }

  private void assertMutable() {
    if (immutable) throw new UnsupportedOperationException("Stat list has been frozen");
  }

  public String indexDebugString(int index) {
    final byte flags = this.flags[index];
    switch (flags & ENCODING_MASK) {
      default: // fall-through
      case 0: return entry(index) + "(" + ids[index] + ")=" + ((flags & FLAG_PARAMS) == 0 ? asString(index) : (asString(index) + ":" + params[index]));
      case 1: return entry(index) + "(" + ids[index] + ")=" + param1(index) + ":" + value1(index);
      case 2: return entry(index) + "(" + ids[index] + ")=" + param1(index) + ":" + param2(index) + ":" + value1(index);
      case 3: return entry(index) + "(" + ids[index] + ")=" + param1(index) + ":" + param2(index) + ":" + value1(index) + ":" + value2(index);
      case 4: return entry(index) + "(" + ids[index] + ")=" + value1(index) + ":" + value2(index) + ":" + value3(index);
    }
  }

  public String listDebugString(int list) {
    final int startIndex = startingOffset(list);
    final int endIndex = endingOffset(list);
    return new ToStringBuilder(this)
        .append("immutable", immutable)
        .append("offsets", '{' + StringUtils.join(offsets, ',', (list << 1), (list << 1) + 2) + '}')
        .append("ids", '{' + StringUtils.join(ids, ',', startIndex, endIndex) + '}')
        .append("values", '{' + StringUtils.join(values, ',', startIndex, endIndex) + '}')
        .append("params", '{' + StringUtils.join(params, ',', startIndex, endIndex) + '}')
        .append("flags", '{' + StringUtils.join(flags, ',', startIndex, endIndex) + '}')
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("immutable", immutable)
        .append("offsets", '{' + StringUtils.join(offsets, ',', 0, numLists << 1) + '}')
        .append("ids", '{' + StringUtils.join(ids, ',', 0, tail) + '}')
        .append("values", '{' + StringUtils.join(values, ',', 0, tail) + '}')
        .append("params", '{' + StringUtils.join(params, ',', 0, tail) + '}')
        .append("flags", '{' + StringUtils.join(flags, ',', 0, tail) + '}')
        .build();
  }

  public IndexIterator indexIterator(int list) {
    return INDEX_ITERATOR == null
        ? INDEX_ITERATOR = new IndexIterator().reset(list)
        : INDEX_ITERATOR.reset(list);
  }

  public final class IndexIterator {
    int index;
    int endIndex;

    IndexIterator reset(int list) {
      index = startingOffset(list);
      endIndex = endingOffset(list);
      return this;
    }

    public boolean hasNext() {
      return index < endIndex;
    }

    public int next() {
      return index++;
    }
  }

  public StatIterator statIterator(int list) {
    return STAT_ITERATOR == null
        ? STAT_ITERATOR = new StatIterator().reset(list)
        : STAT_ITERATOR.reset(list);
  }

  public final class StatIterator implements Iterator<StatGetter> {
    final StatGetter stat = new StatGetter(StatList.this);
    int index;
    int endIndex;

    StatIterator reset(int list) {
      index = startingOffset(list);
      endIndex = endingOffset(list);
      return this;
    }

    @Override
    public boolean hasNext() {
      return index < endIndex;
    }

    @Override
    public StatGetter next() {
      return stat.update(index++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
