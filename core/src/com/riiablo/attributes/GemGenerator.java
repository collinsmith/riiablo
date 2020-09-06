package com.riiablo.attributes;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;
import com.riiablo.logger.MDC;

import static com.riiablo.attributes.StatListFlags.GEM_ARMOR_LIST;
import static com.riiablo.attributes.StatListFlags.GEM_SHIELD_LIST;
import static com.riiablo.attributes.StatListFlags.GEM_WEAPON_LIST;

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
    assert attrs.isType(Attributes.GEM) : "attrs(" + attrs + ") is not a GEM(" + attrs.type() + ")";
    attrs.clear();
    final StatList stats = attrs.list();
    try {
      int list;
      MDC.put("propList", StatListFlags.gemToString(GEM_WEAPON_LIST));
      list = generator
          .add(stats.buildList(), gem.weaponModCode, gem.weaponModParam, gem.weaponModMin, gem.weaponModMax)
          .listIndex();
      assert list == GEM_WEAPON_LIST : "list(" + list + ") != GEM_WEAPON_LIST(" + GEM_WEAPON_LIST + ")";
      MDC.put("propList", StatListFlags.gemToString(GEM_ARMOR_LIST));
      list = generator
          .add(stats.buildList(), gem.helmModCode, gem.helmModParam, gem.helmModMin, gem.helmModMax)
          .listIndex();
      assert list == GEM_ARMOR_LIST : "list(" + list + ") != GEM_WEAPON_LIST(" + GEM_WEAPON_LIST + ")";
      MDC.put("propList", StatListFlags.gemToString(GEM_SHIELD_LIST));
      list = generator
          .add(stats.buildList(), gem.shieldModCode, gem.shieldModParam, gem.shieldModMin, gem.shieldModMax)
          .listIndex();
      assert list == GEM_SHIELD_LIST : "list(" + list + ") != GEM_WEAPON_LIST(" + GEM_WEAPON_LIST + ")";
    } finally {
      MDC.remove("propList");
    }
    stats.freeze();
    return attrs;
  }
}
