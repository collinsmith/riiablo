package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.util.Console;

public class Client implements ApplicationListener {

private final Console CONSOLE;
private final CommandManager COMMAND_MANAGER;
private final CvarManager CVAR_MANAGER;

public Client() {
    this.COMMAND_MANAGER = new CommandManager();
    this.CVAR_MANAGER = new GdxCvarManager();
    this.CONSOLE = new Console() {};
    System.setOut(CONSOLE);
    System.setErr(CONSOLE);
}

public CommandManager getCommandManager() { return COMMAND_MANAGER; }
public CvarManager getCvarManager() { return CVAR_MANAGER; }

@Override
public void create() {
    Cvars.addTo(CVAR_MANAGER);
    Commands.addTo(COMMAND_MANAGER);
}

}
