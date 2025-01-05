package com.riiablo.asset;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.asset.param.MpqParams;
import com.riiablo.map5.Block;
import com.riiablo.mpq_bytebuf.Mpq;

public class BlockParams extends MpqParams<Block[]> {
  private static final int LIBRARY_TILE_ID = -1;
  private static final BlockParams LIBRARY = new BlockParams(LIBRARY_TILE_ID);

  public static BlockParams library() {
    return LIBRARY;
  }

  public static BlockParams of(int tileId) {
    return new BlockParams(tileId);
  }

  public static BlockParams of(short locale, int tileId) {
    return new BlockParams(locale, tileId);
  }

  public static BlockParams of(short locale, short platform, int tileId) {
    return new BlockParams(locale, platform, tileId);
  }

  public final int tileId;

  protected BlockParams(int tileId) {
    super();
    this.tileId = tileId;
  }

  protected BlockParams(short locale, int tileId) {
    super(locale);
    this.tileId = tileId;
  }

  protected BlockParams(short locale, short platform, int tileId) {
    super(locale, platform);
    this.tileId = tileId;
  }

  public BlockParams copy(int tileId) {
    return of(locale, platform, tileId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BlockParams)) return false;
    if (!super.equals(o)) return false;
    BlockParams dt1Extra = (BlockParams) o;
    return tileId == dt1Extra.tileId;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + tileId;
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("locale", Mpq.localeToString(locale))
        .append("tileId", tileId)
        .toString();
  }
}
