package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.common.collect.ImmutableList;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javafx.geometry.VerticalDirection;

public class Console implements Disposable {
private static final String TAG = Console.class.getSimpleName();
private static final int CONSOLE_BUFFER_SIZE = 128;
private static final float CARET_HOLD_DELAY = 1.0f;
private static final float CARET_BLINK_DELAY = 0.5f;

private final Client CLIENT;
private final Set<CommandProcessor> commandProcessors;
private BitmapFont font;

private final Timer CARET_TIMER;
private final Timer.Task CARET_BLINK_TASK;

private StringBuffer consoleBuffer;
private boolean isVisible;
private boolean showCaret;
private int caretPosition;
private String commandPrefix;

private ListIterator<String> prefixedKeysIterator;
private String currentlyReadKey;
private VerticalDirection prefixedKeysIteratorMomentum;

private final Deque<String> HISTORY = new ArrayDeque<String>(64);
private ListIterator<String> historyIterator;
private VerticalDirection historyIteratorMomentum;

private final Deque<String> OUTPUT = new LinkedList<String>();

public Console(Client client) {
    this.CLIENT = client;
    this.commandProcessors = new CopyOnWriteArraySet<CommandProcessor>();
    this.isVisible = false;

    final CvarChangeListener<Float> consoleFontColorCvarListener = new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.a)) {
                font.getColor().a = toValue;
            } else if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.r)) {
                font.getColor().r = toValue;
            } else if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.g)) {
                font.getColor().g = toValue;
            } else if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.b)) {
                font.getColor().b = toValue;
            }
        }
    };

    Cvars.Client.Overlay.ConsoleFont.addCvarChangeListener(new CvarChangeListener<AssetDescriptor<BitmapFont>>() {
        @Override
        public void onCvarChanged(Cvar<AssetDescriptor<BitmapFont>> cvar, AssetDescriptor<BitmapFont> fromValue, AssetDescriptor<BitmapFont> toValue) {
            Console.this.getClient().getAssetManager().load(toValue);
            Console.this.getClient().getAssetManager().finishLoading();

            Console.this.font = CLIENT.getAssetManager().get(Cvars.Client.Overlay.ConsoleFont.getValue());
            Cvars.Client.Overlay.ConsoleFontColor.r.addCvarChangeListener(consoleFontColorCvarListener);
            Cvars.Client.Overlay.ConsoleFontColor.g.addCvarChangeListener(consoleFontColorCvarListener);
            Cvars.Client.Overlay.ConsoleFontColor.b.addCvarChangeListener(consoleFontColorCvarListener);
            Cvars.Client.Overlay.ConsoleFontColor.a.addCvarChangeListener(consoleFontColorCvarListener);
        }
    });

    Cvars.Client.Overlay.CommandPrefix.addCvarChangeListener(new CvarChangeListener<String>() {
        @Override
        public void onCvarChanged(Cvar<String> cvar, String fromValue, String toValue) {
            Console.this.commandPrefix = toValue;
        }
    });

    CARET_TIMER = new Timer();
    CARET_BLINK_TASK = new Timer.Task() {
        @Override
        public void run() {
            Console.this.showCaret = !Console.this.showCaret;
        }
    };

    clearBuffer();
    updateCaret();
    CARET_TIMER.start();
}

public Client getClient() {
    return CLIENT;
}

public BitmapFont getFont() {
    return font;
}

public boolean isVisible() {
    return isVisible;
}

public void setVisible(boolean b) {
    this.isVisible = b;
    Gdx.input.setOnscreenKeyboardVisible(isVisible());
    if (isVisible()) {
        updateCaret();
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
    consoleBuffer = new StringBuffer(CONSOLE_BUFFER_SIZE);
    caretPosition = 0;
}

public String getBuffer() {
    return consoleBuffer.toString();
}

public boolean keyDown(int keycode) {
    switch (keycode) {
        case Input.Keys.LEFT:
            caretPosition = Math.max(caretPosition - 1, 0);
            updateCaret();
            return true;
        case Input.Keys.RIGHT:
            caretPosition = Math.min(caretPosition + 1, consoleBuffer.length());
            updateCaret();
            return true;
        case Input.Keys.UP:
            if (historyIterator != null) {
                if (historyIteratorMomentum != VerticalDirection.UP) {
                    historyIteratorMomentum = VerticalDirection.UP;
                    resetListIterator(historyIterator);
                }

                clearBuffer();
                for (char ch : resetListIterator(historyIterator).toCharArray()) {
                    keyTyped(ch, false);
                }
            }

            updateCaret();
            return true;
        case Input.Keys.DOWN:
            if (historyIterator != null) {
                if (historyIteratorMomentum != VerticalDirection.DOWN) {
                    historyIteratorMomentum = VerticalDirection.DOWN;
                    advanceListIterator(historyIterator);
                }

                clearBuffer();
                for (char ch : advanceListIterator(historyIterator).toCharArray()) {
                    keyTyped(ch, false);
                }
            }

            updateCaret();
            return true;
        case Input.Keys.TAB:
            if (prefixedKeysIterator == null) {
                // INVALID
            } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                resetToCarrot();
                try {
                    if (prefixedKeysIteratorMomentum != VerticalDirection.UP) {
                        prefixedKeysIteratorMomentum = VerticalDirection.UP;
                        resetListIterator(prefixedKeysIterator);
                    }

                    currentlyReadKey = resetListIterator(prefixedKeysIterator);
                    for (char ch : currentlyReadKey.toCharArray()) {
                        keyTyped(ch, false);
                    }
                } catch (NoSuchElementException e) {
                }
            } else {
                resetToCarrot();
                try {
                    if (prefixedKeysIteratorMomentum != VerticalDirection.DOWN) {
                        prefixedKeysIteratorMomentum = VerticalDirection.DOWN;
                        advanceListIterator(prefixedKeysIterator);
                    }

                    currentlyReadKey = advanceListIterator(prefixedKeysIterator);
                    for (char ch : currentlyReadKey.toCharArray()) {
                        keyTyped(ch, false);
                    }
                } catch (NoSuchElementException e) {
                }
            }

            return true;
        default:
            return false;
    }
}

