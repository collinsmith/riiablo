package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.key.KeyManager;
import com.gmail.collinsmith70.util.Console;

public class Client implements ApplicationListener {

private static final String TAG = Client.class.getSimpleName();

private Console CONSOLE;

private CommandManager COMMAND_MANAGER;
private CvarManager CVAR_MANAGER;
private KeyManager<Integer> KEY_MANAGER;

public Client() {
    this.CONSOLE = new Console() {};
    System.setOut(CONSOLE);
    System.setErr(CONSOLE);
}

public CommandManager getCommandManager() { return COMMAND_MANAGER; }
public CvarManager getCvarManager() { return CVAR_MANAGER; }

@Override
public void create() {
    this.COMMAND_MANAGER = new CommandManager();
    Commands.addTo(COMMAND_MANAGER);

    this.CVAR_MANAGER = new GdxCvarManager();
    Cvars.addTo(CVAR_MANAGER);

    this.KEY_MANAGER = new GdxKeyManager();
    Keys.addTo(KEY_MANAGER);
}

@Override
public void dispose() {
    Gdx.app.log(TAG, "Saving cvars...");
    CVAR_MANAGER.saveAll();
    this.CVAR_MANAGER = null;

    Gdx.app.log(TAG, "Saving key bindings...");
    KEY_MANAGER.saveAll();
    this.KEY_MANAGER = null;

    this.COMMAND_MANAGER = null;
}

}
