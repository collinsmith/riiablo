package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.widget.NpcMenu;

@Transient
@PooledWeaver
public class MenuWrapper extends Component {
  public NpcMenu menu;
}
