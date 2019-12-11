package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.event.ZoneChangeEvent;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

public class ZoneMovementModesChanger extends PassiveSystem {
  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<MovementModes> mMovementModes;

  @Subscribe
  public void onZoneChanged(ZoneChangeEvent event) {
    if (!mPlayer.has(event.entityId)) return;
    MovementModes movementModes = mMovementModes.get(event.entityId);
    if (event.zone.isTown()) {
      movementModes.NU = Engine.Player.MODE_TN;
      movementModes.WL = Engine.Player.MODE_TW;
      movementModes.RN = Engine.Player.MODE_RN;
    } else {
      movementModes.NU = Engine.Player.MODE_NU;
      movementModes.WL = Engine.Player.MODE_WL;
      movementModes.RN = Engine.Player.MODE_RN;
    }
  }
}
