package com.google.collinsmith70.diablo;

import com.badlogic.gdx.audio.Music;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.loader.VolumeController;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicVolumeController implements VolumeController<Music> {

private Collection<WeakReference<Music>> managedMusic;

private float musicVolume;

public MusicVolumeController() {
    this.managedMusic = new CopyOnWriteArrayList<WeakReference<Music>>();
    Cvars.Client.Sound.Sfx.Volume.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            MusicVolumeController.this.setVolume(toValue);
        }
    });
}

@Override
public float getVolume() {
    return musicVolume;
}

@Override
public void setVolume(float volume) {
    this.musicVolume = volume;
    for (WeakReference<Music> container : managedMusic) {
        Music musicReference = container.get();
        if (musicReference == null) {
            managedMusic.remove(container);
            continue;
        }

        musicReference.setVolume(volume);
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
