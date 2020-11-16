package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.engine.server.event.AnimDataFinishedEvent;

import net.mostlyoriginal.api.event.common.Subscribe;

@All({CofReference.class, Sequence.class, AnimData.class})
public class SequenceHandler extends IteratingSystem {
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<Sequence> mSequence;
  protected ComponentMapper<AnimData> mAnimData;

  protected CofManager cofs;

  @Subscribe
  public void onAnimDataFinished(AnimDataFinishedEvent event) {
    if (!mSequence.has(event.entityId)) return;
    Sequence sequence = mSequence.get(event.entityId);
    cofs.setMode(event.entityId, sequence.mode2);
    mAnimData.get(event.entityId).override = -1;
    mSequence.remove(event.entityId);
  }

  @Override
  protected void process(int entityId) {
    Sequence sequence = mSequence.get(entityId);
    if (mCofReference.get(entityId).mode != sequence.mode1) {
      cofs.setMode(entityId, sequence.mode1);
      mAnimData.get(entityId).override = -1;
    }
  }
}
