package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.client.component.Hovered;
import com.riiablo.engine.client.component.Label;
import com.riiablo.engine.server.component.Position;
import com.riiablo.map.RenderSystem;

@All({Hovered.class, Label.class, Position.class})
public class LabelManager extends IteratingSystem {
  protected ComponentMapper<Label> mLabel;
  protected ComponentMapper<Position> mPosition;

  protected RenderSystem renderer;
  protected MenuManager menuManager;

  @Wire(name = "iso")
  protected IsometricCamera iso;

  private final Vector2 tmpVec2 = new Vector2();
  private final Array<Actor> labels = new Array<>();

  @Override
  protected boolean checkProcessing() {
    return menuManager.getMenu() == null;
  }

  @Override
  protected void begin() {
    labels.clear();
  }

  @Override
  protected void end() {
    for (Actor label : labels) {
      tmpVec2.x = label.getX();
      tmpVec2.y = label.getY();
      tmpVec2.x = MathUtils.clamp(tmpVec2.x, renderer.getMinX(), renderer.getMaxX() - label.getWidth());
      tmpVec2.y = MathUtils.clamp(tmpVec2.y, renderer.getMinY(), renderer.getMaxY() - label.getHeight());
      label.setPosition(tmpVec2.x, tmpVec2.y);
    }

    Riiablo.batch.begin();
    for (Actor label : labels) {
      label.draw(Riiablo.batch, 1);
    }
    Riiablo.batch.end();
  }

  @Override
  protected void process(int entityId) {
    tmpVec2.set(mPosition.get(entityId).position);
    iso.toScreen(tmpVec2);

    Label label = mLabel.get(entityId);
    tmpVec2.add(label.offset);

    Actor actor = label.actor;
    actor.setPosition(tmpVec2.x, tmpVec2.y, Align.center | Align.bottom);
    labels.add(actor);
  }
}
