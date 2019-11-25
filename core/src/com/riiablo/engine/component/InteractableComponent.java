package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class InteractableComponent implements Component, Pool.Poolable {
  private static final Interactor DEFAULT_INTERACTOR = new DefaultInteractor();

  public float range;
  public int count;
  public Interactor interactor = DEFAULT_INTERACTOR;

  @Override
  public void reset() {
    range = 0.0f;
    count = 0;
    interactor = DEFAULT_INTERACTOR;
  }

  public interface Interactor {
    void interact(Entity src, Entity entity);
  }

  public static class DefaultInteractor implements Interactor {
    @Override
    public void interact(Entity src, Entity entity) {}
  }
}
