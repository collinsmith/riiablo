package com.google.collinsmith70.diablo.asset.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.google.collinsmith70.diablo.audio.VolumeControlled;
import com.google.collinsmith70.diablo.audio.VolumeController;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class VolumeControlledSoundLoader extends SoundLoader implements VolumeControlled<Sound> {

private VolumeController<Sound> volumeController;
private Sound sound;

public VolumeControlledSoundLoader(FileHandleResolver resolver) {
    super(resolver);
}

public VolumeControlledSoundLoader(FileHandleResolver resolver, VolumeController<Sound> controller) {
    super(resolver);
    setVolumeController(controller);
}

@Override
public void setVolumeController(VolumeController<Sound> controller) {
    this.volumeController = Objects.requireNonNull(controller);
}

@Override
public VolumeController<Sound> getVolumeController() {
    return volumeController;
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter) {
    //super.loadAsync(manager, fileName, file, parameter);
    sound = VolumeManagedSoundWrapper.wrap(this, Gdx.audio.newSound(file));
    if (volumeController != null) {
        volumeController.addManagedSound(new WeakReference<Sound>(sound));
    }
}

@Override
public Sound loadSync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter) {
    Sound sound = this.sound;
    this.sound = null;
    return sound;
}

private static class VolumeManagedSoundWrapper implements Sound {

    private final VolumeControlledSoundLoader soundLoader;
    private final Sound parent;

    private static Sound wrap(VolumeControlledSoundLoader soundLoader, Sound sound) {
        return new VolumeManagedSoundWrapper(soundLoader, sound);
    }

    private VolumeManagedSoundWrapper(VolumeControlledSoundLoader soundLoader, Sound sound) {
        this.soundLoader = soundLoader;
        this.parent = sound;
    }

    @Override
    public long play() {
        return play(1.0f);
    }

    @Override
    public long play(float volume) {
        return play(1.0f, 1.0f, 0.0f);
    }

    @Override
    public long play(float volume, float pitch, float pan) {
        VolumeController<?> volumeController = soundLoader.getVolumeController();
        if (volumeController != null) {
            volume *= volumeController.getVolume();
        }

        return parent.play(volume, pitch, pan);
    }

    @Override
    public long loop() {
        return parent.loop(1.0f);
    }

    @Override
    public long loop(float volume) {
        return parent.loop(1.0f, 1.0f, 0.0f);
    }

    @Override
    public long loop(float volume, float pitch, float pan) {
        VolumeController<?> volumeController = soundLoader.getVolumeController();
        if (volumeController != null) {
            volume *= volumeController.getVolume();
        }

        return parent.loop(volume, pitch, pan);
    }

    @Override
    public void stop() {
        parent.stop();
    }

    @Override
    public void pause() {
        parent.pause();
    }

    @Override
    public void resume() {
        parent.resume();
    }

    @Override
    public void dispose() {
        parent.dispose();
    }

    @Override
    public void stop(long soundId) {
        parent.stop();
    }

    @Override
    public void pause(long soundId) {
        parent.pause(soundId);
    }

    @Override
    public void resume(long soundId) {
        parent.resume(soundId);
    }

    @Override
    public void setLooping(long soundId, boolean looping) {
        parent.setLooping(soundId, looping);
    }

    @Override
    public void setPitch(long soundId, float pitch) {
        parent.setPitch(soundId, pitch);
    }

    @Override
    public void setVolume(long soundId, float volume) {
        parent.setVolume(soundId, volume);
    }

    @Override
    public void setPan(long soundId, float pan, float volume) {
        parent.setPan(soundId, pan, volume);
    }

    @Override
    public void setPriority(long soundId, int priority) {
        parent.setPriority(soundId, priority);
    }
}

}