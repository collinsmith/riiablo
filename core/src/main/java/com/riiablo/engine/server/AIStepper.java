package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.riiablo.engine.server.component.AIWrapper;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.Position;
import com.riiablo.map.RenderSystem;

@Wire(failOnNull = false)
@All({AIWrapper.class, Position.class, Monster.class})
public class AIStepper extends IteratingSystem {
  protected ComponentMapper<AIWrapper> mAIWrapper;
//  protected ComponentMapper<Monster> mMonster;
  protected ComponentMapper<Position> mPosition;

//  protected ComponentMapper<Interactable> mInteractable;
//  protected ComponentMapper<Size> mSize;

  protected RenderSystem renderer;

// NOTE: Handled by EntityFactory
//  @Override
//  protected void inserted(int entityId) {
//    Monster monster = mMonster.get(entityId);
//    AIWrapper aiWrapper = mAIWrapper.get(entityId);
//    AI ai = aiWrapper.ai = AI.findAI(entityId, monster);
//    world.getInjector().inject(ai);
//    ai.initialize();
//    if (monster.monstats.interact) {
//      mInteractable.create(entityId).set(mSize.get(entityId).size, ai);
//    }
//  }

  @Override
  protected void process(int entityId) {
    if (renderer != null && !renderer.withinRadius(mPosition.get(entityId).position)) return;
    mAIWrapper.get(entityId).ai.update(world.delta);
  }
}
