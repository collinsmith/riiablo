package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.event.AnimDataFinishedEvent;

import net.mostlyoriginal.api.event.common.EventSystem;

@All(AnimData.class)
public class AnimStepper extends IntervalIteratingSystem {
  protected ComponentMapper<AnimData> mAnimData;

  protected EventSystem events;

  public AnimStepper() {
    super(null, Animation.FRAME_DURATION);
  }

  @Override
  protected void process(int entityId) {
    AnimData animData = mAnimData.get(entityId);
    animData.frame += animData.override >= 0 ? animData.override : animData.speed;
    if (animData.frame >= animData.numFrames) {
      animData.frame -= animData.numFrames;
      events.dispatch(AnimDataFinishedEvent.obtain(entityId));
    }
  }
}
