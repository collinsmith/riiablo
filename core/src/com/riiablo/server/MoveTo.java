package com.riiablo.server;

import com.badlogic.gdx.math.GridPoint2;

public class MoveTo {

  public int   id;
  public int   x;
  public int   y;
  public float angle;

  private MoveTo() {}

  public MoveTo(GridPoint2 origin, float angle) {
    x = origin.x;
    y = origin.y;
    this.angle = angle;
  }

}
