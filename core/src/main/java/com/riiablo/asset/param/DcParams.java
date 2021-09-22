package com.riiablo.asset.param;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.codec.DC;
import com.riiablo.mpq_bytebuf.Mpq;

public class DcParams extends MpqParams<DC> {
  public static DcParams of(int direction) {
    return of(direction, false);
  }

  public static DcParams of(int direction, boolean combineFrames) {
    return new DcParams(direction, combineFrames);
  }

  public static DcParams of(short locale, int direction, boolean combineFrames) {
    return new DcParams(locale, direction, combineFrames);
  }

  public static DcParams of(short locale, short platform, int direction, boolean combineFrames) {
    return new DcParams(locale, platform, direction, combineFrames);
  }

  public final int direction;
  public final boolean combineFrames;

  protected DcParams(int direction, boolean combineFrames) {
    super();
    this.direction = direction;
    this.combineFrames = combineFrames;
  }

  protected DcParams(short locale, int direction, boolean combineFrames) {
    super(locale);
    this.direction = direction;
    this.combineFrames = combineFrames;
  }

  protected DcParams(short locale, short platform, int direction, boolean combineFrames) {
    super(locale, platform);
    this.direction = direction;
    this.combineFrames = combineFrames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DcParams)) return false;
    if (!super.equals(o)) return false;
    DcParams dccExtra = (DcParams) o;
    if (direction != dccExtra.direction) return false;
    return combineFrames == dccExtra.combineFrames;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + direction;
    result = 31 * result + (combineFrames ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("locale", Mpq.localeToString(locale))
        .append("direction", direction)
        .append("combineFrames", combineFrames)
        .toString();
  }
}
