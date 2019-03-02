package gdx.diablo.ai;

import gdx.diablo.entity.Monster;
import gdx.diablo.screen.GameScreen;

public abstract class AI {
  public static final AI IDLE = new Idle();

  protected Monster entity;

  public AI(Monster entity) {
    this.entity = entity;
  }

  public void interact(GameScreen gameScreen) {}

  public void update(float delta) {}
}
