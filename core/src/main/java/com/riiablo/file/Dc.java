package com.riiablo.file;

import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Array;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.codec.util.BBox;

public abstract class Dc<D extends Dc.Direction> implements Disposable {
  protected final FileHandle handle;
  protected final int numDirections;
  protected final int numFrames;
  protected final int[] dirOffsets;
  protected final D[] directions;

  @SuppressWarnings("unchecked")
  protected Dc(FileHandle handle, int numDirections, int numFrames, int[] dirOffsets, Class<D> dirType) {
    this.handle = handle;
    this.numDirections = numDirections;
    this.numFrames = numFrames;
    this.dirOffsets = dirOffsets;
    this.directions = (D[]) Array.newInstance(dirType, numDirections);
  }

  @Override
  public void dispose() {
    ReferenceCountUtil.release(handle);
  }

  public FileHandle handle() {
    return handle;
  }

  public int numDirections() {
    return numDirections;
  }

  public int numFrames() {
    return numFrames;
  }

  /** Dc file direction offsets table */
  public int[] dirOffsets() {
    return dirOffsets;
  }

  public int dirOffset(int d) {
    return dirOffsets[d];
  }

  public interface Direction<F extends Dc.Frame> {
    F[] frames();
    F frame(int f);
    BBox box();
  }

  public interface Frame {
    boolean flipY();
    int width();
    int height();
    int xOffset();
    int yOffset();
    BBox box();
  }
}
