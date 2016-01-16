package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.cvar2.GdxCvarManagerListener;

public class GdxCvarManager extends CvarManager {

private static final String TAG = GdxCvarManager.class.getSimpleName();

private final Preferences PREFERENCES;

public GdxCvarManager() {
    PREFERENCES = Gdx.app.getPreferences(GdxCvarManagerListener.class.getName());
}

@Override
public <T> void commit(Cvar<T> cvar) {
    super.commit(cvar);
    PREFERENCES.flush();
}

@Override
public <T> void save(Cvar<T> cvar) {
    super.save(cvar);
    PREFERENCES.putString(cvar.getAlias(), getSerializer(cvar).serialize(cvar.getValue()));
    Gdx.app.log(TAG, String.format(
            "%s saved as %s [%s]%n",
            cvar.getAlias(),
            cvar.getValue(),
            cvar.getType().getName()));
}

@Override
public <T> void load(Cvar<T> cvar) {
    super.load(cvar);
    String serializedValue = PREFERENCES.getString(cvar.getAlias());
    if (serializedValue == null) {
        serializedValue = getSerializer(cvar).serialize(cvar.getDefaultValue());
    }

    cvar.setValue(getSerializer(cvar).deserialize(serializedValue));
    Gdx.app.log(TAG, String.format(
            "%s loaded as %s [%s]%n",
            cvar.getAlias(),
            serializedValue,
            cvar.getType().getName()));
}

@Override
public void afterChanged(Cvar cvar, Object from, Object to) {
    super.afterChanged(cvar, from, to);
    Gdx.app.log(TAG, String.format("changed %s from %s to %s%n",
            cvar.getAlias(),
            from,
            to));
}

}
