package gdx.diablo.server;

import gdx.diablo.Diablo;

public class Event {

  public int id;
  public String[] args;

  private Event() {}

  @Override
  public String toString() {
    return Diablo.string.format(id, (Object[]) args);
  }
}
