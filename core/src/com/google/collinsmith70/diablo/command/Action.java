package com.google.collinsmith70.diablo.command;

import com.google.collinsmith70.diablo.Client;

public interface Action {

void onActionExecuted(Client client, String[] args);

static final Action EMPTY_ACTION = new Action() {
    @Override
    public void onActionExecuted(Client client, String[] args) {
        //...
    }
};

}
