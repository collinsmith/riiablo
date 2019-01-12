package gdx.diablo;

import com.google.common.base.MoreObjects;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import gdx.diablo.cvar.Cvar;
import gdx.diablo.cvar.SaveableCvarManager;
import gdx.diablo.serializer.StringSerializer;

public class GdxCvarManager extends SaveableCvarManager {
  private static final String TAG = "GdxCvarManager";

  private final Preferences PREFERENCES = Gdx.app.getPreferences(TAG);

  @Override
  public <T> void save(Cvar<T> cvar) {
    String alias = cvar.getAlias();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG && !isManaging(cvar)) {
      throw new CvarManagerException("%s must be managed by this CvarManager", alias);
    }

    StringSerializer<T> serializer = MoreObjects.firstNonNull(cvar.getSerializer(), getSerializer(cvar));
    if (serializer == null) {
      throw new CvarManagerException("%s cannot be saved (no serializer found for %s)", alias, cvar.getType().getName());
    }

    T value = cvar.get();
    String serialization = serializer.serialize(value);
    PREFERENCES.putString(alias, serialization);
    PREFERENCES.flush();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s saved as \"%s\" (raw: \"%s\")", alias, value, serialization));
    }
  }

  @Override
  public <T> T load(Cvar<T> cvar) {
    String alias = cvar.getAlias();
    StringSerializer<T> serializer = MoreObjects.firstNonNull(cvar.getSerializer(), getSerializer(cvar));
    if (serializer == null) {
      try {
        throw new CvarManagerException("%s cannot be loaded (no deserializer found for %s)", alias, cvar.getType().getName());
      } finally {
        return cvar.getDefault();
      }
    }

    String serialization = PREFERENCES.getString(alias, null);
    if (serialization == null) return cvar.getDefault();

    T deserialization = serializer.deserialize(serialization);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s loaded as \"%s\" [%s] (raw: \"%s\")",
          alias, deserialization, deserialization.getClass().getName(), serialization));
    }

    return deserialization;
  }

  @Override
  public void onChanged(Cvar cvar, Object from, Object to) {
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s changed from %s to %s", cvar.getAlias(), from, to));
    }

    super.onChanged(cvar, from, to);
  }
}
