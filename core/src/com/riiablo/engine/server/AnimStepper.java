package com.riiablo.engine.server;

import org.apache.logging.log4j.Logger;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import net.mostlyoriginal.api.event.common.EventSystem;

import com.riiablo.codec.Animation;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.event.AnimDataFinishedEvent;
import com.riiablo.engine.server.event.AnimDataKeyframeEvent;
import com.riiablo.log.LogManager;

@All(AnimData.class)
public class AnimStepper extends IntervalIteratingSystem {
  private static final Logger log = LogManager.getLogger(AnimStepper.class);

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

    final byte keyframe = animData.keyframes[animData.frame >>> 8];
    if (keyframe > Engine.KEYFRAME_NIL) {
      log.debug("broadcasting AnimDataKeyframeEvent({},{})", entityId, Engine.getKeyframe(keyframe));
      events.dispatch(AnimDataKeyframeEvent.obtain(entityId, keyframe));
    }
  }
}
