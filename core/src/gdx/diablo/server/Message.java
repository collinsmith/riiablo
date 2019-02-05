package gdx.diablo.server;

public class Message {

  public String name;
  public String text;

  private Message() {}

  public Message(String name, String text) {
    this.name = name;
    this.text = text;
  }

  @Override
  public String toString() {
    return name + ": " + text;
  }
}
