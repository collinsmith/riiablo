package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.physics.box2d.Body;

@Transient
@PooledWeaver
public class Box2DBody extends Component {
  public Body body;
}
