package com.riiablo.console;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Timer;
import com.riiablo.Keys;
import com.riiablo.Riiablo;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.riiablo.Cvars;
import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;

public class RenderedConsole extends Console implements Disposable, InputProcessor {
  private static final String TAG = "RenderedConsole";

  private static final String BUFFER_PREFIX = ">";

  private final Array<String>         OUTPUT = new Array<>();
  private final ByteArrayOutputStream BUFFER = new ByteArrayOutputStream();

  private final Array<String> HISTORY = new Array<>(64);
  private int historyIndex;

  private BitmapFont font;

  private Texture modalBackground;
  private Texture hintBackground;
  private Texture cursorTexture;

  private boolean visible;
  private float   height;
  private float   clientWidth, clientHeight;
  private float   textHeight;
  private float   lineHeight;
  private int     consoleHeight;
  private float   outputHeight;
  private float   consoleY;
  private float   bufferY;
  private float   outputY;
  private int     scrollOffset;
  private int     scrollOffsetMin;


  private static final float CARET_BLINK_DELAY = 0.5f;
  private static final float CARET_HOLD_DELAY  = 1.0f;
  private Timer.Task caretBlinkTask;
  private boolean showCaret;

  public static RenderedConsole wrap(OutputStream out) {
    ConsoleOutputStream cout = new ConsoleOutputStream();
    out = new TeeOutputStream(out, cout);
    RenderedConsole console = new RenderedConsole(out);
    cout.bind(console);
    return console;
  }

  RenderedConsole(OutputStream out) {
    super(out);
  }

  private void recalculateScrollOffset() {
    if (font == null) {
      return;
    }

    clientWidth     = Riiablo.client.width();
    clientHeight    = Riiablo.client.height();
    lineHeight      = font.getLineHeight();
    textHeight      = font.getCapHeight();
    consoleHeight   = (int) (clientHeight * height);
    consoleY        = clientHeight - consoleHeight;
    bufferY         = consoleY + textHeight;
    outputY         = bufferY + lineHeight;
    outputHeight    = consoleHeight - lineHeight - textHeight;
    scrollOffsetMin = (int) (outputHeight / lineHeight) + 1;
    scrollOffset    = Math.max(scrollOffset, scrollOffsetMin);
  }

  public BitmapFont getFont() {
    return font;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean b) {
    if (b != visible) {
      visible = b;
      updateCaret();
      Gdx.input.setOnscreenKeyboardVisible(b);
      scrollOffset = OUTPUT.size;
    }
  }

  @Override
  public void clear() {
    OUTPUT.clear();
  }

