package com.riiablo.profiler;

import com.artemis.BaseSystem;
import com.artemis.World;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class ProfilerManager extends PassiveSystem {
  private static Array<SystemProfiler> profilers = new Array<>(SystemProfiler.class);
  private static ObjectMap<String, SystemProfiler> profilerByName = new ObjectMap<>();

  private boolean RUNNING = true;

  @Override
  protected void dispose() {
    profilers.clear();
    profilerByName.clear();
  }

  public void pause() {
    RUNNING = false;
  }

  public void resume() {
    RUNNING = true;
  }

  public boolean isRunning() {
    return RUNNING;
  }

  Array<SystemProfiler> get() {
    return profilers;
  }

  public int size() {
    return profilers.size;
  }

  public SystemProfiler add(SystemProfiler profiler) {
    if (profiler.added) return profiler;
    profiler.added = true;
    profilers.add(profiler);
    profilerByName.put(profiler.getName(), profiler);
    return profiler;
  }

  public SystemProfiler get(String name) {
    return profilerByName.get(name, null);
  }

  public SystemProfiler get(int index) {
    return profilers.get(index);
  }

  public SystemProfiler create(String name) {
    SystemProfiler profiler = get(name);
    if (profiler != null) return profiler;
    return add(new SystemProfiler(name));
  }

  public SystemProfiler getFor(BaseSystem system) {
    final SystemProfiler[] profilers = this.profilers.items;
    for (int i = 0, s = this.profilers.size; i < s; i++) {
      SystemProfiler profiler = profilers[i];
      if (profiler.system == system) {
        return profiler;
      }
    }
    return null;
  }

  public SystemProfiler createFor(BaseSystem system, World world) {
    return add(new SystemProfiler(system, world));
  }
}
