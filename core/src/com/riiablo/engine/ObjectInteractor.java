package com.riiablo.engine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.riiablo.Riiablo;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.ObjectComponent;

public class ObjectInteractor implements InteractableComponent.Interactor {
  private static final String TAG = "ObjectInteractor";

  private final ComponentMapper<ObjectComponent> objectComponent = ComponentMapper.getFor(ObjectComponent.class);

  @Override
  public void interact(Entity src, Entity entity) {
    ObjectComponent objectComponent = this.objectComponent.get(entity);
    operate(src, entity, objectComponent.base.OperateFn);
  }

  private void operate(Entity src, Entity entity, int operateFn) {
    switch (operateFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
      case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19:
      case 20: case 21: case 22:
        break;
      case 23: // waypoint
        //if (mode == MODE_NU) {
        //  sequence(MODE_OP, MODE_ON);
        //  Riiablo.audio.play("object_waypoint_open", true);
        //} else {
          Riiablo.game.setLeftPanel(Riiablo.game.waygatePanel);
        //}
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
        Gdx.app.error(TAG, "Invalid OperateFn for " + entity + ": " + operateFn);
    }
  }
}
