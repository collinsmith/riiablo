package com.google.collinsmith70.diablo.audio;

import java.lang.ref.WeakReference;

public interface VolumeController<T> {

boolean isEnabled();
void setEnabled(boolean enabled);

float getVolume();
void setVolume(float volume);

void addManagedSound(WeakReference<T> sound);
boolean removeManagedSound(Object o);

}
