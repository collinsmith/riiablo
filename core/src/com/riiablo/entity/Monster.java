package com.riiablo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.riiablo.Riiablo;
import com.riiablo.ai.AI;
import com.riiablo.ai.Idle;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DS1;
import com.riiablo.map.DT1.Tile;
import com.riiablo.map.Map;
import com.riiablo.screen.GameScreen;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;

public class Monster extends Entity {
  private static final String TAG = "Monster";

  public static final byte MODE_DT =  0;
  public static final byte MODE_NU =  1;
  public static final byte MODE_WL =  2;
  public static final byte MODE_GH =  3;
  public static final byte MODE_A1 =  4;
  public static final byte MODE_A2 =  5;
  public static final byte MODE_BL =  6;
  public static final byte MODE_SC =  7;
  public static final byte MODE_S1 =  8;
  public static final byte MODE_S2 =  9;
  public static final byte MODE_S3 = 10;
  public static final byte MODE_S4 = 11;
  public static final byte MODE_DD = 12;
  //public static final byte MODE_GH = 13;
  public static final byte MODE_XX = 14;
  public static final byte MODE_RN = 15;

  public final Map             map;
  public final DS1.Object      object;
  public final MonStats.Entry  monstats;
  public final MonStats2.Entry monstats2;

  AI ai;

  public static Monster create(Map map, Map.Zone zone, DS1 ds1, DS1.Object object) {
    assert object.type == DS1.Object.DYNAMIC_TYPE;
    String id = Riiablo.files.obj.getType1(ds1.getAct(), object.id);
    MonStats.Entry monstats = Riiablo.files.monstats.get(id);
    if (monstats == null) {
      Gdx.app.error(TAG, "Unknown dynamic entity id: " + id + "; object=" + object);
      return null;
    }

    Monster monster = new Monster(map, zone, object, monstats);
    monster.ai = findAI(monster);
    return monster;
  }

  public static Monster create(Map map, Map.Zone zone, MonStats.Entry monstats) {
    Monster monster = new Monster(map, zone, null, monstats);
    monster.ai = findAI(monster);
    return monster;
  }

  private static AI findAI(Monster monster) {
    try {
      Class clazz = Class.forName("com.riiablo.ai." + monster.monstats.AI);
      if (clazz == Idle.class) return AI.IDLE;
      Constructor constructor = clazz.getConstructor(Monster.class);
      return (AI) constructor.newInstance(monster);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      return AI.IDLE;
    }
  }

  Monster(Map map, Map.Zone zone, DS1.Object object, MonStats.Entry monstats) {
    super(Type.MON, monstats.Id, monstats.Code);
    this.map = map;
    this.object = object;
    this.monstats = monstats;
    this.monstats2 = Riiablo.files.monstats2.get(monstats.MonStatsEx);
    name(monstats.NameStr.equalsIgnoreCase("dummy") ? monstats.Id : Riiablo.string.lookup(monstats.NameStr));
    setWeapon((byte) Riiablo.files.WeaponClass.index(monstats2.BaseW));
    setMode(monstats.spawnmode.isEmpty() ? MODE_NU : (byte) Riiablo.files.MonMode.index(monstats.spawnmode));
    setWalkSpeed(monstats.Velocity);
    setRunSpeed(monstats.Run);
    for (byte i = 0; i < monstats2.ComponentV.length; i++) {
      String ComponentV = monstats2.ComponentV[i];
      if (!ComponentV.isEmpty()) {
        String[] v = StringUtils.remove(ComponentV, '"').split(",");
        int random = MathUtils.random(0, v.length - 1);
        setComponent(i, (byte) Riiablo.files.compcode.index(v[random]));
      }
    }
  }

  @Override
  public byte getNeutralMode() {
    return MODE_NU;
  }

  @Override
  public byte getWalkMode() {
    return MODE_WL;
  }

  @Override
  public byte getRunMode() {
    return MODE_RN;
  }

  @Override
  public float getLabelOffset() {
    return monstats2.pixHeight;
  }

  @Override
  public boolean isSelectable() {
    return monstats2.isSel;
  }

  @Override
  public float getInteractRange() {
    // FIXME: SizeX and SizeY appear to always be equal -- is this method sufficient?
    return monstats2.SizeX;
  }

  @Override
  public void interact(GameScreen gameScreen) {
    if (!monstats.interact) return;
    if (ai != null) ai.interact(gameScreen);
  }

  @Override
  public void update(float delta) {
    if (ai != null) ai.update(delta);
    super.update(delta);
  }

  @Override
  public void drawShadow(PaletteIndexedBatch batch) {
    if (monstats2.Shadow) super.drawShadow(batch);
  }

  @Override
  public void drawDebugPath(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    if (object == null) return;
    DS1.Path path = object.path;
    if (path == null) return;
    DS1.Path.Point point;
    float p1x = 0, p1y = 0;
    float p2x = 0, p2y = 0;
    for (int i = 0; i < path.numPoints; i++, p1x = p2x, p1y = p2y) {
      point = path.points[i];
      if (p1x == 0 && p1y == 0) {
        p2x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
        p2y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
        continue;
      }

      p2x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p2y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      shapes.setColor(Color.PURPLE);
      shapes.rectLine(p1x, p1y, p2x, p2y, 2);
    }

    if (path.numPoints > 1) {
      point = path.points[0];
      p1x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p1y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      shapes.setColor(Color.PURPLE);
      shapes.rectLine(p1x, p1y, p2x, p2y, 2);
    }

    final float BOX_SIZE = 8;
    final float HALF_BOX = BOX_SIZE / 2;
    p1x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    p1y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    shapes.setColor(Color.WHITE);
    shapes.rect(p1x - HALF_BOX, p1y - HALF_BOX, BOX_SIZE, BOX_SIZE);
    for (int i = 0; i < path.numPoints; i++) {
      point = path.points[i];
      p1x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p1y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      shapes.setColor(Color.WHITE);
      shapes.rect(p1x - HALF_BOX, p1y - HALF_BOX, BOX_SIZE, BOX_SIZE);
    }

    shapes.end();
    batch.begin();
    batch.setShader(null);
    for (int i = 0; i < path.numPoints; i++) {
      point = path.points[i];
      p1x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p1y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      Riiablo.fonts.consolas12.draw(batch, Integer.toString(point.action), p1x, p1y - BOX_SIZE, 0, Align.center, false);
    }
    batch.end();
    batch.setShader(Riiablo.shader);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
  }
}
