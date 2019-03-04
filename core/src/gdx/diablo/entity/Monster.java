package gdx.diablo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import org.apache.commons.lang3.StringUtils;

import gdx.diablo.Diablo;
import gdx.diablo.ai.AI;
import gdx.diablo.ai.Npc;
import gdx.diablo.codec.excel.MonStats;
import gdx.diablo.codec.excel.MonStats2;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.map.DS1;
import gdx.diablo.map.DT1.Tile;
import gdx.diablo.map.Map;
import gdx.diablo.screen.GameScreen;

public class Monster extends Entity {
  private static final String TAG = "Monster";

  public final Map             map;
  public final DS1.Object      object;
  public final MonStats.Entry  monstats;
  public final MonStats2.Entry monstats2;

  AI ai;

  public Monster(Map map, DS1.Object object, MonStats.Entry monstats) {
    super(monstats.Code, EntType.MONSTER);
    this.map = map;
    this.object = object;
    this.monstats = monstats;
    this.monstats2 = Diablo.files.monstats2.get(monstats.MonStatsEx);
    className = monstats.Id;
    setName(monstats.NameStr.equalsIgnoreCase("dummy") ? monstats.Id : Diablo.string.lookup(monstats.NameStr));
    setWeaponClass(monstats2.BaseW);
    setMode(monstats.spawnmode.isEmpty() ? "NU" : monstats.spawnmode);
    setWalkSpeed(monstats.Velocity);
    setRunSpeed(monstats.Run);
    for (int i = 0; i < monstats2.ComponentV.length; i++) {
      String ComponentV = monstats2.ComponentV[i];
      if (!ComponentV.isEmpty()) {
        String[] v = StringUtils.remove(ComponentV, '"').split(",");
        int random = MathUtils.random(0, v.length - 1);
        setArmType(Component.valueOf(i), v[random]);
      }
    }
  }

  public static Monster create(Map map, Map.Zone zone, DS1 ds1, DS1.Object obj) {
    assert obj.type == DS1.Object.DYNAMIC_TYPE;

    String id = Diablo.files.obj.getType1(ds1.getAct(), obj.id);
    MonStats.Entry monstats = Diablo.files.monstats.get(id);
    Gdx.app.debug(TAG, "Monster: " + monstats);
    if (monstats == null) return null; // TODO: Which ones fall under this case? Some static entities did, none here yet in testing.
    //if (!object.Draw) return null; // TODO: Not yet

    Monster monster = new Monster(map, obj, monstats);
    if (monstats.AI.equalsIgnoreCase("Idle")) {
      monster.ai = AI.IDLE;
    } else if (monstats.AI.equalsIgnoreCase("Npc")) {
      monster.ai = new Npc(monster);
    }

    return monster;
  }

  @Override
  public void drawDebugPath(PaletteIndexedBatch batch, ShapeRenderer shapes) {
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
      Diablo.fonts.consolas12.draw(batch, Integer.toString(point.action), p1x, p1y - BOX_SIZE, 0, Align.center, false);
    }
    batch.end();
    batch.setShader(Diablo.shader);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
  }

  @Override
  public float getLabelOffset() {
    return monstats2.pixHeight;
  }

  @Override
  public boolean contains(Vector2 coords) {
    if (!monstats2.isSel) return false;
    return super.contains(coords);
  }

  @Override
  public boolean contains(Vector3 coords) {
    if (!monstats2.isSel) return false;
    return super.contains(coords);
  }

  @Override
  public void update(float delta) {
    if (ai != null) ai.update(delta);
    super.update(delta);
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
  public void drawShadow(PaletteIndexedBatch batch) {
    if (monstats2.Shadow) super.drawShadow(batch);
  }

  @Override
  public boolean isSelectable() {
    return monstats2.isSel;
  }
}
