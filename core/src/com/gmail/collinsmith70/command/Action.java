package com.gmail.collinsmith70.command;

public interface Action<T> {

  static final Action EMPTY_ACTION = new Action() {

    @Override
    public void onActionExecuted(CommandInstance command, Object obj) {
      //...
    }

  };

  void onActionExecuted(CommandInstance command, T obj);

}
