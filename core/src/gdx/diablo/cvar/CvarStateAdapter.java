package gdx.diablo.cvar;

public class CvarStateAdapter<T> implements Cvar.StateListener<T> {
  @Override
  public void onChanged(Cvar<T> cvar, T from, T to) {}

  @Override
  public void onLoaded(Cvar<T> cvar, T to) {
    onChanged(cvar, null, to);
  }
}
