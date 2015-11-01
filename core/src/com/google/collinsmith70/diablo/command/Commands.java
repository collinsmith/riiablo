package com.google.collinsmith70.diablo.command;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.command.resolver.CvarParameterResolver;
import com.google.collinsmith70.diablo.cvar.Cvar;

public class Commands {

public static void loadAll() {
    EXIT.load();
    HELP.load();
    CVARS.load();
    RESET.load();
}

private static final String TAG = Commands.class.getSimpleName();

public static final Command EXIT = new Command(
        "exit",
        new Action() {
            @Override
            public void onActionExecuted() {
                Gdx.app.exit();
            }
        });

public static final Command HELP = new Command("help",
        new Action() {
            @Override
            public void onActionExecuted() {
                for (Command cmd : Command.getCommands()) {
                    Gdx.app.log(TAG, cmd.toString());
                }
            }
        });

public static final Command CVARS = new Command("cvars",
        new Action() {
            @Override
            public void onActionExecuted() {
                for (Cvar<?> cvar : Cvar.getCvars()) {
                    Gdx.app.log(TAG, cvar.toString());
                }
            }
        });

public static final Command RESET = new Command(
        "reset",
        new Action() {
            @Override
            public void onActionExecuted() {

            }
        },
        CvarParameterResolver.INSTANCE);
}
