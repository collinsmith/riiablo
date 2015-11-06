package com.google.collinsmith70.diablo.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class SoundVolumeController implements VolumeController<Sound> {

private static final String TAG = SoundVolumeController.class.getSimpleName();

private final Collection<WeakReference<Sound>> managedSounds;

private boolean globalSoundEnabled;
private boolean soundEnabled;
private float soundVolume;

public SoundVolumeController() {
    this.managedSounds = new CopyOnWriteArrayList<WeakReference<Sound>>();

    Cvars.Client.Sound.Enabled.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            SoundVolumeController.this.globalSoundEnabled = toValue;
            SoundVolumeController.this.refreshVolume();
        }
    });

    Cvars.Client.Sound.Sfx.Enabled.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            SoundVolumeController.this.setEnabled(toValue);
        }
    });

    Cvars.Client.Sound.Sfx.Volume.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            SoundVolumeController.this.setVolume(toValue);
        }
    });
}

@Override
public boolean isEnabled() {
    return soundEnabled;
}

@Override
public void setEnabled(boolean enabled) {
    this.soundEnabled = enabled;
    Gdx.app.log(TAG, "Sound " + (soundEnabled ? "enabled" : "disabled"));
}

@Override
public float getVolume() {
    if (!globalSoundEnabled || !isEnabled()) {
        return 0.0f;
    }

    return soundVolume;
}

@Override
public void setVolume(float volume) {
    Gdx.app.log(TAG, "Updating sound volume to " + volume);
    this.soundVolume = volume;
    refreshVolume();
}

private void refreshVolume() {
    //...
}

@Override
public void addManagedSound(WeakReference<Sound> sound) {
managedSounds.add(sound);
}

@Override
public boolean removeManagedSound(Object o) {
return managedSounds.remove(o);
}

}
