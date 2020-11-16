package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Flags extends Component {
  /**
   * @see com.riiablo.net.packet.d2gs.EntityFlags
   */
  public int flags = 0;
}
