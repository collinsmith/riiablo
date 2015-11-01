package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.command.Command;
import com.google.collinsmith70.diablo.command.Commands;
import com.google.collinsmith70.diablo.cvar.Cvar;

import java.util.Objects;

public class ClientCommandProcessor implements CommandProcessor {

private static final String TAG = ClientCommandProcessor.class.getSimpleName();

private final Client CLIENT;

public ClientCommandProcessor(Client client) {
    this.CLIENT = Objects.requireNonNull(client);
}

public Client getClient() {
    return CLIENT;
}

@Override
public boolean process(String command) {
    String[] args = command.split("\\s+");
    Command cmd = Command.get(args[0]);
    if (cmd != null) {
        if (cmd.getNumParamters() == args.length-1) {
            Gdx.app.log(TAG, command);
            cmd.execute(args);
        } else {
            Gdx.app.log(TAG, String.format("Bad syntax, expected \"%s\"", cmd));
        }

        return true;
    }

    Cvar<?> cvar = Cvar.get(args[0]);
    if (cvar != null) {
        switch (args.length) {
            case 1:
                Gdx.app.log(TAG, "GET " + cvar.getKey() + " = " + cvar.getStringValue());
                return true;
            case 2:
                Gdx.app.log(TAG, "SET " + cvar.getKey() + " = " + args[1]);
                try {
                    cvar.setValue(args[1]);
                } catch (NumberFormatException e) {
                    Gdx.app.log(TAG,
                            String.format("Invalid value specified: \"%s\", Expected type: %s",
                                    args[1],
                                    cvar.getType().getName()));
                }
                return true;
        }
    }

    Gdx.app.log(TAG, String.format(
            "Unrecognized command \"%s\". To see available commands, type \"%s\"",
            command,
            Commands.HELP.getCommand()));

    return false;
}
}
