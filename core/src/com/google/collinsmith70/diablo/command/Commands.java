package com.google.collinsmith70.diablo.command;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.command.resolver.CvarParameterResolver;
import com.google.collinsmith70.diablo.command.resolver.KeyAliasParameterResolver;
import com.google.collinsmith70.diablo.command.resolver.KeyParameterResolver;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.key.Key;

import java.lang.reflect.Field;

public class Commands {

public static void loadAll() {
    init(Commands.class);
}

public static void init(Class<?> clazz) {
    // Hack to instantiate all Cvars
    for (Field field : clazz.getFields()) {
        //...
    }

    for (Class subclass : clazz.getClasses()) {
        init(subclass);
    }
}

private static final String TAG = Commands.class.getSimpleName();

public static final Command EXIT = new Command("exit",
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
        })
        .addAlias("cls");

public static final Command HELP = new Command("help",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                for (Command cmd : Command.getCommands()) {
                    client.getConsole().log(cmd.toString());
                }
            }
        })
        .addAlias("?");

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

public static final Command RESET = new Command("reset",
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

public static final Command BIND = new Command("bind",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                Key key;
                switch (args.length) {
                    case 2:
                        key = Key.get(args[1]);
                        if (key == null) {
                            client.getConsole().log(String.format(
                                    "Unrecognized key alias \"%s\". To see available key aliases, type \"%s\"",
                                    args[1],
                                    Commands.BINDS.getCommand()));
                            break;
                        }

                        client.getConsole().log("bind " + key.getAlias() + " = " + key.getKeysString());
                        break;
                    case 3:
                        key = Key.get(args[1]);
                        if (key == null) {
                            client.getConsole().log(String.format(
                                    "Unrecognized key alias \"%s\". To see available key aliases, type \"%s\"",
                                    args[1],
                                    Commands.BINDS.getCommand()));
                            break;
                        }

                        int keycode = Input.Keys.valueOf(args[2]);
                        if (keycode == -1) {
                            client.getConsole().log(String.format(
                                    "Unrecognized key value \"%s\".",
                                    args[2]));
                            break;
                        }

                        key.addKey(keycode);
                        client.getConsole().log("bind " + key.getAlias() + " = " + key.getKeysString());
                        break;
                    default:
                        client.getConsole().log(String.format(
                                "Invalid number of parameters: %d. Expected 2 or 3",
                                args.length));
                }
            }
        },
        KeyAliasParameterResolver.INSTANCE,
        new OptionalParameterResolver<KeyParameterResolver>(KeyParameterResolver.INSTANCE));

public static final Command BINDS = new Command("binds",
        new Action() {
            @Override
            public void onActionExecuted(Client client, String[] args) {
                for (Key key : Key.getAllKeys()) {
                    client.getConsole().log(key.toString());
                }
            }
        });

}
