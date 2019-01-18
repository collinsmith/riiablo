package gdx.diablo.server;

public class Session {

  private String name;
  private String password;
  private String desc;

  private Session() {}

  public Session(String name) {
    this.name = name;
  }

  public Session(Builder builder) {
    name = builder.name;
    password = builder.password;
    desc = builder.desc;
  }

  @Override
  public String toString() {
    return name;
  }

  public static class Builder {
    public String name;
    public String password;
    public String desc;

    public Session build() {
      return new Session(this);
    }
  }
}
