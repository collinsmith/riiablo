package gdx.diablo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import org.apache.commons.lang3.StringUtils;

import gdx.diablo.Diablo;
import gdx.diablo.codec.excel.MonStats;
import gdx.diablo.codec.excel.MonStats2;
import gdx.diablo.map.DS1;

public class Monster extends Entity {
  private static final String TAG = "Monster";

  MonStats.Entry monstats;
  MonStats2.Entry monstats2;

  public Monster(MonStats.Entry monstats) {
    super(monstats.Code, EntType.MONSTER);
    this.monstats = monstats;
    this.monstats2 = Diablo.files.monstats2.get(monstats.MonStatsEx);
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
    return new Monster(monstats);
  }

}
