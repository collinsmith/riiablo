package gdx.diablo.console;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import gdx.diablo.util.StringUtils;

public class Console implements InputProcessor {
  private static final int INITIAL_BUFFER_CAPACITY = 128;

  public final PrintStream out;
  public final InputOps    in = new InputOps();

  private final StringBuffer INPUT = new StringBuffer(INITIAL_BUFFER_CAPACITY);

  private final Set<SuggestionProvider> SUGGESTION_PROVIDERS = new CopyOnWriteArraySet<>();
  private final Set<Processor> PROCESSORS = new CopyOnWriteArraySet<>();

  public Console(OutputStream out) {
    this.out = out instanceof PrintStream ? (PrintStream) out : new PrintStream(out);
  }

  public void clear() {}

  protected void onInputCommit(String buffer) {}

  private void inputModified() {
    String bufferContents = in.toString();
    onInputModified(bufferContents, in.caret);
    caretMoved();
  }

  protected void onInputModified(String buffer, int position) {}

  private void caretMoved() {
    onCaretMoved(in.caret);
  }

  protected void onCaretMoved(int position) {}

  public boolean addProcessor(@NonNull Processor l) {
    Preconditions.checkArgument(l != null, "processor cannot be null");
    return PROCESSORS.add(l);
  }

  public boolean removeProcessor(@Nullable Object o) {
    return o != null && PROCESSORS.remove(o);
  }

  public boolean containsProcessor(@Nullable Object o) {
    return o != null && PROCESSORS.contains(o);
  }

  public boolean addSuggestionProvider(@NonNull SuggestionProvider l) {
    Preconditions.checkArgument(l != null, "suggestion provider cannot be null");
    return SUGGESTION_PROVIDERS.add(l);
  }

  public boolean removeSuggestionProvider(@Nullable Object o) {
    return o != null && SUGGESTION_PROVIDERS.remove(o);
  }

  public boolean containsSuggestionProvider(@Nullable Object o) {
    return o != null && SUGGESTION_PROVIDERS.contains(o);
  }

