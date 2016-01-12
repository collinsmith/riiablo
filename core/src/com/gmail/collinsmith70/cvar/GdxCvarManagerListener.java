package com.gmail.collinsmith70.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GdxCvarManagerListener implements CvarManagerListener {

private static final String TAG = GdxCvarManagerListener.class.getSimpleName();

private final Preferences PREFERENCES;

public GdxCvarManagerListener() {
    PREFERENCES = Gdx.app.getPreferences(GdxCvarManagerListener.class.getName());
}

public <T> void onCvarChanged(Cvar<T> cvar, T from, T to) {
    Gdx.app.log(TAG, String.format("changing %s from %s to %s%n",
            cvar.getAlias(),
            from,
            to));
}

@Override
public <T> void onDefaultValueInvalidated(Cvar<T> cvar) {
    Gdx.app.log(TAG, String.format(
            "warning: the specified default value for %s is not considered valid " +
                    "by the specified %s%n",
            cvar.getAlias(),
            cvar.getValueValidator().getClass().getName()));
}

@Override
public void onCommit() {

}

@Override
public <T> void onSave(Cvar<T> cvar) {
    PREFERENCES.putString(cvar.getAlias(), cvar.getSerializedValue());
    Gdx.app.log(TAG, String.format(
            "%s saved as %s [%s]%n",
            cvar.getAlias(),
            cvar.getValue(),
            cvar.getType().getName()));
}

@Override
public <T> void onLoad(Cvar<T> cvar) {
    String serializedValue = PREFERENCES.getString(cvar.getAlias());
    if (serializedValue == null) {
        serializedValue = cvar.getSerializer().serialize(cvar.getDefaultValue());
    }

    cvar.setValue(cvar.getSerializer().deserialize(serializedValue));
    Gdx.app.log(TAG, String.format(
            "%s loaded as %s [%s]%n",
            cvar.getAlias(),
            serializedValue,
            cvar.getType().getName()));
}

}
