package com.riiablo.map5;

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.asset.AssetUtils;

public class Dt1
    extends AbstractReferenceCounted
    implements Disposable
{
  private Dt1() {}

  @SuppressWarnings("GDXJavaStaticResource")
  public static Texture MISSING_TEXTURE;

  FileHandle handle;
  int version;
  int flags;
  int tileOffset;
  int numTiles;
  Tile[] tiles;

  public static Dt1 obtain(FileHandle handle) {
    Dt1 dt1 = new Dt1();
    dt1.handle = handle;
    return dt1;
  }

  @Override
  protected void deallocate() {
    ReferenceCountUtil.release(handle);
    AssetUtils.disposeQuietly(tiles, 0, numTiles);
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

  public int version() {
    return version;
  }

  public int flags() {
    return flags;
  }

  public int tileOffset() {
    return tileOffset;
  }

  public int numTiles() {
    return numTiles;
  }

  public int blocksOffset(int tile) {
    return tiles[tile].blocksOffset;
  }

  public int blocksLength(int tile) {
    return tiles[tile].blocksLength;
  }

  public void uploadTexture(int t) {
    tiles[t].uploadTexture();
  }

  public Tile get(int t) {
    return tiles[t];
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("handle", handle)
        .append("version", version)
        .append("flags", String.format("0x%08x", flags))
        .append("tileOffset", String.format("+0x%x", tileOffset))
        .append("numTiles", numTiles)
        // .append("tiles", tiles)
        .toString();
  }
}
