package com.google.collinsmith70.diablo.command;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.command.resolver.CvarParameterResolver;

public class Commands {

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
                for (Command c : Command.getCommands()) {
                    Gdx.app.log(TAG, c.toString());
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
