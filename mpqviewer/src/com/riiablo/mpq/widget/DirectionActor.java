package com.riiablo.mpq.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.riiablo.entity.Direction;

public class DirectionActor extends Actor {
  private static final boolean DEBUG_ANGLE = false;

  final float r;
  float angle;
  int dirs;

  public DirectionActor(float rad) {
    this.r = rad;
    setSize(r * 2, r * 2);
    setDebug(true);
    addListener(new InputListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        update(x, y);
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float x, float y, int pointer) {
        update(x, y);
      }

      void update(float x, float y) {
        y = -r + y;
        x = -r + x;
        angle = MathUtils.atan2(y, x);
        DirectionActor.this.update();
      }
    });
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
    update();
  }

  public int getDirections() {
    return dirs;
  }

  public void setDirections(int dirs) {
    this.dirs = dirs;
    this.angle = MathUtils.atan2(-2, -4);
    update();
  }

  public int getDirection() {
    return Direction.radiansToDirection(angle, dirs);
  }

  void update() {
    ChangeListener.ChangeEvent event = new ChangeListener.ChangeEvent();
    event.setListenerActor(this);
    fire(event);
  }

  @Override
  public void drawDebug(ShapeRenderer shapes) {
    //super.drawDebug(shapes);
    if (!isVisible()) return;

    float cx = getX() + r;
    float cy = getY() + r;

    shapes.setColor(Color.WHITE);
    shapes.set(ShapeRenderer.ShapeType.Filled);
    shapes.circle(cx, cy, r, 64);
    shapes.set(ShapeRenderer.ShapeType.Line);

    float snap = Direction.snapToDirection(angle, dirs);
    float dx = r * MathUtils.cos(snap);
    float dy = r * MathUtils.sin(snap);

    shapes.setColor(Color.BLUE);
    shapes.line(cx, cy, cx + dx, cy + dy);

    if (DEBUG_ANGLE) {
      dx = r * MathUtils.cos(angle);
      dy = r * MathUtils.sin(angle);

      shapes.setColor(Color.BLACK);
      shapes.line(cx, cy, cx + dx, cy + dy);
    }
  }
}
