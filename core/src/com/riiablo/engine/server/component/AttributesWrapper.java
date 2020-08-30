package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

import com.riiablo.item.Attributes;

@Transient
@PooledWeaver
public class AttributesWrapper extends Component {
  public Attributes attrs; // TODO: make final and call attrs.base().reset() and attrs.reset()
}