  public void create() {
    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 0.5f);
    solidColorPixmap.fill();
    modalBackground = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    solidColorPixmap.fill();
    cursorTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    final Cvar.StateListener<Float> colorChangeListener = new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(Cvar<Float> cvar, Float from, Float to) {
        if (cvar == Cvars.Client.Console.Color.a) {
          font.getColor().a = to;
        } else if (cvar == Cvars.Client.Console.Color.r) {
          font.getColor().r = to;
        } else if (cvar == Cvars.Client.Console.Color.g) {
          font.getColor().g = to;
        } else if (cvar == Cvars.Client.Console.Color.b) {
          font.getColor().b = to;
        }
      }
    };
    Cvars.Client.Console.Font.addStateListener(new CvarStateAdapter<String>() {
      @Override
      public void onChanged(Cvar<String> cvar, String from, String to) {
        if (from != null) Riiablo.assets.unload(from);
        Riiablo.assets.load(to, BitmapFont.class);
        Riiablo.assets.finishLoadingAsset(to);
        font = Riiablo.assets.get(to);
        Cvars.Client.Console.Color.a.addStateListener(colorChangeListener);
        Cvars.Client.Console.Color.r.addStateListener(colorChangeListener);
        Cvars.Client.Console.Color.g.addStateListener(colorChangeListener);
        Cvars.Client.Console.Color.b.addStateListener(colorChangeListener);
        recalculateScrollOffset();
      }
    });

    Cvars.Client.Console.Height.addStateListener(new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(Cvar<Float> cvar, Float from, Float to) {
        height = to;
        recalculateScrollOffset();
      }
    });

    caretBlinkTask = new Timer.Task() {
      @Override
      public void run() {
        showCaret = !showCaret;
      }
    };

    in.clear();
    updateCaret();
  }

  @Override
  protected void onCaretMoved(int position) {
    updateCaret();
  }

  private void updateCaret() {
    caretBlinkTask.cancel();
    Timer.schedule(caretBlinkTask, CARET_HOLD_DELAY, CARET_BLINK_DELAY);
    showCaret = true;
  }

  public void resize(int width, int height) {
    recalculateScrollOffset();
  }

  public void render(Batch b) {
    if (!visible || font == null) return;

    b.draw(modalBackground, 0, consoleY - 4, clientWidth, consoleHeight + 4);

    final int x = 2;
    String inputContents = in.getContents();
    GlyphLayout glyphs = font.draw(b, BUFFER_PREFIX + inputContents, x, bufferY - 2);
    b.draw(cursorTexture, x, bufferY, clientWidth, 2);
    if (showCaret) {
      final int caret = in.getCaretPosition();
      if (caret != in.length()) {
        glyphs.setText(font, BUFFER_PREFIX + inputContents.substring(0, caret));
      }

      b.draw(cursorTexture, x + glyphs.width, consoleY - 2, 2, textHeight);
    }
    Pools.free(glyphs);

    final float outputOffset = scrollOffset * lineHeight;
    if (outputOffset < outputHeight) {
      // offsets output to always appear that it starts at top of console window
      scrollOffset = Math.max(scrollOffset, scrollOffsetMin);
    }

    float position = outputY;
    final int outputSize = OUTPUT.size;
    if (scrollOffset > outputSize) {
      scrollOffset = outputSize;
      position += ((scrollOffsetMin - scrollOffset) * lineHeight);
    }

    for (int i = scrollOffset - 1; i >= 0; i--) {
      if (position > clientHeight) break;
      String line = OUTPUT.get(i);
      font.draw(b, line, x, position);
      position += lineHeight;
    }
  }

  @Override
  public void dispose() {
    cursorTexture.dispose();
    modalBackground.dispose();
  }

  @Override
  protected void onInputCommit(String buffer) {
    HISTORY.add(buffer);
    historyIndex = HISTORY.size;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (Keys.Console.isAssigned(keycode)
        || ((Gdx.input.isKeyPressed(Input.Keys.VOLUME_UP) || Gdx.input.isKeyPressed(Input.Keys.VOLUME_DOWN))
            && Gdx.input.isKeyPressed(Input.Keys.BACK))) {
      visible = !visible;
      return true;
    } else if (!visible) {
      return false;
    }

    switch (keycode) {
      case Input.Keys.MENU:
      case Input.Keys.ESCAPE:
      case Input.Keys.BACK:
        setVisible(true);
        return true;
      case Input.Keys.UP:
        if (historyIndex > 0) {
          in.set(HISTORY.get(--historyIndex));
        }
        return true;
      case Input.Keys.DOWN:
        if (historyIndex < HISTORY.size) {
          in.set(HISTORY.get(historyIndex++));
        } else {
          in.clear();
        }
        return true;
      default:
        super.keyDown(keycode);
        return true;
    }
  }

  @Override
  public boolean keyUp(int keycode) {
    if (!visible) return false;
    super.keyUp(keycode);
    return true;
  }

  @Override
  public boolean keyTyped(char ch) {
    if (!visible) return false;
    if (Keys.Console.isAssigned(Input.Keys.valueOf(Character.toString(ch)))) {
      return true;
    }

    super.keyTyped(ch);
    return true;
  }

  @Override
  public boolean scrolled(int amount) {
    if (!visible) return false;
    switch (amount) {
      case -1:
        if (scrollOffset > 0) {
          scrollOffset--;
        }
        break;
      case 1:
        if (scrollOffset < OUTPUT.size) {
          scrollOffset++;
        }
        break;
      default:
        Gdx.app.error(TAG, "Unexpected scroll amount: " + amount);
    }

    super.scrolled(amount);
    return true;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (!visible) return false;
    Gdx.input.setOnscreenKeyboardVisible(true);
    super.touchDown(screenX, screenY, pointer, button);
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (!visible) return false;
    super.touchUp(screenX, screenY, pointer, button);
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    if (!visible) return false;
    super.touchDragged(screenX, screenY, pointer);
    return true;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    if (!visible) return false;
    super.mouseMoved(screenX, screenY);
    return true;
  }

  static class ConsoleOutputStream extends OutputStream {
    RenderedConsole console;

    void bind(RenderedConsole console) {
      Preconditions.checkState(this.console == null, "already bound to " + this.console);
      this.console = console;
    }

    @Override
    public void write(int b) throws IOException {
      console.BUFFER.write(b);
      if (b == '\n') flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      console.BUFFER.write(b, off, len);
      int size = off + len;
      for (int i = off; i < size; i++) {
        if (b[i] == '\n') flush();
      }
    }

    @Override
    public void flush() throws IOException {
      console.OUTPUT.add(console.BUFFER.toString("UTF-8"));
      console.BUFFER.reset();
      int size = console.OUTPUT.size;
      if (console.scrollOffset == size - 1) {
        console.scrollOffset = size;
      }
    }
  }
}
