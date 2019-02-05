package gdx.diablo.server;

import com.badlogic.gdx.math.GridPoint2;

public class MoveTo {

  public String name;
  public int x;
  public int y;

  private MoveTo() {}

  public MoveTo(String name, GridPoint2 origin) {
    this.name = name;
    x = origin.x;
    y = origin.y;
  }

}
