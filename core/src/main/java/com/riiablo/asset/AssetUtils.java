package com.riiablo.asset;

import com.badlogic.gdx.utils.Disposable;

public final class AssetUtils {
  private AssetUtils() { }

  public static void dispose(Object o) {
    if (o instanceof Disposable) ((Disposable) o).dispose();
  }
}
