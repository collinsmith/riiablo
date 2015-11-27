package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Console {

private static final int CONSOLE_BUFFER_SIZE = 256;

private final Client CLIENT;
private final Set<CommandProcessor> commandProcessors;

private StringBuilder buffer;
private boolean isVisible;

public Console(Client client) {
    this.CLIENT = client;
    this.commandProcessors = new CopyOnWriteArraySet<CommandProcessor>();
    this.isVisible = false;
}

public Client getClient() {
    return CLIENT;
}

public boolean isVisible() {
    return isVisible;
}

public void setVisible(boolean b) {
    this.isVisible = b;
    Gdx.input.setOnscreenKeyboardVisible(isVisible());
    if (isVisible()) {
        //...
    }
}

public void addCommandProcessor(CommandProcessor p) {
    commandProcessors.add(p);
}

public boolean containsCommandProcessor(CommandProcessor p) {
    return commandProcessors.contains(p);
}

public boolean removeCommandProcessor(CommandProcessor p) {
    return commandProcessors.remove(p);
}

public void clearBuffer() {
    buffer = new StringBuilder(CONSOLE_BUFFER_SIZE);
    //caretPosition = 0;
}

public String getBuffer() {
    return buffer.toString();
}

}
