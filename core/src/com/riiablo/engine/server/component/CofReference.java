package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.riiablo.engine.Engine;

@PooledWeaver
public class CofReference extends Component {
  public String token;
  public byte   mode;
  public byte   wclass = Engine.WEAPON_HTH;

  public CofReference set(String token, byte mode) {
    this.token = token;
    this.mode = mode;
    return this;
  }
}
