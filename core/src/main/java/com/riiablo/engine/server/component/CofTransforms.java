package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.riiablo.codec.COF;

import java.util.Arrays;

@PooledWeaver
public class CofTransforms extends PooledComponent {
  public static final byte TRANSFORM_NULL = -1;
  public static final byte[] DEFAULT_TRANSFORM;
  static {
    DEFAULT_TRANSFORM = new byte[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_TRANSFORM, TRANSFORM_NULL);
  }

  public final byte[] transform = new byte[COF.Component.NUM_COMPONENTS]; {
    reset();
  }

  @Override
  protected void reset() {
    System.arraycopy(DEFAULT_TRANSFORM, 0, transform, 0, COF.Component.NUM_COMPONENTS);
  }
}
