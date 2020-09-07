package com.riiablo.attributes;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class AggregateAttributes extends Attributes {
  private static final Logger log = LogManager.getLogger(AggregateAttributes.class);

  final StatList base = StatList.obtainLarge();
  final StatList agg = StatList.obtainLarge();
  final StatList rem = StatList.obtainLarge();

  AggregateAttributes(byte type) {
    super(type);
  }

  @Override
  public StatListGetter base() {
    if (!base.contains(0)) base.newList(0);
    return base.first();
  }

  @Override
  public StatListGetter aggregate() {
    if (!agg.contains(0)) agg.newList(0);
    return agg.first();
  }

  @Override
  public StatListGetter remaining() {
    if (!rem.contains(0)) rem.newList(0);
    return rem.first();
  }

  @Override
  public Attributes reset() {
    final StatList base = base().parent();
    assert base.numLists() == 1 : "base.numLists(" + base.numLists() + ") != " + 1;
    if (base.numLists() < 1) log.warn("#reset() called on Attributes with empty base!");
    agg.setAll(base);
    //mod.clear();
    rem.clear();
    return this;
  }

  /**
   * {@inheritDoc}, {@link #base()}, {@link #aggregate()}, and {@link #remaining()}
   */
  @Override
  public void clear() {
    base.clear();
    agg.clear();
    //mod.clear();
    rem.clear();
    super.clear();
  }
}
