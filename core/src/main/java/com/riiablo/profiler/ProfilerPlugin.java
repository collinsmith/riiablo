package com.riiablo.profiler;

import com.artemis.ArtemisPlugin;
import com.artemis.WorldConfigurationBuilder;

/**
 * Artemis system profiler.
 *
 * Tracks performance of artemis systems and displays it in a line graph. Overhead is insignificant
 * while closed.
 *
 * Does not require {@see @com.artemis.annotations.Profile} on systems.
 *
 * Open/Close with P by default.
 *
 * @author piotr-j (Plugin)
 * @author Daan van Yperen (Integration)
 */
public class ProfilerPlugin implements ArtemisPlugin {
  @Override
  public void setup(WorldConfigurationBuilder b) {
    b.register(new ProfilerInvocationStrategy());
    b.dependsOn(WorldConfigurationBuilder.Priority.LOWEST + 1000, ProfilerManager.class, ProfilerSystem.class);
  }
}
