package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.LabelComponent;
import com.riiablo.engine.component.PositionComponent;

public class LabelSystem extends IteratingSystem {
  private final ComponentMapper<LabelComponent> labelComponent = ComponentMapper.getFor(LabelComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);

  private final Vector2 tmpVec2 = new Vector2();
  private final IsometricCamera iso;

  private final Array<Actor> labels = new Array<>();

  public LabelSystem(IsometricCamera iso) {
    super(Family.all(PositionComponent.class, LabelComponent.class).get());
    this.iso = iso;
    setProcessing(false);
  }

  @Override
  public void update(float delta) {
    labels.clear();
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    if ((entity.flags & Flags.SELECTED) != Flags.SELECTED) {
      return;
    }

    PositionComponent positionComponent = this.positionComponent.get(entity);
    iso.toScreen(tmpVec2.set(positionComponent.position));

    LabelComponent labelComponent = this.labelComponent.get(entity);
    tmpVec2.add(labelComponent.offset);

    Actor actor = labelComponent.actor;
    actor.setPosition(tmpVec2.x, tmpVec2.y, Align.center);
    labels.add(actor);
  }

  public Array<Actor> getLabels() {
    return labels;
  }
}
