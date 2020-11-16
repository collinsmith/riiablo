package com.riiablo.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.riiablo.codec.Animation;

public class AnimationWrapper extends Actor {
  protected final Array<Animation> animations;

  private AnimationWrapper() {
    throw new UnsupportedOperationException();
  }

  public AnimationWrapper(Animation animation) {
    animations = new Array<>(1);
    animations.add(animation);
    setTouchable(Touchable.disabled);
  }

  public AnimationWrapper(Animation... animations) {
    this.animations = new Array<>(animations);
    setTouchable(Touchable.disabled);
  }

  // TODO: It's not currently within the scope of this class to support actual layouts -- only to
  //       save the position of the animation to avoid recalculation, and render it with a stage.
  @Override
  public void setPosition(float x, float y, int alignment) {
    setPosition(x, y);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    for (Animation animation : animations) animation.act(delta);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    final float x = getX();
    final float y = getY();
    for (Animation animation : animations) animation.draw(batch, x, y);
  }
}
