package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.riiablo.engine.client.component.Selectable;
import com.riiablo.engine.server.component.Object;
import com.riiablo.engine.server.event.ModeChangeEvent;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

@All(Selectable.class)
public class SelectableManager extends PassiveSystem {
  protected ComponentMapper<Selectable> mSelectable;
  protected ComponentMapper<Object> mObject;
//  protected ComponentMapper<Monster> mMonster;
//  protected ComponentMapper<Warp> mWarp;

  @Subscribe
  public void onModeChanged(ModeChangeEvent event) {
    int entityId = event.entityId;
    if (mObject.has(entityId)) {
      boolean b = mObject.get(entityId).base.Selectable[event.mode];
      setSelectable(entityId, b);
      // Note: HANDLED WITHIN ENTITY CONSTRUCTION
//    } else if (mMonster.has(entityId)) {
//      boolean b = mMonster.get(entityId).monstats2.isSel;
//      setSelectable(entityId, b);
//    } else if (mWarp.has(entityId)) {
//      setSelectable(entityId, true);
    }
  }

  public void setSelectable(int id, boolean b) {
    if (b) {
      mSelectable.create(id);
    } else {
      mSelectable.remove(id);
    }
  }
}
