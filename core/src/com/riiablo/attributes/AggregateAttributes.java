package com.riiablo.attributes;

public class AggregateAttributes extends Attributes {
  final StatList base = StatList.obtain(1);
  final StatList agg = StatList.obtain(1);
  final StatList rem = StatList.obtain(1);

  @Override
  public StatList base() {
    return base;
  }

  @Override
  public StatList aggregate() {
    return agg;
  }

  @Override
  public StatList remaining() {
    return rem;
  }

  @Override
  public void resetToBase() {
    assert base.numLists() == 1;
    agg.setAll(base);
    //mod.clear();
    rem.clear();
    super.clear();
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
