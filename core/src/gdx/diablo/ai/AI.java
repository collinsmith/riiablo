package gdx.diablo.ai;

import gdx.diablo.entity.Monster;

public abstract class AI {

  protected Monster entity;

  public AI(Monster entity) {
    this.entity = entity;
  }

  public void update(float delta) {}
}
