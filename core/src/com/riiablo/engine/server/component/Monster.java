package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;

@Transient
@PooledWeaver
public class Monster extends Component {
  public MonStats.Entry  monstats;
  public MonStats2.Entry monstats2;

  public Monster set(MonStats.Entry monstats, MonStats2.Entry monstats2) {
    this.monstats = monstats;
    this.monstats2 = monstats2;
    return this;
  }
}
