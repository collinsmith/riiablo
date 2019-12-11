package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.riiablo.codec.COF;

import java.util.Arrays;

@PooledWeaver
public class CofComponents extends PooledComponent {
  public static final int COMPONENT_NULL = -1;
  public static final int COMPONENT_NIL  = 0;
  public static final int COMPONENT_LIT  = 1;
  public static final int[] DEFAULT_COMPONENT;
  static {
    DEFAULT_COMPONENT = new int[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_COMPONENT, COMPONENT_NIL);
  }

  public final int[] component = new int[COF.Component.NUM_COMPONENTS]; {
    reset();
  }

  @Override
  protected void reset() {
    System.arraycopy(DEFAULT_COMPONENT, 0, component, 0, COF.Component.NUM_COMPONENTS);
  }
}
