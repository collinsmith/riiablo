package gdx.diablo.key;

import com.google.common.base.Preconditions;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.badlogic.gdx.Input.Keys.*;

public class MappedKey implements Iterable<Integer> {

  public static final int NOT_MAPPED = 0;

  public static final int PRIMARY_MAPPING    = 0;
  public static final int SECONDARY_MAPPING  = 1;
  @IntDef({PRIMARY_MAPPING, SECONDARY_MAPPING })
  @Retention(RetentionPolicy.SOURCE)
  public @interface Assignment {}

  @IntDef({ NOT_MAPPED, NUM_0, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, A,
      ALT_LEFT, ALT_RIGHT, APOSTROPHE, AT, B, BACK, BACKSLASH, C, CALL, CAMERA, CLEAR, COMMA, D,
      /*DEL,*/ BACKSPACE, FORWARD_DEL, DPAD_CENTER, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, DPAD_UP,
      /*CENTER, DOWN, LEFT, RIGHT, UP,*/ E, ENDCALL, ENTER, ENVELOPE, EQUALS, EXPLORER, F, FOCUS, G,
      GRAVE, H, HEADSETHOOK, HOME, I, J, K, L, LEFT_BRACKET, M, MEDIA_FAST_FORWARD, MEDIA_NEXT,
      MEDIA_PLAY_PAUSE, MEDIA_PREVIOUS, MEDIA_REWIND, MEDIA_STOP, MENU, MINUS, MUTE, N,
      NOTIFICATION, NUM, O, P, PERIOD, PLUS, POUND, POWER, Q, R, RIGHT_BRACKET, S, SEARCH,
      SEMICOLON, SHIFT_LEFT, SHIFT_RIGHT, SLASH, SOFT_LEFT, SOFT_RIGHT, SPACE, STAR, SYM, T, TAB, U,
      /*UNKNOWN,*/ V, VOLUME_DOWN, VOLUME_UP, W, X, Y, Z, /*META_ALT_LEFT_ON, META_ALT_ON,
      META_ALT_RIGHT_ON, META_SHIFT_LEFT_ON, META_SHIFT_ON,*/ META_SHIFT_RIGHT_ON, /*META_SYM_ON,*/
      CONTROL_LEFT, CONTROL_RIGHT, ESCAPE, END, INSERT, PAGE_UP, PAGE_DOWN, PICTSYMBOLS,
      SWITCH_CHARSET, /*BUTTON_CIRCLE,*/ BUTTON_A, BUTTON_B, BUTTON_C, BUTTON_X, BUTTON_Y, BUTTON_Z,
      BUTTON_L1, BUTTON_R1, BUTTON_L2, BUTTON_R2, BUTTON_THUMBL, BUTTON_THUMBR, BUTTON_START,
      BUTTON_SELECT, BUTTON_MODE, NUMPAD_0, NUMPAD_1, NUMPAD_2, NUMPAD_3, NUMPAD_4, NUMPAD_5,
      NUMPAD_6, NUMPAD_7, NUMPAD_8, NUMPAD_9, COLON, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11,
      F12 })
  @Retention(RetentionPolicy.SOURCE)
  public @interface Keycode {}

  private final String NAME;
  private final String ALIAS;
  @Keycode @Size(min = 2) int[] assignments;
  private int pressed;

  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<>();
  private final Set<StateListener>      STATE_LISTENERS      = new CopyOnWriteArraySet<>();

  public MappedKey(String name, String alias, @Keycode int primary) {
    this(name, alias, primary, NOT_MAPPED);
  }

  public MappedKey(String name, String alias, @Keycode int primary, @Keycode int secondary) {
    Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty");
    Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be empty");
    Preconditions.checkArgument(primary != NOT_MAPPED, "primary key mapping must be mapped");
    Preconditions.checkArgument(primary != secondary, "key mappings must be unique");

    NAME = name;
    ALIAS = alias;
    assignments = new int[] { primary, secondary };
    pressed = 0;
  }

  public String getName() {
    return NAME;
  }

  public String getAlias() {
    return ALIAS;
  }

  @Keycode
  public int getMapping(@Assignment int assignment) {
    return assignments[assignment];
  }

  @Keycode
  public int getPrimaryAssignment() {
    return getMapping(PRIMARY_MAPPING);
  }

  @Keycode
  public int getSecondaryMapping() {
    return getMapping(SECONDARY_MAPPING);
  }

  @Override
  public String toString() {
    return getAlias();
  }

  private int[] validateAssignments(@Keycode @Size(min = 2) int[] keycodes) {
    Preconditions.checkArgument(keycodes.length >= 2, "keycodes.length must be >= 2");
    boolean forceUnmapped = false;
    for (int i = 0; i < keycodes.length; i++) {
      @Keycode int keycode = keycodes[i];
      if (keycode == NOT_MAPPED) {
        forceUnmapped = true;
      }

      for (int j = i + 1; j < keycodes.length; j++) {
        @Keycode int anotherKeycode = keycodes[j];
        if (anotherKeycode == NOT_MAPPED) {
          forceUnmapped = true;
        } else if (forceUnmapped || keycode == anotherKeycode) {
          throw new IllegalArgumentException(
              "mapped keys cannot contain any duplicates or mappings after the last unmapped index. " +
              "Key: " + getName() + " [" + getAlias() + "]");
        }
      }

      if (forceUnmapped) {
        break;
      }
    }

    return keycodes;
  }

