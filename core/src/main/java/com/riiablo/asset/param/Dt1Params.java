package com.riiablo.asset.param;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.map5.Dt1;
import com.riiablo.mpq_bytebuf.Mpq;

public class Dt1Params extends MpqParams<Dt1> {
  private static final int LIBRARY_TILE_ID = -1;
  private static final Dt1Params LIBRARY = new Dt1Params(LIBRARY_TILE_ID);

  public static Dt1Params library() {
    return LIBRARY;
  }

  public static Dt1Params of(int tileId) {
    return new Dt1Params(tileId);
  }

  public static Dt1Params of(short locale, int tileId) {
    return new Dt1Params(locale, tileId);
  }

  public static Dt1Params of(short locale, short platform, int tileId) {
    return new Dt1Params(locale, platform, tileId);
  }

  public final int tileId;

  protected Dt1Params(int tileId) {
    super();
    this.tileId = tileId;
  }

  protected Dt1Params(short locale, int tileId) {
    super(locale);
    this.tileId = tileId;
  }

  protected Dt1Params(short locale, short platform, int tileId) {
    super(locale, platform);
    this.tileId = tileId;
  }

  public Dt1Params copy(int tileId) {
    return of(locale, platform, tileId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Dt1Params)) return false;
    if (!super.equals(o)) return false;
    Dt1Params dt1Extra = (Dt1Params) o;
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
