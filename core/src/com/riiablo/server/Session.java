package com.riiablo.server;

public class Session {

  public String name;
  public String password;
  public String desc;
  public String host;
  public int    port;

  private Session() {}

  public Session(String name) {
    this.name = name;
  }

  public Session(Builder builder) {
    name = builder.name;
    password = builder.password;
    desc = builder.desc;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
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
