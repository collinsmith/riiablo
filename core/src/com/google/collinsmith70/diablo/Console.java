package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Console {
private static final String TAG = Console.class.getSimpleName();

private final Set<CommandProcessor> commandProcessors;

private StringBuffer consoleBuffer;

public Console() {
    this.commandProcessors = new CopyOnWriteArraySet<CommandProcessor>();
    this.consoleBuffer = new StringBuffer(32);
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
    consoleBuffer = new StringBuffer(32);
}

public String getBuffer() {
    return consoleBuffer.toString();
}

public boolean keyTyped(char ch) {
    switch (ch) {
        case '\b':
            if (consoleBuffer.length() > 0) {
                consoleBuffer.deleteCharAt(consoleBuffer.length() - 1);
            }

            break;
        case '\r':
        case '\n':
            String command = consoleBuffer.toString();
            for (CommandProcessor p : commandProcessors) {
                if (p.process(command)) {
                    break;
                }
            }

            clearBuffer();
            // TODO: Output "Invalid command entered: %s"
            break;
        default:
            consoleBuffer.append(ch);
            break;
    }

    Gdx.app.debug(TAG, "Command Buffer=" + getBuffer());
    return false;
}
}
