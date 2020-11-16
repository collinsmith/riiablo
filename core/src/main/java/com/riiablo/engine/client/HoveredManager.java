package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.client.component.BBoxWrapper;
import com.riiablo.engine.client.component.Hovered;
import com.riiablo.engine.client.component.Selectable;
import com.riiablo.engine.server.component.Position;

@All({Selectable.class, BBoxWrapper.class, Position.class})
public class HoveredManager extends IteratingSystem {
  protected ComponentMapper<Hovered> mHovered;
  protected ComponentMapper<BBoxWrapper> mBBoxWrapper;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;

  @Wire(name="iso")
  protected IsometricCamera iso;

  private final Vector2 coords = new Vector2();
  private final Vector2 tmpVec2 = new Vector2();

  @Override
  protected void begin() {
    coords.set(Gdx.input.getX(), Gdx.input.getY());
    iso.unproject(coords);
  }

  @Override
  protected void removed(int entityId) {
    setHovered(entityId, false);
  }

  @Override
  protected void process(int entityId) {
    BBox box = mBBoxWrapper.get(entityId).box;
    if (box == null) return;
    Vector2 position = mPosition.get(entityId).position;
    iso.toScreen(tmpVec2.set(position));
    float x = tmpVec2.x + box.xMin;
    float y = tmpVec2.y - box.yMax;
    boolean b = x <= coords.x && coords.x <= x + box.width
             && y <= coords.y && coords.y <= y + box.height;
    setHovered(entityId, b);
  }

  public void setHovered(int id, boolean b) {
    if (b) {
      mHovered.create(id);
    } else {
      mHovered.remove(id);
    }

    if (mAnimationWrapper.has(id)) mAnimationWrapper.get(id).animation.setHighlighted(b);
  }
}
