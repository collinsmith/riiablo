package gdx.diablo.key;

import com.google.common.base.Throwables;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import gdx.diablo.serializer.SerializeException;

public abstract class SaveableKeyMapper extends KeyMapper {

  private boolean autosave;

  public SaveableKeyMapper() {
    this(true);
  }

  public SaveableKeyMapper(boolean autosave) {
    this.autosave = autosave;
  }

  public boolean isAutosaving() {
    return autosave;
  }

  public void setAutosave(boolean b) {
    if (b != autosave) {
      autosave = b;
      if (b) saveAll();
    }
  }

  @Override
  public boolean add(MappedKey key) {
    try {
      int[] assignments = load(key);
      if (assignments != null) {
        switch (assignments.length) {
          case 0:
            key.unassign();
            break;
          case 1:
            key.unassign();
            key.assign(MappedKey.PRIMARY_MAPPING, assignments[0]);
            break;
          default:
            key.assign(assignments);
        }
      }
    } catch (Throwable t) {
      super.add(key);
      Throwables.propagateIfPossible(t, SerializeException.class);
      throw new SerializeException(t);
    }

    return super.add(key);
  }

  @Nullable
  public abstract int[] load(MappedKey key);

  public abstract void save(MappedKey key);

  public Collection<Throwable> saveAll() {
    Collection<Throwable> throwables = null;
    for (MappedKey key : this) {
      try {
        save(key);
      } catch (RuntimeException e) {
        if (throwables == null) throwables = new ArrayList<>(1);
        throwables.add(e);
      }
    }

    return throwables != null ? throwables : Collections.<Throwable>emptyList();
  }
}
