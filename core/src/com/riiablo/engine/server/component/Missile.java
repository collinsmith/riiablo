package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.codec.DCC;
import com.riiablo.codec.excel.Missiles;

@Transient
@PooledWeaver
public class Missile extends PooledComponent {
  public Missiles.Entry missile;
  public float range = 0;
  public AssetDescriptor<DCC> missileDescriptor;
  public final Vector2 start = new Vector2();

  @Override
  protected void reset() {
    missile = null;
    range = 0;
    missileDescriptor = null;
    start.setZero();
  }

  public Missile set(Missiles.Entry missile, Vector2 start, float range) {
    this.missile = missile;
    this.start.set(start);
    this.range = range;
    this.missileDescriptor = new AssetDescriptor<>(Class.Type.MIS.PATH + '\\' + missile.CelFile + ".dcc", DCC.class);
    return this;
  }
}
