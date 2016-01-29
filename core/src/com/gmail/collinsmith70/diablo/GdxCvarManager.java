package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;

public class GdxCvarManager extends CvarManager {

private static final String TAG = GdxCvarManager.class.getSimpleName();

private final Preferences PREFERENCES;

public GdxCvarManager() {
    super();
    this.PREFERENCES = Gdx.app.getPreferences(GdxCvarManager.class.getName());
}

@Override
public <T> void commit(Cvar<T> cvar) {
    super.commit(cvar);
    PREFERENCES.flush();
}

@Override
public <T> void save(Cvar<T> cvar) {
    super.save(cvar);
    if (getSerializer(cvar) == null) {
        Gdx.app.log(TAG, String.format(
                "Cvar %s cannot be saved (no serializer found)",
                cvar.getAlias()));
        return;
    }

    PREFERENCES.putString(cvar.getAlias(), getSerializer(cvar).serialize(cvar.getValue()));
    Gdx.app.log(TAG, String.format(
            "%s saved as %s [%s]",
            cvar.getAlias(),
            cvar.getValue(),
            cvar.getType().getName()));
}

@Override
public <T> T load(Cvar<T> cvar) {
    super.load(cvar);
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
public void afterChanged(Cvar cvar, Object from, Object to) {
    super.afterChanged(cvar, from, to);
    Gdx.app.log(TAG, String.format("changed %s from %s to %s",
            cvar.getAlias(),
            from,
            to));
}

}
