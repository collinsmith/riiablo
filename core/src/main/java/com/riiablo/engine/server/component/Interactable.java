package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Interactable extends PooledComponent {
  private static final Interactor DEFAULT_INTERACTOR = new DefaultInteractor();

  public float range;
  public int count;
  public Interactor interactor = DEFAULT_INTERACTOR;

  @Override
  protected void reset() {
    range = 0;
    count = 0;
    interactor = DEFAULT_INTERACTOR;
  }

  public Interactable set(float range, Interactor interactor) {
    this.range = range;
    this.interactor = interactor;
    return this;
  }

  public interface Interactor {
    void interact(int src, int entity);
  }

  public static class DefaultInteractor implements Interactor {
    @Override public void interact(int src, int entity) {}
  }
}
