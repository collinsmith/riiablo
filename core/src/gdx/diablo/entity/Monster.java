package gdx.diablo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import org.apache.commons.lang3.StringUtils;

import gdx.diablo.Diablo;
import gdx.diablo.codec.excel.MonStats;
import gdx.diablo.codec.excel.MonStats2;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.map.DS1;
import gdx.diablo.map.DT1.Tile;

public class Monster extends Entity {
  private static final String TAG = "Monster";

  DS1.Object      object;
  MonStats.Entry  monstats;
  MonStats2.Entry monstats2;

  public Monster(DS1.Object object, MonStats.Entry monstats) {
    super(monstats.Code, EntType.MONSTER);
    this.object = object;
    this.monstats = monstats;
    this.monstats2 = Diablo.files.monstats2.get(monstats.MonStatsEx);
    setName(monstats.NameStr);
    setWeaponClass(monstats2.BaseW);
    setMode(monstats.spawnmode.isEmpty() ? "NU" : monstats.spawnmode);
    for (int i = 0; i < monstats2.ComponentV.length; i++) {
      String ComponentV = monstats2.ComponentV[i];
      if (!ComponentV.isEmpty()) {
        String[] v = StringUtils.remove(ComponentV, '"').split(",");
        int random = MathUtils.random(0, v.length - 1);
        setArmType(Component.valueOf(i), v[random]);
      }
    }
  }

  public static Monster create(DS1 ds1, DS1.Object obj) {
    assert obj.type == DS1.Object.DYNAMIC_TYPE;

    String id = Diablo.files.obj.getType1(ds1.getAct(), obj.id);
    MonStats.Entry monstats = Diablo.files.monstats.get(id);
    Gdx.app.debug(TAG, "Monster: " + monstats);
    if (monstats == null) return null; // TODO: Which ones fall under this case? Some static entities did, none here yet in testing.
    //if (!object.Draw) return null; // TODO: Not yet
    return new Monster(obj, monstats);
  }

  @Override
  public void drawDebugPath(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    DS1.Path path = object.path;
    if (path == null) return;
    float p1x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float p1y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    float p2x = 0, p2y = 0;
    for (int i = 0; i < path.numPoints; i++) {
      DS1.Path.Point point = path.points[i];
      p2x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p2y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      shapes.setColor(Color.PURPLE);
      shapes.rectLine(p1x, p1y, p2x, p2y, 2);

      p1x = p2x;
      p1y = p2y;
    }

    if (path.numPoints > 1) {
      DS1.Path.Point point = path.points[0];
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
      DS1.Path.Point point = path.points[i];
      p1x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p1y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      shapes.setColor(Color.WHITE);
      shapes.rect(p1x - HALF_BOX, p1y - HALF_BOX, BOX_SIZE, BOX_SIZE);
    }

    shapes.end();
    batch.begin();
    batch.setShader(null);
    for (int i = 0; i < path.numPoints; i++) {
      DS1.Path.Point point = path.points[i];
      p1x = +(point.x * Tile.SUBTILE_WIDTH50)  - (point.y * Tile.SUBTILE_WIDTH50);
      p1y = -(point.x * Tile.SUBTILE_HEIGHT50) - (point.y * Tile.SUBTILE_HEIGHT50);
      Diablo.fonts.consolas16.draw(batch, Integer.toString(point.action), p1x, p1y - BOX_SIZE, 0, Align.center, false);
    }
    batch.end();
    batch.setShader(Diablo.shader);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
  }

  @Override
  public void drawLabel(PaletteIndexedBatch batch) {
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    label.setPosition(x, y + monstats2.pixHeight + label.getHeight(), Align.center);
    label.draw(batch, 1);
  }

  @Override
  public boolean contains(Vector3 coords) {
    if (!monstats2.isSel) return false;
    return super.contains(coords);
  }
}
