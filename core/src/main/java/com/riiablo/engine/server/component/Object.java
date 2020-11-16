package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.codec.excel.Objects;

@Transient
@PooledWeaver
public class Object extends Component {
  public Objects.Entry base;
}
