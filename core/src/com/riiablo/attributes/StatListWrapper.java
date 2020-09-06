package com.riiablo.attributes;

public class StatListWrapper extends Attributes {
  StatListWrapper(StatList stats) {
    super(Attributes.WRAPPER, stats);
  }

  @Override
  public StatListGetter list(int list) {
    assert list == 0 : "list(" + list + ") != " + 0;
    return super.list(list);
  }

  @Override
  public Attributes reset() {
    throw new UnsupportedOperationException();
  }
}
