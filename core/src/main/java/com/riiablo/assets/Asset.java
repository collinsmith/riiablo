package com.riiablo.assets;

import io.netty.util.AsciiString;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Asset<T> {
  public static <T> Asset<T> of(CharSequence path, Class<T> type) {
    return of(path, type, null);
  }

  public static <T> Asset<T> of(CharSequence path, Class<T> type, AssetParameters<T> params) {
    return new Asset<>(new AsciiString(path), type, params);
  }

  final AsciiString path;
  final Class<T> type;
  final AssetParameters<T> params;

  Asset(AsciiString path, Class<T> type, AssetParameters<T> params) {
    this.path = path;
    this.type = type;
    this.params = params;
  }

  public String path() {
    return path.toString();
  }

  public Class<T> type() {
    return type;
  }

  public AssetParameters<T> params() {
    return params;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("path", path)
        .append("type", type)
        .append("params", params)
        .build();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null) return false;
    if (!(obj instanceof Asset)) return false;
    final Asset other = (Asset) obj;
    return path.equals(other.path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
