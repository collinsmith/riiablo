package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.SaveableCvarManager;

public class GdxCvarManager extends SaveableCvarManager {

  private static final String TAG = GdxCvarManager.class.getSimpleName();

  private final Preferences PREFERENCES;

  public GdxCvarManager() {
    super();
    this.PREFERENCES = Gdx.app.getPreferences(GdxCvarManager.class.getName());
  }

  @Override
  public <T> void save(Cvar<T> cvar) {
    if (!isManaging(cvar)) {
      throw new IllegalArgumentException(String.format(
              "Cvar %s is not managed by this %s",
              cvar.getAlias(),
              getClass().getSimpleName()));
    }

    if (containsSerializer(cvar)) {
      Gdx.app.log(TAG, String.format(
              "Cvar %s cannot be saved (no serializer found)",
              cvar.getAlias()));
      return;
    }

    PREFERENCES.putString(cvar.getAlias(), getSerializer(cvar).serialize(cvar.getValue()));
    PREFERENCES.flush();
    Gdx.app.log(TAG, String.format(
            "%s saved as %s [%s]",
            cvar.getAlias(),
            cvar.getValue(),
            cvar.getType().getName()));
  }

  @Override
  public <T> T load(Cvar<T> cvar) {
    if (!isManaging(cvar)) {
      throw new IllegalArgumentException(String.format(
              "Cvar %s is not managed by this %s",
              cvar.getAlias(),
              getClass().getSimpleName()));
    }

    String serializedValue = PREFERENCES.getString(cvar.getAlias());
    if (serializedValue == null) {
      serializedValue = getSerializer(cvar).serialize(cvar.getDefaultValue());
    }

    Gdx.app.log(TAG, String.format(
            "%s loaded as %s [%s]",
            cvar.getAlias(),
            serializedValue,
            cvar.getType().getName()));

    return getSerializer(cvar).deserialize(serializedValue);
  }

  @Override
  public void onChanged(Cvar cvar, Object from, Object to) {
    super.onChanged(cvar, from, to);
    Gdx.app.log(TAG, String.format("changed %s from %s to %s",
            cvar.getAlias(),
            from,
            to));
  }

}
