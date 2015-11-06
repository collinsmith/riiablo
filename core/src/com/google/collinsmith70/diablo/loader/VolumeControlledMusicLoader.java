package com.google.collinsmith70.diablo.loader;

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

}
