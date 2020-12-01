package com.riiablo.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.riiablo.graphics.PaletteIndexedBatch;

public class EntityWrapper extends Actor {
  private static final Matrix4 tmpMatrix = new Matrix4();
  private static final Matrix4 transformMatrix = new Matrix4();

  @SuppressWarnings("deprecation")
  private com.riiablo.entity.Entity entity;

  public EntityWrapper() {}

  public EntityWrapper(@SuppressWarnings("deprecation") com.riiablo.entity.Entity entity) {
    set(entity);
  }

  public void set(@SuppressWarnings("deprecation") com.riiablo.entity.Entity entity) {
    this.entity = entity;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (entity != null) entity.act(delta);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    draw((PaletteIndexedBatch) batch, parentAlpha);
  }

  public void draw(PaletteIndexedBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    if (entity == null) return;
    // manipulating matrices this way is a bit hacky, but this all assumes we don't care about the
    // entities position and assume it is set to 0,0
    tmpMatrix.set(batch.getTransformMatrix());
    transformMatrix.set(tmpMatrix).translate(getX() + getWidth() / 2, getY() + 8, 0);
    batch.setTransformMatrix(transformMatrix);
    entity.draw(batch);
    batch.setTransformMatrix(tmpMatrix);
  }
}
