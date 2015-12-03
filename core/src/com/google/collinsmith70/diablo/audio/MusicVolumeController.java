package com.google.collinsmith70.diablo.audio;

import com.badlogic.gdx.audio.Music;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicVolumeController implements VolumeController<Music> {

private static final String TAG = MusicVolumeController.class.getSimpleName();

private Collection<WeakReference<Music>> managedMusic;

private boolean globalSoundEnabled;
private boolean musicEnabled;
private float musicVolume;

public MusicVolumeController() {
    this.managedMusic = new CopyOnWriteArrayList<WeakReference<Music>>();

    Cvars.Client.Sound.Enabled.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            MusicVolumeController.this.globalSoundEnabled = toValue;
            MusicVolumeController.this.refreshVolume();
        }
    });

    Cvars.Client.Sound.Music.Enabled.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            MusicVolumeController.this.setEnabled(toValue);
        }
    });

    Cvars.Client.Sound.Music.Volume.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            MusicVolumeController.this.setVolume(toValue);
        }
    });
}

@Override
public boolean isEnabled() {
    return musicEnabled;
}

@Override
public void setEnabled(boolean enabled) {
    this.musicEnabled = enabled;
    refreshVolume();
    //Gdx.app.log(TAG, "Music " + (musicEnabled ? "enabled" : "disabled"));
}

@Override
public float getVolume() {
    if (!globalSoundEnabled || !isEnabled()) {
        return 0.0f;
    }

    return musicVolume;
}

@Override
public void setVolume(float volume) {
    //Gdx.app.log(TAG, "Updating music volume to " + volume);
    this.musicVolume = volume;
    refreshVolume();
}

private void refreshVolume() {
    float volume = getVolume();
    for (WeakReference<Music> container : managedMusic) {
        Music musicReference = container.get();
        if (musicReference == null) {
            managedMusic.remove(container);
            continue;
        }

        musicReference.setVolume(getVolume());
    }
}

@Override
public void addManagedSound(WeakReference<Music> music) {
    managedMusic.add(music);
}

@Override
public boolean removeManagedSound(Object o) {
    return managedMusic.remove(o);
}

}
