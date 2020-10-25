package com.riiablo.asset;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AssetDesc<T> {
  public static <T> AssetDesc<T> of(CharSequence path, Class<T> type) {
    return of(path, type, null);
  }

  public static <T> AssetDesc<T> of(CharSequence path, Class<T> type, AssetParams<T> params) {
    return new AssetDesc<>(MutableString.wrap(path), type, params);
  }

  final MutableString path;
  final Class<T> type;
  final AssetParams<T> params;

  AssetDesc(MutableString path, Class<T> type, AssetParams<T> params) {
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

  public AssetParams<T> params() {
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
    if (!(obj instanceof AssetDesc)) return false;
    final AssetDesc other = (AssetDesc) obj;
    return path.equals(other.path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
