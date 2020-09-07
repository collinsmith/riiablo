package com.riiablo.attributes;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class StatList {
  private static final Logger log = LogManager.getLogger(StatList.class);

  public static StatList obtain() {
    return obtainUnpooled(DEFAULT_SIZE, MAX_LISTS);
  }

  public static StatList obtainLarge() {
    return obtainUnpooled(MAX_SIZE, 1);
  }

  public static StatList obtainUnpooled(int maxSize, int maxLists) {
    return new StatList(maxSize).reset(maxLists);
  }

  private static final StatList EMPTY_LIST = new StatList().freeze();
  public static StatList emptyList() {
    return EMPTY_LIST;
  }

  static final int MAX_LISTS = 8;
  static final int DEFAULT_SIZE = 32;
  static final int MAX_SIZE = 1 << Byte.SIZE;

  /** @see #encodeFlags */
  private static final int ENCODING_MASK = (1 << 3) - 1;
  private static final int FLAG_PARAMS = 1 << 3;
  private static final int FLAG_FIXED = 1 << 4;
  private static final int FLAG_LONG = 1 << 5;

  private static final long UINT_MAX_VALUE = (1L << Integer.SIZE) - 1;

  private final byte[] offsets = new byte[MAX_LISTS << 1];
  private final short[] ids;
  private final int[] params;
  private final int[] values;
  private final byte[] flags;

  private final int maxSize;
  private int maxLists;
  private int numLists;
  private int size;
  private int tail;
  private boolean immutable;

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

  public StatList reset(final int maxLists) {
    log.traceEntry("reset(maxLists: {})", maxLists);
    assertMutable();
    assert maxLists > 0 : "maxLists(" + maxLists + ") <= " + 0;
    assert maxLists <= MAX_LISTS : "maxLists(" + maxLists + ") > MAX_LISTS(" + MAX_LISTS + ")";
    this.maxLists = maxLists;
    clear();
    return this;
  }

  public StatList freeze() {
    immutable = true;
    return this;
  }

  public StatList clear() {
    log.traceEntry("clear()");
    assertMutable();
    numLists = 0;
    size = 0;
    tail = 0;
    return this;
  }

  StatList forceClear() {
    immutable = false;
    return clear();
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int numLists() {
    return numLists;
  }

  public int maxLists() {
    return maxLists;
  }

  public void clearList(final int list) {
    log.traceEntry("clearList(list: {})", list);
    assertMutable();
    size -= size(list);
    setEndingOffset(list, startingOffset(list));
  }

  public int newList() {
    return newList(0);
  }

  public int newList(final int capacity) {
    log.traceEntry("newList(capacity: {})", capacity);
    assertMutable();
    if (numLists + 1 > maxLists) {
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

  public StatListBuilder buildList() {
    return buildList(0);
  }

  public StatListBuilder buildList(final int capacity) {
    assertMutable();
    return new StatListBuilder(this, newList(capacity));
  }

  private void arraycopy(final int srcIndex, final int dstIndex, final int length) {
    arraycopy(this, srcIndex, this, dstIndex, length);
  }

  private static void arraycopy(
      final StatList src, final int srcIndex,
      final StatList dst, final int dstIndex,
      final int length) {
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
    arraycopy(index, index + capacity, tail - index);
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

  public StatList setAll(final StatList src) {
    if (log.traceEnabled()) log.traceEntry("setAll(src: {})", src);
    assertMutable();
    clear();
    System.arraycopy(src.offsets, 0, this.offsets, 0, src.numLists << 1);
    arraycopy(src, 0, this, 0, src.tail);
    maxLists = src.maxLists;
    numLists = src.numLists;
    size = src.size;
    tail = src.tail;
    return this;
  }

  public StatListGetter get(final int list) {
    if (list >= numLists) throw new IndexOutOfBoundsException("list(" + list + ") >= numLists(" + numLists + ")");
    return new StatListGetter(this, list);
  }

  public StatListGetter first() {
    return get(0);
  }

  public int add(final int index, final int value) {
    log.traceEntry("add(index: {}, value: {})", index, value);
    assertMutable();
    final short stat = ids[index];
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert encoding(stat) <= 2 : "#add() unsupported for encoding(" + encoding(stat) + ")";
    if (log.traceEnabled()) log.trace(
        "add(stat: {} ({}), this: {}, src: {})",
        stat, entry(stat), asString(index), _asString(index, value));
    final int result = values[index] += value;
    if (log.debugEnabled()) log.debug(indexDebugString(index));
    return result;
  }

  public int add(final int index, final long value) {
    return add(index, _asInt(value));
  }

  public int add(final int index, final float value) {
    return add(index, _asInt(value));
  }

  public int add(final int list, final short stat, final StatList src, final int srcList) {
    if (log.traceEnabled()) log.traceEntry("add(list: {}, stat: {}, src: {}, srcList: {})", list, stat, src, srcList);
    assertMutable();
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert encoding(stat) <= 2 : "#add() unsupported for encoding(" + encoding(stat) + ")";
    final int index = indexOf(list, stat, 0);
    final int otherIndex = src.indexOf(srcList, stat, 0);
    if (log.traceEnabled()) log.trace(
        "add(stat: {} ({}), this: {}, src: {})",
        stat, entry(stat), index >= 0 ? asString(index) : "null", src.asString(otherIndex));
    if (index >= 0 && ids[index] == stat) {
      values[index] += src.values[otherIndex];
      if (log.debugEnabled()) log.debug(indexDebugString(index));
      return index;
    } else {
      /** TODO: possibility to speed this up with {@link #insertAt} */
      final int putIndex = put(list, stat, src.values[otherIndex]);
      assert putIndex == ~index : "index(" + putIndex + ") != expected index(" + ~index + ")";
      return putIndex;
    }
    // max
  }

  public int add(final int list, final StatGetter src) {
    if (log.traceEnabled()) log.traceEntry("add(list: {}, src: {})", list, src);
    assertMutable();
    final short stat = src.id();
    final int param = src.param();
    final int index = indexOf(list, stat, param);
    if (log.traceEnabled()) log.trace(
        "add(stat: {} ({}), this: {}, src: {})",
        stat, entry(stat), index >= 0 ? asString(index) : "null", src.debugString());
    if (index >= 0 && ids[index] == stat) {
      values[index] += src.value();
      if (log.debugEnabled()) log.debug(indexDebugString(index));
      return index;
    } else {
      /** TODO: possibility to speed this up with {@link #insertAt} */
      final int putIndex = put(list, stat, param, src.value());
      assert putIndex == ~index : "index(" + putIndex + ") != expected index(" + ~index + ")";
      return putIndex;
    }
    // max
  }

  public int addAll(final int list, final StatList src, final int srcList) {
    if (log.traceEnabled()) log.traceEntry("addAll(list: {}, src: {}, srcList: {})", list, src, srcList);
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
  public boolean sub(final int list, final short stat, final StatList src, final int srcList) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated see {@link #sub} */
  @Deprecated
  public void subAll(final int list, final StatList src, final int srcList) {
    throw new UnsupportedOperationException();
  }

  public int put(final int list, final short stat, final int param, final int value) {
    final ItemStatCost.Entry entry = entry(stat);
    final int encoding = entry.Encode;
    if (log.traceEnabled()) log.tracefEntry(
        "put(stat: %d (%s), param: %d (0x%3$x), value: %d (0x%4$x))", stat, entry, param, value);
    assertMutable();
    if (log.warnEnabled() && (encoding < 0 || encoding > 4)) log.warn(
        "stat: {} ({}) has unsupported encoding({})", stat, entry, encoding);

    final int index = indexOf(list, stat, param);
    if (index >= 0 && ids[index] == stat && params[index] == param) {
      set(index, stat, param, value, entry);
      return index;
    } else {
      insertAt(list, ~index, stat, param, value, entry);
      return ~index;
    }
  }

  public int put(
      final int list,
      final short stat,
      final int param1, final int param2,
      final int value1, final int value2, final int value3) {
    assertMutable();
    final int encoding = entry(stat).Encode;
    return put(
        list, stat,
        Stat.encodeParam(encoding, param1, param2),
        Stat.encodeValue(encoding, value1, value2, value3));
  }

  public int put(final int list, final short stat, final int value) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    return put(list, stat, 0, value);
  }

  public int put(final int list, final short stat, final long value) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert value <= UINT_MAX_VALUE : "value(" + value + ") > " + UINT_MAX_VALUE;
    return put(list, stat, 0, _asInt(value));
  }

  public int put(final int list, final short stat, final float value) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    assert entry(stat).ValShift == 8 : "entry.ValShift(" + entry(stat).ValShift + ") != " + 8;
    return put(list, stat, 0, _asInt(value));
  }

  public boolean contains(final int list) {
    return list >= 0 && list < numLists;
  }

  public boolean contains(final int list, final short stat) {
    return Arrays.binarySearch(ids, startingOffset(list), endingOffset(list), stat) >= 0;
  }

  public boolean contains(final int list, final short stat, final int param) {
    return indexOf(list, stat, param) >= 0;
  }

  public int indexOf(final int list, final short stat, final int param) {
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

  public int indexOf(final int list, final short stat) {
    assert !Stat.hasParams(stat) : "stat(" + stat + ") requires params";
    return indexOf(list, stat, 0);
  }

  int firstIndexOf(final int list, final short stat) {
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

  int asInt(final int index) {
    return values[index];
  }

  private static int _asInt(final long value) {
    return (int) value;
  }

  private static long _asLong(final int value) {
    return value & UINT_MAX_VALUE;
  }

  long asLong(final int index) {
    assert entry(index).Send_Bits >= Integer.SIZE : "entry.Send_Bits(" + entry(index).Send_Bits + ") < " + Integer.SIZE;
    return _asLong(values[index]);
  }

  private static int _asInt(final float value) {
    return Fixed.floatToIntBits(value, 8);
  }

  private static float _asFixed(final int value) {
    return Fixed.intBitsToFloat(value, 8);
  }

  float asFixed(final int index) {
    assert entry(index).ValShift == 8 : "entry.ValShift(" + entry(index).ValShift + ") != " + 8;
    return _asFixed(values[index]);
  }

  private String _asString(final int index, final int value) {
    final byte flags = this.flags[index];
    return (flags & FLAG_FIXED) == 0
        ? (flags & FLAG_LONG) == 0
            ? String.valueOf(value)
            : String.valueOf(_asLong(value))
        : String.valueOf(_asFixed(value));
  }

  String asString(final int index) {
    final byte flags = this.flags[index];
    return (flags & FLAG_FIXED) == 0
        ? (flags & FLAG_LONG) == 0
            ? String.valueOf(asInt(index))
            : String.valueOf(asLong(index))
        : String.valueOf(asFixed(index));
  }

  public short id(final int index) {
    return ids[index];
  }

  ItemStatCost.Entry entry(final int index) {
    return entry(ids[index]);
  }

  ItemStatCost.Entry entry(final short stat) {
    return Stat.entry(stat);
  }

  public int encoding(final int index) {
    return flags[index] & ENCODING_MASK;
  }

  byte flags(final int index) {
    return flags[index];
  }

  int value(final int index) {
    return values[index];
  }

  int param(final int index) {
    return params[index];
  }

  public int value1(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return values[index];
      case 1: return values[index];
      case 2: return values[index];
      case 3: return values[index] & 0xFF;
      case 4: return values[index] & 0x3;
    }
  }

  public int value2(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return 0;
      case 3: return (values[index] >>> 8) & 0xFF;
      case 4: return (values[index] >>> 2) & 0x3FF;
    }
  }

  public int value3(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return 0;
      case 3: return 0;
      case 4: return (values[index] >>> 12) & 0x3FF;
    }
  }

  public int param1(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return params[index];
      case 1: return params[index];
      case 2: return params[index] & 0x3F;
      case 3: return params[index] & 0x3F;
      case 4: return params[index];
    }
  }

  public int param2(final int index) {
    switch (encoding(index)) {
      default: // fall-through
      case 0: return 0;
      case 1: return 0;
      case 2: return (params[index] >>> 6) & 0x3FF;
      case 3: return (params[index] >>> 6) & 0x3FF;
      case 4: return 0;
    }
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

  public int size(final int list) {
    final int sliceIndex = index(list);
    return (offsets[sliceIndex + 1] - offsets[sliceIndex]) & 0xFF;
  }

  public boolean isEmpty(final int list) {
    final int sliceIndex = index(list);
    return (offsets[sliceIndex + 1] == offsets[sliceIndex]);
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

  private void assertSorted() {
    assertSorted(0);
  }

  private void assertSorted(final int offset) {
    assert isSorted(offset) :
        "offsets({" + StringUtils.join(offsets, ',', 0, index(numLists) + offset) + "}) "
            + "tail(" + tail + ") contains property lists that are out of order";
  }

  private static byte encodeFlags(final ItemStatCost.Entry entry) {
    byte flags = (byte) (entry.Encode & ENCODING_MASK);
    if (entry.Save_Param_Bits > 0) flags |= FLAG_PARAMS;
    if (entry.Send_Bits >= Integer.SIZE) flags |= FLAG_LONG;
    if (entry.ValShift > 0) flags |= FLAG_FIXED;
    return flags;
  }

  private void set(final int index, final short stat, final int param, final int value, final ItemStatCost.Entry entry) {
    if (log.traceEnabled()) log.tracefEntry(
        "set(index: %d, stat: %d (%s), param: %d (0x%4$x), value: %d (0x%5$x))", index, stat, entry, param, value);
    assert !immutable;
    ids[index] = stat;
    params[index] = param;
    values[index] = value;
    flags[index] = encodeFlags(entry);
    if (log.debugEnabled()) log.debug(indexDebugString(index));
  }

  private void insertAt(final int list, final int index, final short stat, final int param, final int value, final ItemStatCost.Entry entry) {
    if (log.traceEnabled()) log.tracefEntry(
        "insertAt(index: %d, stat: %d (%s), param: %d (0x%4$x), value: %d (0x%5$x))", index, stat, entry, param, value);
    assert !immutable;
    if (size >= maxSize) {
      log.warn("stat({}) cannot be inserted, property list is full!", stat);
      return;
    }

    ensureCapacity(list, index, 1);
    set(index, stat, param, value, entry);
    size++;
    if (log.traceEnabled()) log.trace(listDebugString(list));
  }

  private void assertMutable() {
    if (immutable) throw new UnsupportedOperationException("Stat list has been frozen");
  }

  public String indexDebugString(final int index) {
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

  public String listDebugString(final int list) {
    final int startIndex = startingOffset(list);
    final int endIndex = endingOffset(list);
    return new ToStringBuilder(this)
        .append("immutable", immutable)
        .append("list", list)
        .append("startIndex", startIndex)
        .append("endIndex", endIndex)
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
        .append("numLists", numLists)
        .append("maxLists", maxLists)
        .append("tail", tail)
        .append("offsets", '{' + StringUtils.join(offsets, ',', 0, numLists << 1) + '}')
        .append("ids", '{' + StringUtils.join(ids, ',', 0, tail) + '}')
        .append("values", '{' + StringUtils.join(values, ',', 0, tail) + '}')
        .append("params", '{' + StringUtils.join(params, ',', 0, tail) + '}')
        .append("flags", '{' + StringUtils.join(flags, ',', 0, tail) + '}')
        .build();
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

  public final class StatIterator implements Iterator<StatGetter> {
    final StatGetter stat = new StatGetter(StatList.this);
    int list; /** used for {@link #pushback} */
    int head; /** used for {@link #pushback} */
    int index;
    int endIndex;

    StatIterator reset(final int list) {
      this.list = list;
      head = index = startingOffset(list);
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

    /**
     * Recycles the previously read stat by re-adding it onto a new list being
     * formed at the start of this list while iteration is occurring. This
     * method effectively breaks the old list and shrinks it to a subset of
     * entries.
     */
    void pushback() {
      assert head < index : "head(" + head + ") cannot pass index(" + index + ")";
      StatList.this.set(head++, stat.id(), stat.param(), stat.value(), stat.entry());
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

  public final class StatListIterator implements Iterator<StatListGetter>, Iterable<StatListGetter> {
    int list = 0;

    StatListIterator reset() {
      list = 0;
      return this;
    }

    @Override
    public Iterator<StatListGetter> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      return list < numLists;
    }

    @Override
    public StatListGetter next() {
      return get(list++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
