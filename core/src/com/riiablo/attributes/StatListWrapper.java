package com.riiablo.attributes;

public class StatListWrapper extends Attributes {
  StatListWrapper(StatList stats) {
    super(stats);
  }

  @Override
  public StatListGetter list(int list) {
    assert list == 0 : "list(" + list + ") != " + 0;
    return super.list(list);
  }

  @Override
  public void resetToBase() {
    throw new UnsupportedOperationException();
  }
}
