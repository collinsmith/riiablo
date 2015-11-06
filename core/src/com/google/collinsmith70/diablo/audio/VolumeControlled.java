package com.google.collinsmith70.diablo.audio;

public interface VolumeControlled<T> {

void setVolumeController(VolumeController<T> controller);
VolumeController<T> getVolumeController();

}
