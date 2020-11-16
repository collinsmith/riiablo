package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class AnimData extends Component {
  public int  speed;     // fixed point 24.8
  public int  frame;     // fixed point 24.8
  public int  numFrames; // fixed point 24.8
  public byte keyframes[];
  public int  override = -1;
}
