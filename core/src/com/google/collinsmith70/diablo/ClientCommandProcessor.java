package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.command.Command;
import com.google.collinsmith70.diablo.command.Commands;
import com.google.collinsmith70.diablo.cvar.Cvar;

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
    String[] args = command.split("\\s+");
    Command cmd = Command.get(args[0]);
    if (cmd != null) {
        if (cmd.getNumParamters() == args.length-1) {
            cmd.execute(args);
        } else {
            Gdx.app.log("TEST", String.format("Bad syntax, expected \"%s\"", cmd));
        }

        return true;
    }

    Cvar<?> cvar = Cvar.get(args[0]);
    if (cvar != null) {
        switch (args.length) {
            case 1:
                Gdx.app.log("TEST", "GET " + cvar.getKey() + " = " + cvar.getStringValue());
                return true;
            case 2:
                Gdx.app.log("TEST", "SET " + cvar.getKey() + " = " + args[1]);
                try {
                    cvar.setValue(args[1]);
                } catch (NumberFormatException e) {
                    Gdx.app.log("TEST",
                            String.format("Invalid value specified: \"%s\", Expected type: %s",
                                    args[1],
                                    cvar.getType().getName()));
                }
                return true;
        }
    }

    Gdx.app.log("TEST", String.format(
            "Unrecognized command \"%s\". To see available commands, type \"%s\"",
            command,
            Commands.HELP.getCommand()));
    return false;

    /*
    String[] args = command.split("\\s+");
    if (args != null && args.length > 0) {
        if (args[0].equals("reset")) {
            switch (args.length) {
                case 1:
                    Gdx.app.log("TEST", "Syntax: reset <cvar>");
                    break;
                default:
                    Cvar<?> cvar;
                    for (int i = 1; i < args.length; i++) {
                        cvar = Cvar.get(args[i]);
                        cvar.reset();
                        Gdx.app.log("TEST", "SET " + cvar.getKey() + " = " + cvar.getStringValue());
                    }
            }

            return true;
        }

        Cvar<?> cvar = Cvar.get(args[0]);
        if (cvar == null) {
            return false;
        }

        switch (args.length) {
            case 1:
                Gdx.app.log("TEST", "GET " + cvar.getKey() + " = " + cvar.getStringValue());
                return true;
            case 2:
                Gdx.app.log("TEST", "SET " + cvar.getKey() + " = " + args[1]);
                try {
                    cvar.setValue(args[1]);
                } catch (NumberFormatException e) {
                    Gdx.app.log("TEST",
                            String.format("Invalid value specified: \"%s\", Expected type: %s",
                                    args[1],
                                    cvar.getType().getName()));
                }
                return true;
            default:
                Gdx.app.log("TEST", "Invalid number of arguments for command: " + command);
                return true;
        }
    }

    return false;*/
}
}
