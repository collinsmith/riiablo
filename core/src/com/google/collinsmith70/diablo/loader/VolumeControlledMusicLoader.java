package com.google.collinsmith70.diablo.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.collinsmith70.diablo.VolumeController;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;

public class VolumeControlledMusicLoader extends MusicLoader {

private Collection<WeakReference<Music>> managedSounds;
private VolumeController volumeController;
private Music music;

public VolumeControlledMusicLoader(FileHandleResolver resolver) {
    super(resolver);
}

public void setVolumeController(VolumeController controller) {
    this.volumeController = Objects.requireNonNull(controller);
}

public VolumeController getVolumeController() {
    return volumeController;
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle file, MusicParameter parameter) {
    music = Gdx.audio.newMusic(file);
    if (volumeController != null) {
        music.setVolume(volumeController.getVolume());
    }

    managedSounds.add(new WeakReference<Music>(music));
}

}
