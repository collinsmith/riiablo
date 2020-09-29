package com.riiablo.assets;

public interface SyncReader<B> {
  B read(Asset asset);
}