  @Override
  public boolean keyTyped(char ch) {
    switch (ch) {
      case '\0':
        break;
      case '\3':
        in.clear();
        break;
      case '\b':
        if (in.caret > 0) {
          INPUT.deleteCharAt(--in.caret);
          inputModified();
        }

        break;
      case '\r':
      case '\n':
        if (INPUT.length() > 0) {
          in.commit();
        }

        break;
      case 127: // DEL
        if (in.caret < INPUT.length()) {
          INPUT.deleteCharAt(in.caret);
          inputModified();
        }

        break;
      case '\t':
        break;
      default:
        if (ch == ',' && Gdx.app.getType() == Application.ApplicationType.Android) {
          break;
        }

        INPUT.insert(in.caret++, ch);
        inputModified();
    }

    return true;
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.LEFT:
        if (in.caret > 0) in.caret--;
        caretMoved();
        return true;
      case Input.Keys.RIGHT:
        if (in.caret < INPUT.length()) in.caret++;
        caretMoved();
        return true;
      case Input.Keys.HOME:
        in.caret = 0;
        caretMoved();
        return true;
      case Input.Keys.END:
        in.caret = INPUT.length();
        caretMoved();
        return true;
      case Input.Keys.TAB:
      case Input.Keys.COMMA:
        if (keycode == Input.Keys.COMMA && Gdx.app.getType() != Application.ApplicationType.Android) {
          break;
        }

        final int length = INPUT.length();
        if (in.caret != length || length == 0) {
          break;
        }

        boolean handled;
        String[] args = StringUtils.parseArgs(INPUT);
        CharSequence bufferWrapper = new CharSequence() {
          @Override
          public int length() {
            return INPUT.length();
          }

          @Override
          public char charAt(int index) {
            return INPUT.charAt(index);
          }

          @Override
          public CharSequence subSequence(int start, int end) {
            return INPUT.subSequence(start, end);
          }
        };
        for (SuggestionProvider l : SUGGESTION_PROVIDERS) {
          handled = l.suggest(Console.this, bufferWrapper, args, 0) > 0;
          if (handled) {
            break;
          }
        }

        return true;
      default:
        return true;
    }

    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }

  public class InputOps implements CharSequence {
    int caret;

    public int getCaretPosition() {
      return caret;
    }

    public String getContents() {
      return INPUT.toString();
    }

    public void clear() {
      INPUT.setLength(0);
      caret = 0;
    }

    public String commit() {
      String inputContents = toString();
      out.println(inputContents);
      clear();
      onInputCommit(inputContents);

      boolean handled = false;
      for (Processor p : PROCESSORS) {
        handled = p.process(Console.this, inputContents);
        if (handled) {
          break;
        }
      }

      if (!handled) {
        for (Processor p : PROCESSORS) p.onUnprocessed(Console.this, inputContents);
      }

      return inputContents;
    }

    public void set(CharSequence str) {
      INPUT.setLength(0);
      append(str);
    }

    @Override
    public char charAt(int index) {
      return INPUT.charAt(index);
    }

    public void setCharAt(int index, char ch) {
      INPUT.setCharAt(index, ch);
      caret = index + 1;
    }

    @Override
    public int length() {
      return INPUT.length();
    }

    public boolean isEmpty() {
      return INPUT.length() == 0;
    }

    public void getChars(int srcBegin, int srcEnd, @NonNull char[] dst, int dstBegin) {
      INPUT.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public int indexOf(@Nullable String str) {
      return INPUT.indexOf(str);
    }

    public int indexOf(@Nullable String str, int fromIndex) {
      return INPUT.indexOf(str, fromIndex);
    }

    public int lastIndexOf(@NonNull String str) {
      return INPUT.lastIndexOf(str);
    }

    public int lastIndexOf(@NonNull String str, int fromIndex) {
      return INPUT.lastIndexOf(str, fromIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
      return INPUT.offsetByCodePoints(index, codePointOffset);
    }

    public void append(boolean b) {
      INPUT.append(b);
      caret = INPUT.length();
      inputModified();
    }

    public void append(char c) {
      INPUT.append(c);
      caret = INPUT.length();
      inputModified();
    }

    public void append(@NonNull char[] str) {
      INPUT.append(str);
      caret = INPUT.length();
      inputModified();
    }

    public void append(@NonNull char[] str, int offset, int len) {
      INPUT.append(str, offset, len);
      caret = INPUT.length();
      inputModified();
    }

    public void append(@Nullable CharSequence s) {
      INPUT.append(s);
      caret = INPUT.length();
      inputModified();
    }

    public void append(@Nullable CharSequence s, int start) {
      if (s == null) {
        append(s);
      } else {
        append(s, start, s.length());
      }
    }

    public void append(@Nullable CharSequence s, int start, int end) {
      INPUT.append(s, start, end);
      caret = INPUT.length();
      inputModified();
    }

    public void append(double d) {
      INPUT.append(d);
      caret = INPUT.length();
      inputModified();
    }

    public void append(float f) {
      INPUT.append(f);
      caret = INPUT.length();
      inputModified();
    }

    public void append(int i) {
      INPUT.append(i);
      caret = INPUT.length();
      inputModified();
    }

    public void append(long lng) {
      INPUT.append(lng);
      caret = INPUT.length();
      inputModified();
    }

    public void append(@Nullable Object obj) {
      INPUT.append(obj);
      caret = INPUT.length();
      inputModified();
    }

    public void append(@Nullable String str) {
      INPUT.append(str);
      caret = INPUT.length();
      inputModified();
    }

    public void appendCodePoint(int codePoint) {
      INPUT.appendCodePoint(codePoint);
      caret = INPUT.length();
      inputModified();
    }

    public int codePointAt(int index) {
      return INPUT.codePointAt(index);
    }

    public int codePointBefore(int index) {
      return INPUT.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
      return INPUT.codePointCount(beginIndex, endIndex);
    }

    public void insert(int offset, boolean b) {
      final int length = INPUT.length();
      INPUT.insert(offset, b);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, char c) {
      final int length = INPUT.length();
      INPUT.insert(offset, c);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, @NonNull char[] str) {
      final int length = INPUT.length();
      INPUT.insert(offset, str);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int index, @NonNull char[] str, int offset, int len) {
      final int length = INPUT.length();
      INPUT.insert(offset, str, offset, len);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int dstOffset, @Nullable CharSequence s) {
      final int length = INPUT.length();
      INPUT.insert(dstOffset, s);
      caret = dstOffset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int dstOffset, @Nullable CharSequence s, int start) {
      if (s == null) {
        insert(dstOffset, s);
      } else {
        insert(dstOffset, s, start, s.length());
      }
    }

    public void insert(int dstOffset, @Nullable CharSequence s, int start, int end) {
      final int length = INPUT.length();
      INPUT.insert(dstOffset, s, start, end);
      caret = dstOffset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, double d) {
      final int length = INPUT.length();
      INPUT.insert(offset, d);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, float f) {
      final int length = INPUT.length();
      INPUT.insert(offset, f);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, int i) {
      final int length = INPUT.length();
      INPUT.insert(offset, i);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, long l) {
      final int length = INPUT.length();
      INPUT.insert(offset, l);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, @Nullable Object obj) {
      final int length = INPUT.length();
      INPUT.insert(offset, obj);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    public void insert(int offset, @Nullable String str) {
      final int length = INPUT.length();
      INPUT.insert(offset, str);
      caret = offset + (length - INPUT.length());
      inputModified();
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
      return INPUT.subSequence(start, end);
    }

    @NonNull
    public String substring(int start) {
      return INPUT.substring(start);
    }

    @NonNull
    public String substring(int start, int end) {
      return INPUT.substring(start, end);
    }

    @NonNull
    @Override
    public String toString() {
      return INPUT.toString();
    }
  }

  public interface Processor {
    boolean process(Console console, String buffer);
    void onUnprocessed(Console console, String buffer);
  }

  public interface SuggestionProvider {
    int suggest(Console console, CharSequence buffer, String[] args, int arg);
  }
}