private void resetToCarrot() {
    if (currentlyReadKey == null) {
        return;
    }

    for (int i = currentlyReadKey.length(); i > 0; i--) {
        keyTyped('\b', false);
    }
}

private <E> E resetListIterator(ListIterator<E> l) throws NoSuchElementException {
    if (l.hasPrevious()) {
        E temp = l.previous();
        return temp;
    } else {
        E start = null;
        while (l.hasNext()) {
            start = l.next();
        }

        l.previous();
        return start;
    }
}

private <E> E advanceListIterator(ListIterator<E> l) throws NoSuchElementException {
    if (l.hasNext()) {
        E temp = l.next();
        return temp;
    } else {
        E start = null;
        while (l.hasPrevious()) {
            start = l.previous();
        }

        l.next();
        return start;
    }
}

private void updateCaret() {
    updateCaret(true);
}

private void updateCaret(boolean updateLookup) {
    CARET_BLINK_TASK.cancel();
    CARET_TIMER.schedule(CARET_BLINK_TASK, CARET_HOLD_DELAY, CARET_BLINK_DELAY);
    this.showCaret = true;

    if (prefixedKeysIterator == null || updateLookup) {
        String buffer = getBuffer();
        int start = buffer.lastIndexOf(' ', caretPosition-1)+1;
        int end = buffer.indexOf(' ', caretPosition);
        if (end == -1) {
            end = caretPosition;
        } else {
            end = Math.min(caretPosition, end);
        }

        Trie<String, Void> autoComplete = new PatriciaTrie<Void>();
        String arg = buffer.substring(start, end);
        for (String key : Cvar.search(arg).keySet()) {
            key = key.substring(end - start);
            int stop = key.indexOf('.');
            if (stop == -1) {
                stop = key.length();
            } else {
                stop = Math.min(key.length(), stop);
            }

            key = key.substring(0, stop);
            autoComplete.put(key, null);
        }

        prefixedKeysIterator = ImmutableList.copyOf(autoComplete.keySet()).listIterator();
        currentlyReadKey = null;
    }
}

public boolean keyTyped(char ch) {
    return keyTyped(ch, true);
}

public boolean keyTyped(char ch, boolean updateLookup) {
    switch (ch) {
        case '\b':
            if (caretPosition > 0) {
                consoleBuffer.deleteCharAt(--caretPosition);
            }

            updateCaret(updateLookup);
            return true;
        case '\r':
        case '\n':
            boolean commandHandled = false;
            String command = consoleBuffer.toString();
            if (command.isEmpty()) {
                return true;
            }

            HISTORY.addLast(command);
            log(commandPrefix + " " + command);
            historyIterator = ImmutableList.copyOf(HISTORY).listIterator();
            for (CommandProcessor p : commandProcessors) {
                if (p.process(command)) {
                    commandHandled = true;
                    break;
                }
            }

            if (!commandHandled) {
                Gdx.app.log(TAG, String.format("Unrecognized command: \"%s\"", command));
            }

            clearBuffer();
            updateCaret(updateLookup);
            return true;
        case 127:
            if (caretPosition < consoleBuffer.length()) {
                consoleBuffer.deleteCharAt(caretPosition);
            }

            updateCaret(updateLookup);
            return true;
        case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':case 'g':case 'h':case 'i':case 'j':
        case 'k':case 'l':case 'm':case 'n':case 'o':case 'p':case 'q':case 'r':case 's':case 't':
        case 'u':case 'v':case 'w':case 'x':case 'y':case 'z':
        case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G':case 'H':case 'I':case 'J':
        case 'K':case 'L':case 'M':case 'N':case 'O':case 'P':case 'Q':case 'R':case 'S':case 'T':
        case 'U':case 'V':case 'W':case 'X':case 'Y':case 'Z':
        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
        case '-':case '_':case '.':case ' ':case '>':case '<':case '\"':
            consoleBuffer.insert(caretPosition++, ch);
            updateCaret(updateLookup);
            return true;
        default:
            return false;
    }
}

public void log(String message) {
    Gdx.app.log(TAG, message);
    OUTPUT.addFirst(message);
}

public void clear() {
    OUTPUT.clear();
}

public void render(Batch b) {
    if (!isVisible()) {
        return;
    }

    GlyphLayout glyphs = font.draw(b, commandPrefix + " " + getBuffer(), 0, getClient().getVirtualHeight());
    if (showCaret) {
        glyphs.setText(font, commandPrefix + " " + getBuffer().substring(0, caretPosition));
        font.draw(b, "_", glyphs.width - 4, getClient().getVirtualHeight() - 1);
    }

    float position;
    if (font.getLineHeight()*OUTPUT.size() >= getClient().getVirtualHeight()) {
        position = font.getLineHeight();
    } else {
        position = getClient().getVirtualHeight() - font.getLineHeight()*OUTPUT.size();
    }

    for (String line : OUTPUT) {
        if (position >= getClient().getVirtualHeight()) {
            break;
        }

        font.draw(b, line, 0, position);
        position += font.getLineHeight();
    }
}

@Override
public void dispose() {
    font.dispose();
}

}
