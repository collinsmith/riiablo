package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;

import com.badlogic.gdx.math.Vector2;

import com.riiablo.engine.server.component.Missile;
import com.riiablo.engine.server.component.Position;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

@All({Missile.class, Position.class})
public class MissileHandler extends IteratingSystem {
  private static final Logger log = LogManager.getLogger(MissileHandler.class);

  protected ComponentMapper<Missile> mMissile;
  protected ComponentMapper<Position> mPosition;

  @Override
  protected void process(int entityId) {
    final Vector2 position = mPosition.get(entityId).position;
    final Missile missile = mMissile.get(entityId);
    if (missile.start.dst(position) >= missile.range) {
      log.debug("Missile {} is out of range, disposing", entityId);
      world.delete(entityId);
    }
  }
}
