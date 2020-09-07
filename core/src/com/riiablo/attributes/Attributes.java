package com.riiablo.attributes;

import android.support.annotation.CallSuper;
import java.util.Iterator;

public abstract class Attributes implements Iterable<StatGetter> {
  public static AggregateAttributes aggregateAttributes() {
    return aggregateAttributes(false);
  }

  public static AggregateAttributes aggregateAttributes(boolean large) {
    return new AggregateAttributes(Attributes.AGGREGATE, large);
  }

  public static AggregateAttributes gemAttributes() {
    return new AggregateAttributes(Attributes.GEM, false);
  }

  public static StatListWrapper wrappedAttributes(StatList stats) {
    return new StatListWrapper(stats);
  }

  /**
   * TODO: Hopefully this doesn't come back to bite me in the ass, but I
   *       decided to go the route of using a marker to tell if the attrs
   *       supports aggregation. By default the answer is no (i.e., an attrs
   *       that wraps a StatList, such as a monster). It's also important to
   *       keep track of whether or not the attrs represents a gem list,
   *       because that subclass was removed when I realized gems have a
   *       required level, and are aggregates. Not going to worry too much
   *       about it now, I think this will work until it doesn't.
   */
  public static final byte WRAPPER = 0;
  public static final byte AGGREGATE = 1;
  public static final byte GEM = 2;

  private StatList stats;
  private byte type;

  Attributes(byte type) {
    this(type, StatList.obtain());
  }

  Attributes(byte type, StatList stats) {
    this.type = type;
    this.stats = stats;
  }

  void setType(byte type) {
    this.type = type;
  }

  public byte type() {
    return type;
  }

  public boolean isType(byte type) {
    return this.type == type;
  }

  public boolean isSimpleType() {
    return type <= 0;
  }

  /**
   * Returns the number of properties contained within {@link #aggregate()}.
   */
  public int size() {
    return aggregate().size();
  }

  /**
   * Returns the whether or not {@link #aggregate()} contains any properties.
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns the property list containing intrinsic traits of the attributes.
   *
   * @see #aggregate()
   * @see #remaining()
   * @see #list()
   */
  public StatListGetter base() {
    return list().first();
  }

  /**
   * Returns the property list containing properties from {@link #list() list}
   * that were applied onto {@link #base() base}.
   *
   * @see #base()
   * @see #remaining()
   * @see #list()
   */
  public StatListGetter aggregate() {
    return list().first();
  }

  /**
   * Returns the property list containing properties from {@link #list() list}
   * that were applied onto {@link #base() base}, but could not be
   * {@link #aggregate() aggregated}.
   *
   * @see #base()
   * @see #aggregate()
   * @see #list()
   */
  public StatListGetter remaining() {
    return list().first();
  }

  /**
   * Returns the property list associated with this attributes. These
   * properties will be applied onto {@link #base() base} and the result will
   * be {@link #aggregate() aggregated}, with any unused properties being
   * {@link #remaining() remaining}.
   *
   * @see #base()
   * @see #aggregate()
   * @see #remaining()
   */
  public StatList list() {
    return stats;
  }

  /**
   * Returns the <i>nth</i> list within {@link #list()}.
   */
  public StatListGetter list(int list) {
    final StatList stats = list();
    assert stats.contains(list) : "list(" + list + ") does not exist: numLists(" + stats.numLists() + ")";
    return stats.get(list);
  }

  /**
   * Clears {@link #remaining()} and {@link #list()} and sets
   * {@link #aggregate()} equal to {@link #base()}.
   */
  public Attributes reset() {
    return this;
  }

  /**
   * Clears {@link #list()}
   */
  @CallSuper
  public void clear() {
    stats.forceClear();
  }

  /**
   * Returns an iterator over the {@link #aggregate() aggregate} properties.
   */
  @Override
  public Iterator<StatGetter> iterator() {
    return aggregate().statIterator();
  }
}
