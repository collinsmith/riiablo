package com.riiablo.net;

import com.riiablo.net.packet.bnls.LoginResponse;

public class Account {
  public String username;

  public Account() {}

  public Account(LoginResponse loginResponse) {
    username = loginResponse.username();
  }

  @Override
  public String toString() {
    return username;
  }
}
