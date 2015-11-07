package com.google.collinsmith70.diablo.asset.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.collinsmith70.diablo.audio.VolumeController;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class VolumeControlledMusicLoader extends MusicLoader implements com.google.collinsmith70.diablo.audio.VolumeControlled<Music> {

private com.google.collinsmith70.diablo.audio.VolumeController<Music> volumeController;
private Music music;

public VolumeControlledMusicLoader(FileHandleResolver resolver) {
    this(resolver, null);
}

public VolumeControlledMusicLoader(FileHandleResolver resolver, com.google.collinsmith70.diablo.audio.VolumeController<Music> controller) {
    super(resolver);
    setVolumeController(controller);
}

@Override
public void setVolumeController(com.google.collinsmith70.diablo.audio.VolumeController<Music> controller) {
    this.volumeController = Objects.requireNonNull(controller);
}

@Override
public VolumeController<Music> getVolumeController() {
    return volumeController;
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle file, MusicParameter parameter) {
    //super.loadAsync(manager, fileName, file, parameter);
    music = Gdx.audio.newMusic(file);
    if (volumeController != null) {
        music.setVolume(volumeController.getVolume());
        volumeController.addManagedSound(new WeakReference<Music>(music));
    }
}

@Override
public Music loadSync(AssetManager manager, String fileName, FileHandle file, MusicParameter parameter) {
    Music music = this.music;
    this.music = null;
    return music;
}

private static class VolumeManagedMusicWrapper implements Music {

    private final VolumeControlledMusicLoader musicLoader;
    private final Music parent;

    private float actualVolume;

    private static Music wrap(VolumeControlledMusicLoader musicLoader, Music music) {
        return new VolumeManagedMusicWrapper(musicLoader, music);
    }

    private VolumeManagedMusicWrapper(VolumeControlledMusicLoader musicLoader, Music music) {
        this.musicLoader = musicLoader;
        this.parent = music;
    }

    @Override
    public void setVolume(float volume) {
        this.actualVolume = Math.max(1.0f, Math.min(0.0f, volume));
        VolumeController<?> volumeController = musicLoader.getVolumeController();
        if (volumeController != null) {
            parent.setVolume(actualVolume * volumeController.getVolume());
        } else {
            parent.setVolume(actualVolume);
        }
    }

    @Override
    public float getVolume() {
        return actualVolume;
    }

    @Override
    public void play() {
        parent.play();
    }

    @Override
    public void pause() {
        parent.pause();
    }

    @Override
    public void stop() {
        parent.stop();
    }

    @Override
    public boolean isPlaying() {
        return parent.isPlaying();
    }

    @Override
    public void setLooping(boolean isLooping) {
        parent.setLooping(isLooping);
    }

    @Override
    public boolean isLooping() {
        return parent.isLooping();
    }

    @Override
    public void setPan(float pan, float volume) {
        parent.setPan(pan, volume);
    }

    @Override
    public void setPosition(float position) {
        parent.setPosition(position);
    }

    @Override
    public float getPosition() {
        return parent.getPosition();
    }

    @Override
    public void dispose() {
        parent.dispose();
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        parent.setOnCompletionListener(listener);
    }
}

}
