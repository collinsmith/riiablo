package gdx.diablo.key;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class KeyMapper implements MappedKey.AssignmentListener, Iterable<MappedKey> {

  protected final IntMap<ObjectSet<MappedKey>> KEYS = new IntMap<>();

  @Override
  @NonNull
  public Iterator<MappedKey> iterator() {
    IntMap.Values<ObjectSet<MappedKey>> entries = KEYS.values();
    ObjectSet<MappedKey> uniqueEntries = new ObjectSet<>();
    for (ObjectSet<MappedKey> entry : entries) {
      uniqueEntries.addAll(entry);
    }

    return uniqueEntries.iterator();
  }

  public boolean add(MappedKey key) {
    return key.addAssignmentListener(this);
  }

  public boolean remove(MappedKey key) {
    boolean removed = false;
    for (int keycode : key.assignments) {
      removed = removed || unassign(key, keycode);
    }

    return removed;
  }

  public Set<MappedKey> get(@MappedKey.Keycode int keycode) {
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      return Collections.emptySet();
    }

    return ImmutableSet.copyOf(keys);
  }

  @Nullable
  protected ObjectSet<MappedKey> lookup(@MappedKey.Keycode int keycode) {
    return KEYS.get(keycode);
  }

  protected void setPressed(MappedKey key, @MappedKey.Keycode int keycode, boolean pressed) {
    assert isManaging(key);
    key.setPressed(keycode, pressed);
  }

  public boolean isManaging(@Nullable MappedKey key) {
    return key != null && containsAnyAssignmentOf(key);
  }

  private boolean containsAnyAssignmentOf(MappedKey key) {
    for (@MappedKey.Keycode int keycode : key.assignments) {
      ObjectSet<MappedKey> keys = KEYS.get(keycode);
      if (keys != null && keys.contains(key)) {
        return true;
      }
    }

    return false;
  }

  protected final void checkIfManaging(@Nullable MappedKey key) {
    Preconditions.checkArgument(!isManaging(key), "key is not managed by this key mapper");
  }

  private void assign(MappedKey key, @MappedKey.Assignment int assignment, @MappedKey.Keycode int keycode) {
    Preconditions.checkArgument(key != null, "key cannot be null");
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      keys = new ObjectSet<>();
      KEYS.put(keycode, keys);
    }

    keys.add(key);
  }

  private boolean unassign(MappedKey key, @MappedKey.Keycode int keycode) {
    Preconditions.checkArgument(key != null, "key cannot be null");
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) return false;
    boolean removed = keys.remove(key);
    if (keys.size == 0) {
      KEYS.remove(keycode);
    }

    return removed;
  }

  @Override
  public void onAssigned(MappedKey key, @MappedKey.Assignment int assignment, @MappedKey.Keycode int keycode) {
    assign(key, assignment, keycode);
  }

  @Override
  public void onFirstAssignment(MappedKey key, @MappedKey.Assignment int assignment, @MappedKey.Keycode int keycode) {
    onAssigned(key, assignment, keycode);
  }

  @Override
  public void onUnassigned(MappedKey key, @MappedKey.Assignment int assignment, @MappedKey.Keycode int keycode) {
    unassign(key, keycode);
  }
}