  @Keycode
  @Size(min = 2)
  public int[] getAssignments() {
    return Arrays.copyOf(assignments, assignments.length);
  }

  @NonNull
  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private int nextIndex = 0;

      @Override
      public boolean hasNext() {
        return nextIndex < assignments.length;
      }

      @Override
      public Integer next() {
        return assignments[nextIndex++];
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void assign(@Keycode @Size(min = 2) int[] keycodes) {
    validateAssignments(keycodes);
    @Keycode @Size(min = 2) int[] assignments = this.assignments;
    for (@Assignment int i = 0; i < assignments.length; i++) {
      @Keycode int keycode = assignments[i];
      if (keycode == NOT_MAPPED) {
        break;
      }

      for (AssignmentListener l : ASSIGNMENT_LISTENERS) l.onUnassigned(this, i, keycode);
    }

    for (@Assignment int i = 0; i < assignments.length; i++) {
      @Keycode int keycode = assignments[i];
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) l.onAssigned(this, i, keycode);
    }
  }

  @Keycode
  public int assign(@Assignment int assignment, @Keycode int keycode) {
    Preconditions.checkArgument(keycode != NOT_MAPPED, "cannot unmap using this method, use unassign(int) instead");
    @Keycode int previous = assignments[assignment];
    if (previous == keycode) {
      return previous;
    }

    Preconditions.checkArgument(!isAssigned(keycode), "duplicate keycodes are not allowed. Keycode: " + keycode + " Key:" + this);
    assignments[assignment] = keycode;
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
      if (previous != NOT_MAPPED) l.onUnassigned(this, assignment, keycode);
      l.onAssigned(this, assignment, keycode);
    }

    return previous;
  }

  public boolean unassign(@Assignment int assignment) {
    @Keycode int unassigned = assignments[assignment];
    if (unassigned != NOT_MAPPED) {
      System.arraycopy(assignments, assignment + 1, assignments, assignment, assignments.length - assignment - 1);
      assignments[assignments.length - 1] = NOT_MAPPED;
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) l.onUnassigned(this, assignment, unassigned);
      return true;
    }

    return false;
  }

  public boolean unassign() {
    boolean unassigned = false;
    for (@Assignment int i = 0; i < assignments.length; i++) {
      @Keycode int keycode = assignments[i];
      if (keycode != NOT_MAPPED) {
        unassigned = true;
        assignments[i] = NOT_MAPPED;
        for (AssignmentListener l : ASSIGNMENT_LISTENERS) l.onUnassigned(this, i, keycode);
        continue;
      }

      break;
    }

    return unassigned;
  }

  public boolean isAssigned() {
    return assignments[PRIMARY_MAPPING] != NOT_MAPPED;
  }

  public boolean isAssigned(@Keycode int keycode) {
    for (@Keycode int assignedKeycode : assignments) {
      switch (assignedKeycode) {
        case NOT_MAPPED:
          return false;
        default:
          if (keycode == assignedKeycode) return true;
      }
    }

    return false;
  }

  public boolean isPressed() {
    return pressed > 0;
  }

  void setPressed(@Keycode int keycode, boolean pressed) {
    assert isAssigned(keycode);
    if (pressed) {
      this.pressed++;
      for (StateListener l : STATE_LISTENERS) l.onPressed(this, keycode);
    } else {
      this.pressed--;
      for (StateListener l : STATE_LISTENERS) l.onDepressed(this, keycode);
    }
  }

  public boolean addStateListener(StateListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    return STATE_LISTENERS.add(l);
  }

  public boolean containsStateListener(@Nullable Object l) {
    return l != null && STATE_LISTENERS.contains(l);
  }

  public boolean removeStateListener(@Nullable Object l) {
    return l != null && STATE_LISTENERS.remove(l);
  }

  public boolean addAssignmentListener(AssignmentListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    boolean added = ASSIGNMENT_LISTENERS.add(l);
    if (added) {
      for (@Assignment int i = 0; i < assignments.length; i++) {
        @Keycode int keycode = assignments[i];
        if (keycode == NOT_MAPPED) {
          break;
        }

        l.onFirstAssignment(this, i, keycode);
      }
    }

    return added;
  }

  public boolean containsAssignmentListener(@Nullable Object l) {
    return l != null && ASSIGNMENT_LISTENERS.contains(l);
  }

  public boolean removeAssignmentListener(@Nullable Object l) {
    return l != null && ASSIGNMENT_LISTENERS.remove(l);
  }

  interface AssignmentListener {
    void onAssigned(MappedKey key, @Assignment int assignment, @Keycode int keycode);
    void onUnassigned(MappedKey key, @Assignment int assignment, @Keycode int keycode);
    void onFirstAssignment(MappedKey key, @Assignment int assignment, @Keycode int keycode);
  }

  interface StateListener {
    void onPressed(MappedKey key, @Keycode int keycode);
    void onDepressed(MappedKey key, @Keycode int keycode);
  }

}
