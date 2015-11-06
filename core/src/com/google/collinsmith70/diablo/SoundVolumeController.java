package com.google.collinsmith70.diablo;

import com.badlogic.gdx.audio.Sound;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.loader.VolumeController;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class SoundVolumeController implements VolumeController<Sound> {

private final Collection<WeakReference<Sound>> managedSounds;

private float soundVolume;

public SoundVolumeController() {
    this.managedSounds = new CopyOnWriteArrayList<WeakReference<Sound>>();
    Cvars.Client.Sound.Sfx.Volume.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            SoundVolumeController.this.setVolume(toValue);
        }
    });
}

@Override
public float getVolume() {
    return soundVolume;
}

@Override
public void setVolume(float volume) {
    this.soundVolume = volume;
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
