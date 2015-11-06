package com.google.collinsmith70.diablo.loader;

import java.lang.ref.WeakReference;

public interface VolumeController<T> {

float getVolume();
void setVolume(float volume);
void addManagedSound(WeakReference<T> sound);
boolean removeManagedSound(Object o);

}
