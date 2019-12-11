package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.riiablo.Riiablo;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.engine.server.component.Object;
import com.riiablo.engine.server.component.Sequence;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class ObjectInteractor extends PassiveSystem implements Interactable.Interactor {
  private static final String TAG = "ObjectInteractor";

  protected ComponentMapper<Object> mObject;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<Sequence> mSequence;

  @Override
  public void interact(int src, int entityId) {
    operate(src, entityId, mObject.get(entityId).base.OperateFn);
  }

  private void operate(int src, int entityId, int operateFn) {
    switch (operateFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
      case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19:
      case 20: case 21: case 22:
        break;
      case 23: { // waypoint
        CofReference cofComponent = mCofReference.get(entityId);
        if (cofComponent.mode == Engine.Object.MODE_NU) {
          mSequence.create(entityId).sequence(Engine.Object.MODE_OP, Engine.Object.MODE_ON);
          Riiablo.audio.play("object_waypoint_open", true);
        } else if (cofComponent.mode == Engine.Object.MODE_ON) {
          Riiablo.game.setLeftPanel(Riiablo.game.waygatePanel);
        }
      }
        break;
      case 24: case 25: case 26: case 27: case 28: case 29:
      case 30: case 31:
        break;
      case 32: // stash
        Riiablo.game.setLeftPanel(Riiablo.game.stashPanel);
        break;
      case 33: case 34: case 35: case 36: case 37: case 38: case 39:
      case 40: case 41: case 42: case 43: case 44: case 45: case 46: case 47: case 48: case 49:
      case 50: case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58: case 59:
      case 60: case 61: case 62: case 63: case 64: case 65: case 66: case 67: case 68: case 69:
      case 70: case 71: case 72: case 73:
        break;
      default:
        Gdx.app.error(TAG, "Invalid OperateFn for " + entityId + ": " + operateFn);
    }
  }
}
