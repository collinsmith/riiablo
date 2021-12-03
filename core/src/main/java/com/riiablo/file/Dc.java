package com.riiablo.file;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import java.lang.reflect.Array;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.codec.util.BBox;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.util.ImplUtils.todo;

public abstract class Dc<D extends Dc.Direction>
    extends AbstractReferenceCounted
    implements Disposable
{
  private static final Logger log = LogManager.getLogger(Dc.class);

  @SuppressWarnings("GDXJavaStaticResource")
  public static Texture MISSING_TEXTURE;

  protected final FileHandle handle;
  protected final int numDirections;
  protected final int numFrames;
  protected final D[] directions;

  @SuppressWarnings("unchecked")
  protected Dc(FileHandle handle, int numDirections, int numFrames, Class<D> dirType) {
    this.handle = handle;
    this.numDirections = numDirections;
    this.numFrames = numFrames;
    this.directions = (D[]) Array.newInstance(dirType, numDirections);
  }

  public Dc<D> read(ByteBuf buffer, int direction) {
    assert directions[direction] == null;
    assert buffer.isReadable(dirOffset(direction + 1) - dirOffset(direction))
        : handle + " buffer.isReadable(" + (dirOffset(direction + 1) - dirOffset(direction)) + ") = " + buffer.readableBytes();
    retain(); // increment refCnt for each direction read
    return this;
  }

  @Override
  protected void deallocate() {
    ReferenceCountUtil.release(handle);
    for (int d = 0, s = numDirections; d < s; d++) {
      if (directions[d] == null) continue;
      directions[d].dispose();
    }
  }

  @Override
  public ReferenceCounted touch(Object hint) {
    return this;
  }

  @Override
  public void dispose() {
    release();
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

  public abstract int dirOffset(int d);

  public D direction(int d) {
    return directions[d];
  }

  public void uploadTextures(int d, boolean combineFrames) {}

  public BBox box() {
    return todo();
  }

  public BBox box(int d) {
    return direction(d).box();
  }

  public BBox box(int d, int f) {
    return direction(d).frame(f).box();
  }

  public int numPages() {
    return numFrames;
  }

  public TextureRegion page(int i) {
    return page(0, i);
  }

  public TextureRegion page(int d, int i) {
    return direction(d).frame(i).texture();
  }

  public static abstract class Direction<F extends Dc.Frame> implements Disposable {
    @Override public void dispose() {}
    public abstract F[] frames();
    public abstract F frame(int f);
    public abstract BBox box();
  }

  public static abstract class Frame {
    public abstract boolean flipY();
    public abstract int width();
    public abstract int height();
    public abstract int xOffset();
    public abstract int yOffset();
    public abstract BBox box();
    public abstract TextureRegion texture();
  }

  public static int toRealDir(int d, int numDirs) {
    switch (numDirs) {
      case 1:
      case 2:
      case 4:  return d;
      case 8:  return toRealDir8(d);
      case 16: return toRealDir16(d);
      case 32: return toRealDir32(d);
      default: // FIXME: see #113
        log.error("numDirs({}) invalid: should be one of {1,2,4,8,16,32}", numDirs);
        return 0;
    }
  }

  public static int toRealDir8(int d) {
    switch (d) {
      case 0: return 1;
      case 1: return 3;
      case 2: return 5;
      case 3: return 7;

      case 4: return 0;
      case 5: return 2;
      case 6: return 4;
      case 7: return 6;

      default: // FIXME: see #113
        log.error("d({}) invalid: should be in [{}..{})", d, 0, 8);
        return 0;
    }
  }

  public static int toRealDir16(int d) {
    switch (d) {
      case 0:  return 2;
      case 1:  return 6;
      case 2:  return 10;
      case 3:  return 14;

      case 4:  return 0;
      case 5:  return 4;
      case 6:  return 8;
      case 7:  return 12;

      case 8:  return 1;
      case 9:  return 3;
      case 10: return 5;
      case 11: return 7;
      case 12: return 9;
      case 13: return 11;
      case 14: return 13;
      case 15: return 15;

      default: // FIXME: see #113
        log.error("d({}) invalid: should be in [{}..{})", d, 0, 16);
        return 0;
    }
  }

  public static int toRealDir32(int d) {
    switch (d) {
      case 0:  return 4;
      case 1:  return 12;
      case 2:  return 20;
      case 3:  return 28;

      case 4:  return 0;
      case 5:  return 8;
      case 6:  return 16;
      case 7:  return 24;

      case 8:  return 2;
      case 9:  return 6;
      case 10: return 10;
      case 11: return 14;
      case 12: return 18;
      case 13: return 22;
      case 14: return 26;
      case 15: return 30;

      case 16: return 1;
      case 17: return 3;
      case 18: return 5;
      case 19: return 7;
      case 20: return 9;
      case 21: return 11;
      case 22: return 13;
      case 23: return 15;
      case 24: return 17;
      case 25: return 19;
      case 26: return 21;
      case 27: return 23;
      case 28: return 25;
      case 29: return 27;
      case 30: return 29;
      case 31: return 31;

      default: // FIXME: see #113
        log.error("d({}) invalid: should be in [{}..{})", d, 0, 32);
        return 0;
    }
  }
}
