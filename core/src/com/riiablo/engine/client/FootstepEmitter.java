package com.riiablo.engine.client;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.map.Map;
import com.riiablo.map.Material;

@All({Player.class, AnimationWrapper.class, Velocity.class, Position.class, MapWrapper.class})
public class FootstepEmitter extends BaseEntitySystem {
  private static final String TAG = "FootstepEmitter";

  private static final boolean DEBUG = !true;
  private static final boolean DEBUG_TRIGGER = DEBUG && true;
  private static final boolean DEBUG_MATERIAL = DEBUG && true;

  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<MapWrapper> mMapWrapper;

  protected Map map;

  private final int middle = 4;
  private int nextTrigger = -1;

  @Override
  protected void processSystem() {
    final int entityId = Riiablo.game.player;
    boolean isMoving = !mVelocity.get(entityId).velocity.isZero();
    if (!isMoving) {
      if (nextTrigger >= 0) nextTrigger = -1;
      return;
    } else if (nextTrigger == -1) {
      nextTrigger = middle;
    }

    Animation animation = mAnimationWrapper.get(entityId).animation;
    int frame = animation.getFrame();
    if (frame == nextTrigger) {
      nextTrigger = nextTrigger == middle ? 0 : middle;
      if (DEBUG_TRIGGER) Gdx.app.debug(TAG, String.format("Triggered on frame %d, next trigger: %d", frame, nextTrigger));

      Map map = mMapWrapper.get(entityId).map;
      Vector2 position = mPosition.get(entityId).position;
      Material material = map.material(position);
      if (DEBUG_MATERIAL) Gdx.app.debug(TAG, "Material: " + material);
      Riiablo.audio.play("light_run_" + material + "_1", true);
    }
  }
}
