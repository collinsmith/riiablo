package com.riiablo.asset;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AssetDesc<T> {
  public static <T> AssetDesc<T> of(
      CharSequence path,
      Class<T> type
  ) {
    return of(path, type, null);
  }

  public static <T> AssetDesc<T> of(
      CharSequence path,
      Class<T> type,
      AssetParams<? super T> params
  ) {
    return new AssetDesc<>(AssetPath.of(path), type, params);
  }

  public static <T> AssetDesc<T> of(
      AssetDesc<T> asset,
      AssetParams<? super T> params
  ) {
    return new AssetDesc<>(asset.path, asset.type, params);
  }

  final AssetPath path;
  final Class<T> type;
  AssetParams<? super T> params;

  AssetDesc(
      AssetPath path,
      Class<T> type,
      AssetParams<? super T> params
  ) {
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

  public AssetParams params() {
    return params;
  }

  @SuppressWarnings("unchecked")
  public <E extends AssetParams> E params(Class<E> paramsType) {
    return (E) params;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("path", path)
        .append("type", type)
        .append("params", params)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AssetDesc)) return false;
    AssetDesc<?> assetDesc = (AssetDesc<?>) o;
    if (!path.equals(assetDesc.path)) return false;
    return params.equals(assetDesc.params);
  }

  @Override
  public int hashCode() {
    int result = path.hashCode();
    result = 31 * result + params.hashCode();
    return result;
  }
}
