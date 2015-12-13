package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.StringBuilder;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.util.FixedArrayCache;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Console implements InputProcessor {

private static final String TAG = Console.class.getSimpleName();
private static final int CONSOLE_BUFFER_SIZE = 256;

private final Client CLIENT;
private final PrintStream OUTPUT_STREAM;
private final Collection<String> OUTPUT;
private final Set<CommandProcessor> COMMAND_PROCESSORS;

private StringBuilder buffer;
private int position;

private String bufferPrefix;

public Console(Client client) {
    this(client, System.out);
}

public Console(Client client, PrintStream outputStream) {
    this.CLIENT = client;
    this.COMMAND_PROCESSORS = new CopyOnWriteArraySet<CommandProcessor>();
    this.OUTPUT = new FixedArrayCache<String>(1024);
    this.OUTPUT_STREAM = new PrintStream(outputStream, true) {
        @Override
        public void println(String x) {
            super.println(x);
            Console.this.OUTPUT.add(x);
        }
    };

    Cvars.Client.Console.CommandPrefix.addCvarChangeListener(new CvarChangeListener<String>() {
        @Override
        public void onCvarChanged(Cvar<String> cvar, String fromValue, String toValue) {
            Console.this.bufferPrefix = toValue;
        }
    });

    clearBuffer();
}

public Client getClient() {
    return CLIENT;
}

public PrintStream getOutputStream() {
    return OUTPUT_STREAM;
}

public Collection<String> getOutput() {
    return OUTPUT;
}

public String getBufferPrefix() {
    return bufferPrefix;
}

public void setBufferPrefix(String bufferPrefix) {
    this.bufferPrefix = bufferPrefix;
}

public void addCommandProcessor(CommandProcessor p) {
    COMMAND_PROCESSORS.add(p);
}

public boolean containsCommandProcessor(CommandProcessor p) {
    return COMMAND_PROCESSORS.contains(p);
}

public boolean removeCommandProcessor(CommandProcessor p) {
    return COMMAND_PROCESSORS.remove(p);
}

public void clearBuffer() {
    buffer = new StringBuilder(CONSOLE_BUFFER_SIZE);
    setPosition(0);
}

public String getBuffer() {
    return buffer.toString();
}

public int getBufferLength() {
    return buffer.length();
}

public boolean isBufferEmpty() {
    return getBufferLength() == 0;
}

public int getPosition() {
    return position;
}

public void setPosition(int position) {
    if (this.position == position) {
        return;
    } else if (position < 0) {
        position = 0;
    } else if (position > buffer.length()) {
        position = buffer.length();
    }

    this.position = position;
}

public static String[] splitBuffer(String buffer) {
    return buffer.split(";");
}

public String[] splitBuffer() {
    return Console.splitBuffer(buffer.toString());
}

@Override
public boolean keyDown(int keycode) {
    switch (keycode) {
        // TODO: Keys.Console should trigger this as well
        case Input.Keys.LEFT:
            setPosition(getPosition() - 1);
            return true;
        case Input.Keys.RIGHT:
            setPosition(getPosition() + 1);
            return true;
        case Input.Keys.UP:
            return true;
        case Input.Keys.DOWN:
            return true;
        case Input.Keys.TAB:
            return true;
        default:
            return false;
    }
}

@Override
public boolean keyUp(int keycode) {
    return false;
}

@Override
public boolean keyTyped(char ch) {
    int position = getPosition();
    switch (ch) {
        case '\0':
            return true;
        case '\b':
            if (position > 0) {
                buffer.deleteCharAt(position - 1);
                setPosition(position - 1);
            }

            return true;
        case '\r':
        case '\n':
            String bufferContents = buffer.toString();
            if (bufferContents.isEmpty()) {
                return true;
            }

            log(bufferPrefix + " " + bufferContents);

            boolean handled;
            for (String command : Console.splitBuffer(bufferContents)) {
                handled = false;
                Gdx.app.log(TAG, bufferPrefix + " " + command);
                for (CommandProcessor p : COMMAND_PROCESSORS) {
                    if (p.process(command)) {
                        handled = true;
                        break;
                    }
                }

                if (!handled) {
                    Gdx.app.log(TAG, String.format("Unrecognized command: \"%s\"", command));
                }
            }

            clearBuffer();
            return true;
        case 127: // DEL
            if (position < buffer.length()) {
                buffer.deleteCharAt(position);
            }

            setPosition(position);
            return true;
        default:
            buffer.insert(position, ch);
            setPosition(position + 1);
            return true;
    }
}

@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return true;
}

@Override
public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return true;
}

@Override
public boolean touchDragged(int screenX, int screenY, int pointer) {
    return true;
}

@Override
public boolean mouseMoved(int screenX, int screenY) {
    return true;
}

@Override
public boolean scrolled(int amount) {
    return true;
}

public void log(String message) {
    //Gdx.app.log(TAG, message);
    OUTPUT_STREAM.println(message);
}

}
