package com.riiablo.entity;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.audio.Audio;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.DCC;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DT1.Tile;
import com.riiablo.util.EngineUtils;

public class Missile extends Entity {
  public final Missiles.Entry missile;

  AssetDescriptor<DCC> imageDescriptor;
  DC image;
  int blendMode;
  Audio.Instance travelSound;
  int velocity;
  float remainingDist;
  final Vector2 tmpVec2 = new Vector2();

  public Missile(Missiles.Entry missile) {
    super(Type.MIS, missile.Missile, null);
    this.missile = missile;

    velocity = missile.Vel;
    remainingDist = missile.Range;
    switch (missile.Trans) {
      case 0:  blendMode = BlendMode.ID; break;
      case 1:  blendMode = BlendMode.LUMINOSITY; break;
      default: blendMode = BlendMode.ID; break;
    }

    imageDescriptor = new AssetDescriptor<>(Type.MIS.PATH + "\\" + missile.CelFile + ".dcc", DCC.class);
  }

  @Override
  public void update(float delta) {
    float radius = velocity * delta;
    remainingDist -= radius;
    if (remainingDist < 0) {
      Riiablo.engine.remove(this);
      return;
    }

    // FIXME: angle given using pixels but applied using map coordinated
    //tmpVec2.x = radius * MathUtils.cos(angle);
    //tmpVec2.y = radius * MathUtils.sin(angle);
    //position.add(tmpVec2);

    radius *= Tile.SUBTILE_WIDTH; // workaround to get approx speed
    EngineUtils.worldToScreenCoords(position, tmpVec2);
    tmpVec2.x += radius * MathUtils.cos(angle);
    tmpVec2.y += radius * MathUtils.sin(angle);

    float x = tmpVec2.x;
    float y = tmpVec2.y;
    tmpVec2.x = ( x / Tile.SUBTILE_WIDTH50 - y / Tile.SUBTILE_HEIGHT50) / 2;
    tmpVec2.y = (-x / Tile.SUBTILE_WIDTH50 - y / Tile.SUBTILE_HEIGHT50) / 2;

    position.set(tmpVec2);
  }

  @Override
  protected void updateCOF() {
    Riiablo.assets.load(imageDescriptor);
    Riiablo.assets.finishLoadingAsset(imageDescriptor);
    image = Riiablo.assets.get(imageDescriptor);

    animation = Animation.builder()
        .layer(image, blendMode)
        .build();
    animation.setMode(missile.LoopAnim > 0 ? Animation.Mode.LOOP : Animation.Mode.CLAMP); // TODO: Some are 2 -- special case?
    animation.setFrame(missile.RandStart);
    animation.setFrameDuration(Animation.FRAME_DURATION * missile.AnimSpeed);
    animation.setDirection(direction());
    dirty = Dirty.NONE;

    travelSound = Riiablo.audio.play(missile.TravelSound, true);
  }

  @Override
  public void drawShadow(PaletteIndexedBatch batch) {}
}
