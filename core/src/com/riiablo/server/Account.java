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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public String account;
    public String hash;

    public Builder setAccount(String account) {
      this.account = account;
      return this;
    }

    public Account build() {
      return new Account(this);
    }
  }
}
