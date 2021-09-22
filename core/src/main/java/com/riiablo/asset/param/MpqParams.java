package com.riiablo.asset.param;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.asset.AssetParams;
import com.riiablo.mpq_bytebuf.Mpq;

import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_LOCALE;
import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_PLATFORM;

public class MpqParams<T> extends AssetParams<T> {
  public static MpqParams<?> of() {
    return new MpqParams<>();
  }

  public static MpqParams<?> of(short locale) {
    return new MpqParams<>(locale);
  }

  public static MpqParams<?> of(short locale, short platform) {
    return new MpqParams<>(locale, platform);
  }

  public final short locale;
  public final short platform;

  public MpqParams() {
    this(DEFAULT_LOCALE);
  }

  protected MpqParams(short locale) {
    this(locale, DEFAULT_PLATFORM);
  }

  protected MpqParams(short locale, short platform) {
    this.locale = locale;
    this.platform = platform;
  }

  public String localeToString() {
    return Mpq.localeToString(locale);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MpqParams)) return false;
    MpqParams other = (MpqParams) o;
    return locale == other.locale;
  }

  @Override
  public int hashCode() {
    return locale;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("locale", localeToString())
        .toString();
  }
}
