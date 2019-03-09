package com.riiablo.server;

public class Account {

  private String account;
  private String password;

  private Account() {}

  private Account(Builder builder) {
    account  = builder.account;
    password = "";
  }

  @Override
  public String toString() {
    return account;
  }

  public static class Builder {
    public String account;
    public String hash;

    public Account build() {
      return new Account(this);
    }
  }
}
