package com.riiablo.attributes;

import android.support.annotation.CallSuper;
import java.util.Iterator;
import org.apache.commons.collections4.IteratorUtils;

public abstract class Attributes implements Iterable<StatGetter> {
  /**
   * an arithmetic association of StatList
   *
   * item property lists will be frozen after creation
   * base stats should be mutable for players, monsters
   *
   * descriptions
   */

  public static AggregateAttributes aggregateAttributes() {
    return new AggregateAttributes();
  }

  public static GemAttributes gemAttributes() {
    return new GemAttributes();
  }

  public static StatListWrapper wrappedAttributes(StatList stats) {
    return new StatListWrapper(stats);
  }

  private StatList stats;

  Attributes() {
    this.stats = StatList.obtain();
  }

  Attributes(StatList stats) {
    this.stats = stats;
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
  public StatList base() {
    return list();
  }

  /**
   * Returns the property list containing properties from {@link #list() list}
   * that were applied onto {@link #base() base}.
   *
   * @see #base()
   * @see #remaining()
   * @see #list()
   */
  public StatList aggregate() {
    return list();
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
  public StatList remaining() {
    return list();
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
   * Clears {@link #remaining()} and sets {@link #aggregate()} equal to
   * {@link #base()}.
   */
  public Attributes resetToBase() {
    return this;
  }

  /**
   * Clears {@link #list()}
   */
  @CallSuper
  public void clear() {
    stats.clear();
  }

  /**
   * Returns an iterator over the {@link #aggregate() aggregate} properties.
   */
  @Override
  public Iterator<StatGetter> iterator() {
    final StatList agg = aggregate();
    final int numLists = agg.numLists();
    if (numLists < 1) return IteratorUtils.emptyIterator();
    return agg.statIterator(0);
  }
}
