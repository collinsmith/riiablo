package com.google.collinsmith70.diablo;

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
        if (cmd.getNumRequiredParameters() <= args.length-1) {
            cmd.execute(getClient(), args);
        } else {
            getClient().getConsole().log(String.format("Bad syntax, expected \"%s\"", cmd));
        }

        return true;
    }

    Cvar<?> cvar = Cvar.get(args[0]);
    if (cvar != null) {
        switch (args.length) {
            case 1:
                getClient().getConsole().log("get " + cvar.getKey() + " = " + cvar.getStringValue());
                return true;
            case 2:
                getClient().getConsole().log("set " + cvar.getKey() + " = " + args[1]);
                try {
                    cvar.setValue(args[1]);
                } catch (NumberFormatException e) {
                    getClient().getConsole().log(String.format(
                            "Invalid value specified: \"%s\", Expected type: %s",
                            args[1],
                            cvar.getType().getName()));
                }
                return true;
        }
    }

    getClient().getConsole().log(String.format(
            "Unrecognized command \"%s\". To see available commands, type \"%s\"",
            command,
            Commands.HELP.getCommand()));

    return false;
}
}
