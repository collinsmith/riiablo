package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class Size extends Component {
  public static final int INSIGNIFICANT = 0;
  public static final int SMALL         = 1;
  public static final int MEDIUM        = 2;
  public static final int LARGE         = 3;

  public int size = INSIGNIFICANT;
}
