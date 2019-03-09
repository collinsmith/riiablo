package com.riiablo.server;

import com.riiablo.Riiablo;

public class Event {

  public int id;
  public String[] args;

  private Event() {}

  @Override
  public String toString() {
    return Riiablo.string.format(id, (Object[]) args);
  }
}
