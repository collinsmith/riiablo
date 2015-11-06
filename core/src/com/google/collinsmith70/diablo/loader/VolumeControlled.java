package com.google.collinsmith70.diablo.loader;

public interface VolumeControlled<T> {

void setVolumeController(VolumeController<T> controller);
VolumeController<T> getVolumeController();

}
