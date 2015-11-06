package com.google.collinsmith70.diablo.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

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
    sound = Gdx.audio.newSound(file);
    if (volumeController != null) {
        //sound.setVolume(0, volumeController.getVolume());
        volumeController.addManagedSound(new WeakReference<Sound>(sound));
    }
}

}