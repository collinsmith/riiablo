package gdx.diablo.entity2;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import java.util.EnumMap;

import gdx.diablo.codec.Animation;
import gdx.diablo.graphics.PaletteIndexedBatch;

public class Entity {

  /**
   * x, y   (src)
   * angle  (radians)
   * scalar (movement speed)
   *
   * x, y   (dst)
   */
  Vector2 origin;
  Vector2 velocity;

  String mode;
  String code;
  String armorType;
  String weaponClass;

  EnumMap<Component, Animation.Layer> components = new EnumMap<>(Component.class);
  Animation.COFAnimation animation;

  Entity() {
    mode = code = "NU";
    armorType   = "LIT";
    weaponClass = "HTH";
  }

  public void setMode(String mode) {
    setMode(mode, mode);
  }

  public void setMode(String mode, String code) {
    this.mode = mode;
    this.code = code;
  }

  /*
  Entity(Excel<M> excel, String mode) {
    this(excel.get(mode));
  }

  Entity(Excel<M> excel) {
    this(excel, "NU");
  }
  */

  /*
  public M getMode() {
    return mode;
  }

  public void setMode(M mode) {
    this.mode = mode;
  }

  public String getCode() {
    return mode.getCode();
  }
  */

  public int getX() {
    return 0;
  }

  public int getY() {
    return 0;
  }

  public void setComponent(Component component, Animation.Layer value) {
  }

  public void draw(Batch batch) {
    draw((PaletteIndexedBatch) batch);
  }

  public void draw(PaletteIndexedBatch batch) {
  }
}
