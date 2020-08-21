package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;

import com.riiablo.codec.Animation;
import com.riiablo.engine.client.component.Overlay;

@All(Overlay.class)
public class OverlayStepper extends IntervalIteratingSystem {
  protected ComponentMapper<Overlay> mOverlay;

  public OverlayStepper() {
    super(null, Animation.FRAME_DURATION);
  }

  @Override
  protected void process(int entityId) {
    mOverlay.get(entityId).animation.update(Animation.FRAME_DURATION);
  }
}
