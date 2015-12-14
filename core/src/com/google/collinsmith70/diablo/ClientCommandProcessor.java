package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.command.Command;
import com.google.collinsmith70.diablo.command.Commands;
import com.google.collinsmith70.diablo.cvar.Cvar;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
public void bufferModified(CharSequence buffer) {
    if (buffer.length() < 2) {
        return;
    }

    String[] args = buffer.toString().split("\\s+");
    Map<String, Command> prefixMax = Command.search(args[0]);
    Gdx.app.log("TEST", "listing possabilities:");
    for (String s : prefixMax.keySet()) {
        Gdx.app.log("TEST", s);
    }
}

@Override
public Set<String> getSuggestions(String command, int position) {
    return null;
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
            Commands.HELP.getAlias()));

    return false;
}
}
