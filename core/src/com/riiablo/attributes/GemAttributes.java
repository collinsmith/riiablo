package com.riiablo.attributes;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class GemAttributes extends AggregateAttributes {
  private static final Logger log = LogManager.getLogger(GemAttributes.class);

  int selected = -1;

  public int selectedList() {
    return selected;
  }

  public void select(int selected) {
    if (this.selected >= 0 && selected != this.selected) {
      log.warn("this.selected(" + this.selected + ") already set");
    }

    this.selected = selected;
  }

  public StatListGetter selected() {
    return list(selected);
  }
}
