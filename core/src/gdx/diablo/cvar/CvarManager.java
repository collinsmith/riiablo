package gdx.diablo.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;

public class CvarManager implements Cvar.StateListener, Iterable<Cvar> {

  private final Trie<String, Cvar> CVARS = new PatriciaTrie<>();

  public Collection<Cvar> getCvars() {
    return CVARS.values();
  }

  @NonNull
  @Override
  public Iterator<Cvar> iterator() {
    return getCvars().iterator();
  }

  @SuppressWarnings("unchecked")
  public boolean add(@NonNull Cvar cvar) {
    String alias = cvar.ALIAS.toLowerCase();
    Cvar queriedCvar = CVARS.get(alias);
    if (ObjectUtils.equals(queriedCvar, cvar)) {
      return false;
    } else if (queriedCvar != null) {
      throw new CvarManagerException("A Cvar with the alias %s has already been added.", queriedCvar.ALIAS);
    }

    CVARS.put(alias, cvar);
    cvar.addStateListener(this);
    return true;
  }

  public boolean remove(Cvar cvar) {
    if (cvar == null) return false;
    String alias = cvar.ALIAS.toLowerCase();
    Cvar queriedCvar = CVARS.get(alias);
    return ObjectUtils.equals(queriedCvar, cvar) && CVARS.remove(alias) != null;
  }

  @SuppressWarnings("unchecked")
  public <T> Cvar<T> get(String alias) {
    if (alias == null) return null;
    return (Cvar<T>) CVARS.get(alias.toLowerCase());
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public SortedMap<String, Cvar> prefixMap(String alias) {
    if (alias == null) return (SortedMap<String, Cvar>) MapUtils.EMPTY_SORTED_MAP;
    return CVARS.prefixMap(alias);
  }

  public boolean isManaging(Cvar cvar) {
    if (cvar == null) return false;
    String alias = cvar.ALIAS.toLowerCase();
    Cvar queriedCvar = CVARS.get(alias);
    return ObjectUtils.equals(queriedCvar, cvar);
  }

  public boolean isManaging(String alias) {
    return alias != null && CVARS.containsKey(alias.toLowerCase());
  }

  @Override
  public void onChanged(Cvar cvar, Object from, Object to) {}

  @Override
  public void onLoaded(Cvar cvar, Object to) {}

  public static class CvarManagerException extends RuntimeException {
    public CvarManagerException() {
      super();
    }

    public CvarManagerException(@Nullable String message) {
      super(message);
    }

    public CvarManagerException(@NonNull String format, @Nullable Object... args) {
      super(String.format(format, args));
    }
  }
}
