package com.google.collinsmith70.diablo.command;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.command.resolver.CvarParameterResolver;
import com.google.collinsmith70.diablo.cvar.Cvar;

public class Commands {

public static void loadAll() {
    EXIT.load();
    CLEAR.load();
    HELP.load();
    CVARS.load();
    RESET.load();
}

private static final String TAG = Commands.class.getSimpleName();

public static final Command EXIT = new Command(
        "exit",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                Gdx.app.exit();
            }
        });

public static final Command CLEAR = new Command("clear",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                client.getConsole().clear();
            }
        });

public static final Command HELP = new Command("help",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                for (Command cmd : Command.getCommands()) {
                    client.getConsole().log(cmd.toString());
                }
            }
        });

public static final Command CVARS = new Command("cvars",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                for (Cvar<?> cvar : Cvar.getCvars()) {
                    client.getConsole().log(String.format("%s \"%s\" [%s]",
                            cvar.getKey(),
                            cvar.getStringValue(),
                            cvar.getType().getSimpleName()));
                }
            }
        });

public static final Command RESET = new Command(
        "reset",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                for (int i = 1; i < args.length; i++) {
                    Cvar<?> cvar = Cvar.get(args[i]);
                    if (cvar == null) {
                        client.getConsole().log(String.format(
                                "Unrecognized cvar \"%s\". To see available cvars, type \"%s\"",
                                args[i],
                                Commands.CVARS.getCommand()));
                        continue;
                    }

                    cvar.reset();
                    client.getConsole().log("set " + cvar.getKey() + " = " + cvar.getStringValue());
                }
            }
        },
        CvarParameterResolver.INSTANCE);
}
