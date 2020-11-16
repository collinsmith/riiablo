package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.map.DS1;

@Transient
@PooledWeaver
public class PathWrapper extends Component {
  public DS1.Path path;
}
