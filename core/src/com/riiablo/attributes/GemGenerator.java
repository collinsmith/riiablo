package com.riiablo.attributes;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;

public class GemGenerator {
  protected PropertiesGenerator generator;

  public GemGenerator() {}

  GemGenerator(PropertiesGenerator generator) {
    this.generator = generator;
  }

  public Attributes set(Attributes attrs, String code) {
    return set(attrs, Riiablo.files.Gems.get(code));
  }

  public Attributes set(Attributes attrs, Gems.Entry gem) {
    assert attrs instanceof GemAttributes : "attrs(" + attrs + ") is not a " + GemAttributes.class;
    attrs.clear();
    final StatList stats = attrs.list();
    generator.add(stats.buildList(), gem.weaponModCode, gem.weaponModParam, gem.weaponModMin, gem.weaponModMax);
    generator.add(stats.buildList(), gem.helmModCode, gem.helmModParam, gem.helmModMin, gem.helmModMax);
    generator.add(stats.buildList(), gem.shieldModCode, gem.shieldModParam, gem.shieldModMin, gem.shieldModMax);
    return attrs;
  }
}
