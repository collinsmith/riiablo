package com.riiablo.entity;

import com.badlogic.gdx.Gdx;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.excel.Objects;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DS1;
import com.riiablo.map.Map;
import com.riiablo.screen.GameScreen;

import java.util.Arrays;

public class Object extends Entity {
  private static final String TAG = "Object";

  private static final byte[] DEFAULT_COMPONENTS;
  static {
    DEFAULT_COMPONENTS = new byte[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_COMPONENTS, (byte) 1);
  }

  public static final byte MODE_NU = 0;
  public static final byte MODE_OP = 1;
  public static final byte MODE_ON = 2;
  public static final byte MODE_S1 = 3;
  public static final byte MODE_S2 = 4;
  public static final byte MODE_S3 = 5;
  public static final byte MODE_S4 = 6;
  public static final byte MODE_S5 = 7;

  public final Map           map;
  public final DS1.Object    object;
  public final Objects.Entry base;

  public static Object create(Map map, Map.Zone zone, DS1 ds1, DS1.Object object) {
    assert object.type == DS1.Object.STATIC_TYPE;
    int id = Riiablo.files.obj.getType2(ds1.getAct(), object.id);
    Objects.Entry base = Riiablo.files.objects.get(id);
    if (base == null) {
      Gdx.app.error(TAG, "Unknown static entity id: " + id + "; object=" + object);
      return null;
    }
    //if (!base.Draw) return null;
    return new Object(map, zone, object, base);
  }

  Object(Map map, Map.Zone zone, DS1.Object object, Objects.Entry base) {
    super(Type.OBJ, base.Description, base.Token);
    this.map = map;
    this.object = object;
    this.base = base;
    setComponents(DEFAULT_COMPONENTS);
    if (base.SubClass == 64) {
      String levelName = Riiablo.string.lookup(zone.level.LevelName);
      name(String.format("%s\n%s", levelName, Riiablo.string.lookup(base.Name)));
    } else {
      name(base.Name.equalsIgnoreCase("dummy") ? base.Description : Riiablo.string.lookup(base.Name));
    }
    init();
  }

  @Override
  public byte getNeutralMode() {
    return MODE_NU;
  }

  @Override
  public float getLabelOffset() {
    return -base.NameOffset;
  }

  @Override
  protected void updateCOF() {
    if (!base.Draw) return;
    super.updateCOF();
    animation.setLooping(base.CycleAnim[mode]);
    animation.setFrame(base.Start[mode]);
    animation.setFrameDelta(base.FrameDelta[mode]);
  }

  @Override
  public void draw(PaletteIndexedBatch batch) {
    if (base.Draw) super.draw(batch);
  }

  @Override
  public void drawShadow(PaletteIndexedBatch batch) {
    if (base.Draw && base.BlocksLight[mode]) super.drawShadow(batch);
  }

  @Override
  public boolean sequence(byte transition, byte mode) {
    assert !base.CycleAnim[transition];
    return super.sequence(transition, mode);
  }

  @Override
  public boolean isSelectable() {
    return base.Selectable[mode];
  }

  @Override
  public float getInteractRange() {
    return base.OperateRange;
  }

  @Override
  public void interact(GameScreen gameScreen) {
    if (base.OperateFn == 0) return;
    operate(gameScreen);
  }

  public int getOrderFlag() {
    return base.OrderFlag[mode];
  }

  private void init() {
    switch (base.InitFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7:
        break;
      case 8: // torch
        setMode(MODE_ON);

        // FIXME: Set random start frame?
        //int framesPerDir = animation.getNumFramesPerDir();
        //animation.setFrame(MathUtils.random(0, framesPerDir - 1));
        break;
      case 9 : case 10: case 11: case 12: case 13: case 14: case 15: case 16:
        break;
      case 17: // waypoint
        // TODO: Set ON based on save file
        setMode(MODE_ON);
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
        Gdx.app.error(TAG, "Invalid InitFn for " + name() + ": " + base.InitFn);
    }
  }

  private void operate(GameScreen gameScreen) {
    switch (base.OperateFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
      case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19:
      case 20: case 21: case 22:
        break;
      case 23: // waypoint
        if (mode == MODE_NU) {
          sequence(MODE_OP, MODE_ON);
          Riiablo.audio.play("object_waypoint_open", true);
        } else {
          gameScreen.waygatePanel.setVisible(true);
        }
        break;
      case 24: case 25: case 26: case 27: case 28: case 29:
      case 30: case 31:
        break;
      case 32: // stash
        gameScreen.stashPanel.setVisible(true);
        gameScreen.inventoryPanel.setVisible(true);
        break;
      case 33: case 34: case 35: case 36: case 37: case 38: case 39:
      case 40: case 41: case 42: case 43: case 44: case 45: case 46: case 47: case 48: case 49:
      case 50: case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58: case 59:
      case 60: case 61: case 62: case 63: case 64: case 65: case 66: case 67: case 68: case 69:
      case 70: case 71: case 72: case 73:
        break;
      default:
        Gdx.app.error(TAG, "Invalid OperateFn for " + name() + ": " + base.OperateFn);
    }
  }

  private void populate() {
    switch (base.PopulateFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
        break;
      default:
        Gdx.app.error(TAG, "Invalid PopulateFn for " + name() + ": " + base.PopulateFn);
    }
  }

  private void client() {
    switch (base.ClientFn) {
      case 0:
        break;
      case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
        break;
      // Rogue Bonfire
      case 10:
        break;
      case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18:
        break;
      default:
        Gdx.app.error(TAG, "Invalid ClientFn for " + name() + ": " + base.ClientFn);
    }
  }
}
