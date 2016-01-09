package com.gmail.collinsmith70.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GdxCvarManagerListener implements CvarManagerListener {

private final Preferences PREFERENCES;

public GdxCvarManagerListener() {
    PREFERENCES = Gdx.app.getPreferences(GdxCvarManagerListener.class.getName());
}

@Override
public void onCommit() {

}

@Override
public void onSave(Cvar cvar) {

}

@Override
public void onLoad(Cvar cvar) {

}

}
