package gdx.diablo.map2;

import com.badlogic.gdx.utils.async.AsyncTask;

public class MapZoneLoader implements AsyncTask<Void> {
  private static final String TAG = "MapZoneLoader";
  private static final boolean DEBUG = true;

  Map map;

  public MapZoneLoader(Map map) {
    this.map = map;
  }

  @Override
  public Void call() {

    return null;
  }
}
