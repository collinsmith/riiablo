package com.google.collinsmith70.diablo.command;

public interface Action {

void onActionExecuted();

static final Action EMPTY_ACTION = new Action() {
    @Override
    public void onActionExecuted() {
        //...
    }
};

}
