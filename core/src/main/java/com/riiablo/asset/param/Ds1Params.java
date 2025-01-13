package com.riiablo.asset.param;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.map5.Ds1;
import com.riiablo.mpq_bytebuf.Mpq;

public class Ds1Params extends MpqParams<Ds1> {
  public static Ds1Params of() {
    return new Ds1Params();
  }

  public static Ds1Params of(short locale) {
    return new Ds1Params(locale);
  }

  public static Ds1Params of(short locale, short platform) {
    return new Ds1Params(locale, platform);
  }

  protected Ds1Params() {
    super();
  }

  protected Ds1Params(short locale) {
    super(locale);
  }

  protected Ds1Params(short locale, short platform) {
    super(locale, platform);
  }

  public Ds1Params copy() {
    return of(locale, platform);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Ds1Params)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("locale", Mpq.localeToString(locale))
        .toString();
  }
}
