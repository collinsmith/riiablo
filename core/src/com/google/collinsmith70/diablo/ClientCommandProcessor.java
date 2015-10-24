package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.cvar.Cvar;

import java.util.Objects;
import java.util.Set;

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
    } else if (command.matches("list(\\s+commands)?")) {
        // TODO: output list of all commands
        return true;
    } else if (command.matches("list\\s+cvars")) {
        // TODO: output list of all cvars
        return true;
    }

    String[] args = command.split("\\s+");
    Set<String> cvarKeys = Cvar.search(args[0]);
    if (!cvarKeys.isEmpty()) {
        Cvar<?> cvar = Cvar.get(cvarKeys.iterator().next());
        switch (args.length) {
            case 1:
                Gdx.app.log("TEST", "GET " + cvar.getKey() + " = " + cvar.getValue());
                return true;
            case 2:
                Gdx.app.log("TEST", "SET " + cvar.getKey() + " = " + args[1]);
                try {
                    cvar.setValue(args[1]);
                } catch (NumberFormatException e) {
                    Gdx.app.log("TEST", "Invalid value specified: " + args[1]);
                }
                return true;
            default:
                Gdx.app.log("TEST", "Invalid number of arguments for command: " + command);
                return true;
        }
    }

    return false;
}
}
