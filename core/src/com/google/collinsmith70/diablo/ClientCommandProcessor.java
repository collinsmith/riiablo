package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;

import java.util.Objects;

public class ClientCommandProcessor implements CommandProcessor {
private final Client CLIENT;

public ClientCommandProcessor(Client client) {
    this.CLIENT = Objects.requireNonNull(client);
}

public Client getClient() {
    return CLIENT;
}

@Override
public boolean process(String command) {
    if (command.equals("exit")) {
        Gdx.app.exit();
        return true;
    }

    return false;
}
}
