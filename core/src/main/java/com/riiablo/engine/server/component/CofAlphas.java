package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.riiablo.codec.COF;

import java.util.Arrays;

@PooledWeaver
public class CofAlphas extends PooledComponent {
  public static final float ALPHA_NULL = 1.0f;
  public static final float[] DEFAULT_ALPHA;
  static {
    DEFAULT_ALPHA = new float[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_ALPHA, ALPHA_NULL);
  }

  public final float[] alpha = new float[COF.Component.NUM_COMPONENTS]; {
    reset();
  }

  @Override
  protected void reset() {
    System.arraycopy(DEFAULT_ALPHA, 0, alpha, 0, COF.Component.NUM_COMPONENTS);
  }
}
