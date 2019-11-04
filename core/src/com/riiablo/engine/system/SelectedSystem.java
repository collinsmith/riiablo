package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.BBoxComponent;
import com.riiablo.engine.component.PositionComponent;

public class SelectedSystem extends IteratingSystem {
  private final ComponentMapper<BBoxComponent> boxComponent = ComponentMapper.getFor(BBoxComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);

  private final IsometricCamera iso;
  private final Vector2 coords = new Vector2();
  private final Vector2 tmpVec2 = new Vector2();

  public SelectedSystem(IsometricCamera iso) {
    super(Family.all(BBoxComponent.class).get());
    this.iso = iso;
  }

  @Override
  public void update(float delta) {
    coords.set(Gdx.input.getX(), Gdx.input.getY());
    iso.unproject(coords);
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    entity.flags &= ~Flags.SELECTED;
    if ((entity.flags & Flags.SELECTABLE) != Flags.SELECTABLE) return;
    BBox box = boxComponent.get(entity).box;
    if (box == null) return;
    Vector2 position = positionComponent.get(entity).position;
    iso.toScreen(tmpVec2.set(position));
    float x = tmpVec2.x + box.xMin;
    float y = tmpVec2.y - box.yMax;
    if (x <= coords.x && coords.x <= x + box.width
     && y <= coords.y && coords.y <= y + box.height) {
      entity.flags |= Flags.SELECTED;
    }
  }
}
