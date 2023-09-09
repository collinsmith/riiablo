package com.riiablo.asset;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class AssetUtils {
  private AssetUtils() { }

  public static void dispose(Object o) {
    if (o instanceof Disposable) ((Disposable) o).dispose();
  }

  public static void disposeQuietly(Object[] o) {
    disposeQuietly(o, 0, o.length);
  }

  public static void disposeQuietly(Object[] o, int off, int len) {
    for (; off < len; off++) {
      disposeQuietly(o[off]);
      o[off] = null;
    }
  }

  public static void disposeQuietly(Object o) {
    try {
      dispose(o);
    } catch (GdxRuntimeException ignored) {
    }
  }
}
