package com.riiablo.attributes;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class Attributes implements Iterable<StatRef> {
  private static final Logger log = LogManager.getLogger(Attributes.class);

  private static Attributes obtain() {
    return new Attributes();
  }

  /**
   * Returns an attributes capable of holding a large number of aggregated
   * stats. This is intended for players, mercenaries, and any other entities
   * which may have a potential large number/variety of stats aggregated onto
   * them.
   */
  public static Attributes obtainLarge() {
    final Attributes attributes = obtain();
    attributes.reset(Type.LARGE);
    attributes.list = new StatList().reset(StatList.MAX_LISTS);
    attributes.base = new StatList().reset(1).buildList();
    attributes.agg = new StatList(StatList.MAX_SIZE).reset(1).buildList();
    attributes.rem = new StatList(StatList.MAX_SIZE).reset(1).buildList();
    return attributes;
  }

  /**
   * Returns an attributes capable of holding a standard number of aggregated
   * stats. This is intended for standard items or other entities which may
   * have relatively few stats aggregated onto them.
   */
  public static Attributes obtainStandard() {
    final Attributes attributes = obtain();
    attributes.reset(Type.STANDARD);
    attributes.list = new StatList().reset(StatList.MAX_LISTS);
    attributes.base = new StatList().reset(1).buildList();
    attributes.agg = new StatList().reset(1).buildList();
    attributes.rem = new StatList().reset(1).buildList();
    return attributes;
  }

  /**
   * Returns an attributes capable of holding a small number of aggregated
   * stats. This is intended for compact items or other entities which may
   * have only a couple stats aggregated onto them.
   */
  public static Attributes obtainCompact() {
    final Attributes attributes = obtain();
    attributes.reset(Type.COMPACT);
    attributes.list = new StatList().reset(StatList.MAX_LISTS);
    attributes.base = new StatList().reset(1).buildList(); // TODO: create rem as list(4)
    attributes.agg = new StatList().reset(1).buildList(); // TODO: set agg as rem or list(6)
    attributes.rem = new StatList().reset(1).buildList(); // TODO: create rem as list(5)
    return attributes;
  }

  /**
   * Wraps the specified stat list in an attributes.
   */
  public static Attributes wrap(StatList stats) {
    throw new UnsupportedOperationException();
  }

  public enum Type {
    /** (for entities with equippables) standard base with large agg and rem */
    LARGE,
    /** (for items) standard base, agg and rem */
    STANDARD {
      @Override
      boolean isValid(final int listFlags) {
        final int setItemListCount = StatListFlags.countSetItemFlags(listFlags);
        if (setItemListCount > 1) log.warnf("listFlags(0x%x) contains more than 1 set", listFlags);
        return super.isValid(listFlags);
      }
    },
    /** (for gems) standard base with agg and rem included within it */
    COMPACT {
      @Override
      boolean isValid(final int listFlags) {
        final int gemListCount = StatListFlags.countGemFlags(listFlags);
        if (gemListCount == 0) {
          log.warnf("listFlags(0x%x) does not have any gem apply type selected", listFlags);
        } else if (gemListCount > 1) {
          log.warnf("listFlags(0x%x) contains more than 1 gem apply type", listFlags);
        }
        return super.isValid(listFlags);
      }
    },
    /** (for monsters) wraps the specified stat list */
    WRAPPER(false);

    private final boolean updatable;

    Type() {
      this(true);
    }

    Type(boolean updatable) {
      this.updatable = updatable;
    }

    boolean isValid(int listFlags) {
      return updatable;
    }

    boolean updatable() {
      return updatable;
    }
  }

  private Type type;
  private StatList list;
  private StatListRef base;
  private StatListRef agg;
  private StatListRef rem;

  Attributes() {}

  Attributes reset(Type type) {
    this.type = type;
    list = null;
    base = null;
    agg = null;
    rem = null;
    return this;
  }

  public boolean isType(Type type) {
    return this.type == type;
  }

  public Type type() {
    return type;
  }

  public int size() {
    return agg.size();
  }

  public boolean isEmpty() {
    return agg.isEmpty();
  }

  public StatListRef base() {
    return base;
  }

  public StatListRef aggregate() {
    return agg;
  }

  public StatListRef remaining() {
    return rem;
  }

  public StatList list() {
    return list;
  }

  public StatListRef list(int list) {
    return this.list.get(list);
  }

  public Attributes reset() {
    if (base.isEmpty()) log.warn("#reset() called on attributes with an empty base");
    agg.setAll(base);
    rem.clear();
    return this;
  }

  public void clear()  {
    list.forceClear();
    base.clear();
    agg.clear();
    rem.clear();
  }

  @Override
  public Iterator<StatRef> iterator() {
    return agg.statIterator();
  }

  public String dump() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(outputStream);
    out.println("--------------------------------------------------------------------------------");
    out.println("base:");
    for (StatRef stat : base()) {
      out.println(stat.debugString());
    }

    out.println("--------------------------------------------------------------------------------");
    out.println("lists:");
    for (StatListRef list : list().listIterator()) {
      out.println("list:");
      for (StatRef stat : list) {
        out.println("  " + stat.debugString());
      }
    }

    out.println("--------------------------------------------------------------------------------");
    out.println("aggregate:");
    for (StatRef stat : aggregate()) {
      out.println(stat.debugString());
    }

    out.println("--------------------------------------------------------------------------------");
    out.println("remaining:");
    for (StatRef stat : remaining()) {
      out.println(stat.debugString());
    }

    return outputStream.toString(Charset.forName("US-ASCII"));
  }

  public boolean contains(final short stat) {
    return agg.containsAny(stat);
  }

  public StatRef get(final short stat) {
    return agg.get(stat);
  }

  public StatRef getCopy(final short stat) {
    final StatRef ref = agg.get(stat);
    return ref != null ? ref.copy() : null;
  }

  public StatRef set(final short stat, final short srcStat) {
    return agg.set(stat, srcStat);
  }

  public int getValue(final short stat, final int value) {
    return agg.getValue(stat, value);
  }

  public long getValue(final short stat, final long value) {
    return agg.getValue(stat, value);
  }

  public float getValue(final short stat, final float value) {
    return agg.getValue(stat, value);
  }
}
