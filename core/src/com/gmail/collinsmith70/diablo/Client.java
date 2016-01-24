package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.diablo.scene.HudedScene;
import com.gmail.collinsmith70.util.ImmutableDimension;

import java.awt.Dimension;
import java.io.OutputStream;
import java.io.PrintStream;

public class Client implements ApplicationListener {

private static final String TAG = Client.class.getSimpleName();

private final Dimension RESOLUTION;
private final PrintStream STDOUT;
private final PrintStream STDERR;

private ClientConsoleView CONSOLE;
private CommandManager COMMAND_MANAGER;
private GdxCvarManager CVAR_MANAGER;
private GdxKeyManager KEY_MANAGER;
private Stage STAGE;
private HudedScene HUD;

public Client(int width, int height) {
    this.RESOLUTION = new ImmutableDimension(width, height);

    this.STDOUT = System.out;
    this.STDERR = System.err;

    FileHandle consoleFileHandle = Gdx.files.local("console.out");
    OutputStream consoleOut = consoleFileHandle.write(false);

    this.CONSOLE = new ClientConsoleView(consoleOut);
    System.setOut(CONSOLE);
    System.setErr(CONSOLE);
}

public Dimension getResolution() { return RESOLUTION; }
public ClientConsoleView getConsole() { return CONSOLE; }
public CommandManager getCommandManager() { return COMMAND_MANAGER; }
public GdxCvarManager getCvarManager() { return CVAR_MANAGER; }
public GdxKeyManager getKeyManager() { return KEY_MANAGER; }

public void setScene() {

}

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

    System.setOut(STDOUT);
    System.setErr(STDERR);
    CONSOLE.flush();
    CONSOLE.close();
    this.CONSOLE = null;
}

}
