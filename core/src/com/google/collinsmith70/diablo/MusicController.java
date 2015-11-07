package com.google.collinsmith70.diablo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

public class MusicController implements Disposable, Music.OnCompletionListener {

private final Collection<MusicControllerListener> LISTENERS;
private final AssetManager ASSET_MANAGER;
private final Deque<AssetDescriptor<Music>> QUEUE;

private AssetDescriptor<Music> asset;
private Music track;

public MusicController(AssetManager assetManager) {
    this.LISTENERS = new CopyOnWriteArraySet<MusicControllerListener>();
    this.ASSET_MANAGER = assetManager;
    this.QUEUE = new LinkedList<AssetDescriptor<Music>>();
}

public void addMusicControllerListener(MusicControllerListener l) {
    LISTENERS.add(l);
}

public boolean containsMusicControllerListener(MusicControllerListener l) {
    return LISTENERS.contains(l);
}

public boolean removeMusicControllerListener(MusicControllerListener l) {
    return LISTENERS.remove(l);
}

public int size() {
    return QUEUE.size();
}

public boolean isEmpty() {
    return QUEUE.isEmpty();
}

public void clear() {
    stop();
    QUEUE.clear();
}

public void stop() {
    if (track == null) {
        return;
    }

    ASSET_MANAGER.unload(asset.fileName);
    track.dispose();
    track = null;
}

public void enqueue(AssetDescriptor<Music> music) {
    QUEUE.addLast(music);
    if (!isPlaying()) {
        play();
    }
}

private AssetDescriptor<Music> dequeue() {
    return QUEUE.removeFirst();
}

public boolean isPlaying() {
    return track != null && track.isPlaying();
}

public void play() {
    next();
}

public void play(AssetDescriptor<Music> music) {
    QUEUE.addFirst(music);
    play();
}

public void next() {
    stop();
    if (isEmpty()) {
        return;
    }

    asset = dequeue();
    ASSET_MANAGER.load(asset);
    ASSET_MANAGER.finishLoadingAsset(asset.fileName);
    track = ASSET_MANAGER.get(asset);
    track.play();
    track.setOnCompletionListener(this);
    for (MusicControllerListener l : LISTENERS) {
        l.onTrackChanged(track);
    }
}

@Override
public void onCompletion(Music music) {
    next();
}

@Override
public void dispose() {
    clear();
}

}
