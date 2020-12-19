package com.riiablo.table;

/**
 * Defines behaviors necessary to inject a record with its required
 * dependencies.
 *
 * @param <R> record type
 * @param <M> manifest
 */
public interface Injector<R, M> {
  R inject(M manifest, R record);
}
