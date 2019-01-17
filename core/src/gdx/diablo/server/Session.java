package gdx.diablo.server;

public class Session {

  private String name;

  private Session() {}

  public Session(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
