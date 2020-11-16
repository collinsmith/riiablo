package com.riiablo.profiler;

import com.artemis.BaseSystem;
import com.artemis.SystemInvocationStrategy;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;

import com.riiablo.Riiablo;

/**
 * {@link SystemInvocationStrategy} that will create a profiler for all systems that don't already
 * have one Can be used in addition to or instead of {@link com.artemis.annotations.Profile}
 * annotation
 * <p>
 * In addition creates {@link SystemProfiler} with name "Frame" for total frame time It can be
 * accessed with {@link SystemProfiler#get(String)}
 *
 * @author piotr-j
 * @author Daan van Yperen
 */
public class ProfilerInvocationStrategy extends SystemInvocationStrategy {
  protected ProfilerManager profilerManager;

  private boolean initialized = false;

  protected SystemProfiler frameProfiler;
  protected SystemProfiler cpuProfiler;
  protected SystemProfiler gpuProfiler;
  protected SystemProfiler[] profilers;

  @Override
  protected void process() {
    if (!initialized) {
      initialize();
      initialized = true;
    }

    frameProfiler.start();
    processProfileSystems(systems);
    frameProfiler.stop();

    cpuProfiler.sample = gpuProfiler.sample = 0;
    for (int i = 0, s = systems.size(); s > i; i++) {
      if (disabled.get(i)) continue;
      SystemProfiler profiler = profilers[i];
      if (profiler == null) continue;
      SystemProfiler active = profiler.gpu ? gpuProfiler : cpuProfiler;
      active.sample += profiler.sample;
    }

    cpuProfiler.sample(cpuProfiler.sample);
    gpuProfiler.sample(gpuProfiler.sample);

    Riiablo.metrics.cpu = cpuProfiler.getMovingAvg();
    Riiablo.metrics.gpu = gpuProfiler.getMovingAvg();
  }

  private void processProfileSystems(Bag<BaseSystem> systems) {
    final Object[] systemsData = systems.getData();
    for (int i = 0, s = systems.size(); s > i; i++) {
      if (disabled.get(i))
        continue;

      updateEntityStates();
      processProfileSystem(profilers[i], (BaseSystem) systemsData[i]);
    }

    updateEntityStates();
  }

  private void processProfileSystem(SystemProfiler profiler, BaseSystem system) {
    if (profiler != null) profiler.start();
    system.process();
    if (profiler != null) profiler.stop();
  }

  @Override
  protected void initialize() {
    world.inject(this);
    createFrameProfiler();
    createCpuProfiler();
    createGpuProfiler();
    createSystemProfilers();
  }

  private void createSystemProfilers() {
    final ImmutableBag<BaseSystem> systems = world.getSystems();
    profilers = new SystemProfiler[systems.size()];
    for (int i = 0; i < systems.size(); i++) {
      profilers[i] = createSystemProfiler(systems.get(i));
    }
  }

  private SystemProfiler createSystemProfiler(BaseSystem system) {
    SystemProfiler old = profilerManager.getFor(system);
    if (old == null) {
      old = profilerManager.createFor(system, world);
    }
    return old;
  }

  private void createFrameProfiler() {
    frameProfiler = profilerManager.create("Frame");
    frameProfiler.setColor(1, 1, 1, 1);
  }

  private void createCpuProfiler() {
    cpuProfiler = profilerManager.create("CPU");
    cpuProfiler.setColor(0, 0, 1, 1);
  }

  private void createGpuProfiler() {
    gpuProfiler = profilerManager.create("GPU");
    gpuProfiler.setColor(0, 1, 0, 1);
  }
}
