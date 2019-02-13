package gdx.diablo.entity;

import com.badlogic.gdx.Gdx;

import gdx.diablo.Diablo;
import gdx.diablo.codec.excel.Objects;
import gdx.diablo.map.DS1;

public class StaticEntity extends Entity {
  private static final String TAG = "StaticEntity";

  Objects.Entry object;

  public StaticEntity(String type, Objects.Entry object) {
    super(type, EntType.OBJECT);
    this.object = object;
    init();
  }

  public static StaticEntity create(DS1 ds1, DS1.Object obj) {
    assert obj.type == DS1.Object.STATIC_TYPE;

    int id = Diablo.files.obj.getType2(ds1.getAct(), obj.id);
    Objects.Entry object = Diablo.files.objects.get(id);
    if (object == null) return null; // TODO: Which ones fall under this case?
    if (!object.Draw) return null; // TODO: Not yet

    String type = object.Token;
    return new StaticEntity(type, object);
  }

  @Override
  protected void update() {
    super.update();
    int mode = Diablo.files.ObjMode.index(this.mode);
    //System.out.println(getName() + " " + this.mode + "(" + mode + ") " + object.FrameDelta[mode]);
    animation.setLooping(object.CycleAnim[mode]);
    animation.setFrame(object.Start[mode]);
    animation.setFrameDelta(object.FrameDelta[mode]); // FIXME: anim framedelta looks too quick
  }

  public String getName() {
    return object == null ? toString() : object.Name + "(" + object.Id + ")";
  }

  private void init() {
    switch (object.InitFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7:
        break;
      case 8: // torch
        setMode("ON");

        // FIXME: Set random start frame?
        //int framesPerDir = animation.getNumFramesPerDir();
        //animation.setFrame(MathUtils.random(0, framesPerDir - 1));
        break;
      case 9 : case 10: case 11: case 12: case 13: case 14: case 15: case 16:
        break;
      case 17: // waypoint
        // TODO: Automatically sets on
        setMode("ON");
        break;
      case 18:
      case 19: case 20: case 21: case 22: case 23: case 24: case 25: case 26: case 27: case 28:
      case 29: case 30: case 31: case 32: case 33: case 34: case 35: case 36: case 37: case 38:
      case 39: case 40: case 41: case 42: case 43: case 44: case 45: case 46: case 47: case 48:
      case 49: case 50: case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58:
      case 59: case 60: case 61: case 62: case 63: case 64: case 65: case 66: case 67: case 68:
      case 69: case 70: case 71: case 72: case 73: case 74: case 75: case 76: case 77: case 78:
      case 79:
        break;
      default:
        Gdx.app.error(TAG, "Invalid InitFn for " + getName() + ": " + object.InitFn);
    }
  }

  private void operate() {
    switch (object.OperateFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
      case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19:
      case 20: case 21: case 22: case 23: case 24: case 25: case 26: case 27: case 28: case 29:
      case 30: case 31: case 32: case 33: case 34: case 35: case 36: case 37: case 38: case 39:
      case 40: case 41: case 42: case 43: case 44: case 45: case 46: case 47: case 48: case 49:
      case 50: case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58: case 59:
      case 60: case 61: case 62: case 63: case 64: case 65: case 66: case 67: case 68: case 69:
      case 70: case 71: case 72: case 73:
        break;
      default:
        Gdx.app.error(TAG, "Invalid OperateFn for " + getName() + ": " + object.OperateFn);
    }
  }

  private void populate() {
    switch (object.PopulateFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
        break;
      default:
        Gdx.app.error(TAG, "Invalid PopulateFn for " + getName() + ": " + object.PopulateFn);
    }
  }

  private void client() {
    switch (object.ClientFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
      case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18:
        break;
      default:
        Gdx.app.error(TAG, "Invalid ClientFn for " + getName() + ": " + object.ClientFn);
    }
  }
}
