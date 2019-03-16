package com.riiablo;

import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.graphics.BlendMode;

public enum CharacterClass {
  AMAZON     ("am", 0) {{
    name = 4011;
    description = 5128;
    spellsBackground = "skltree_a_back";
    spellIcons = "AmSkillicon";
    spellTree = new String[][]{
        {"StrSklTree10", "StrSklTree11", "StrSklTree4"},
        {"StrSklTree8", "StrSklTree9", "StrSklTree4"},
        {"StrSklTree6", "StrSklTree7", "StrSklTree4"},
    };
    firstSpell = 6;
    lastSpell  = 36;
  }},
  SORCERESS  ("so", 1) {{
    name = 4010;
    description = 5131;
    spellsBackground = "skltree_s_back";
    spellIcons = "SoSkillicon";
    spellTree = new String[][] {
        {"StrSklTree25", "StrSklTree5"},
        {"StrSklTree24", "StrSklTree5"},
        {"StrSklTree23", "StrSklTree5"},
    };
    firstSpell = 36;
    lastSpell  = 66;
    fws = bws = nu3s = true;
    blendSpecial = BlendMode.LUMINOSITY;
  }},
  NECROMANCER("ne", 2) {{
    name = 4009;
    description = 5129;
    spellsBackground = "skltree_n_back";
    spellIcons = "NeSkillicon";
    spellTree = new String[][]{
        {"StrSklTree10", "StrSklTree5"},
        {"StrSklTree17", "StrSklTree18", "StrSklTree5"},
        {"StrSklTree19"},
    };
    firstSpell = 66;
    lastSpell  = 96;
    fws = bws = nu3s = true;
    blendSpecial = BlendMode.LUMINOSITY;
  }},
  PALADIN    ("pa", 3) {{
    name = 4008;
    description = 5132;
    spellsBackground = "skltree_p_back";
    spellIcons = "PaSkillicon";
    spellTree = new String[][]{
        {"StrSklTree15", "StrSklTree4"},
        {"StrSklTree14", "StrSklTree13"},
        {"StrSklTree12", "StrSklTree13"},
    };
    firstSpell = 96;
    lastSpell  = 126;
    fws = true;
    blendSpecial = BlendMode.ID;
  }},
  BARBARIAN  ("ba", 4) {{
    name = 4007;
    description = 5130;
    spellsBackground = "skltree_b_back";
    spellIcons = "BaSkillicon";
    spellTree = new String[][]{
        {"StrSklTree21", "StrSklTree4"},
        {"StrSklTree21", "StrSklTree22"},
        {"StrSklTree20"},
    };
    firstSpell = 126;
    lastSpell  = 156;
    fws = true;
    blendSpecial = BlendMode.ID;
  }},
  DRUID      ("dz", 5) {{
    name = 4012;
    description = 22518;
    spellsBackground = "skltree_d_back";
    spellIcons = "DrSkillicon";
    spellTree = new String[][]{
        {"StrSklTree26", "StrSklTree4"},
        {"StrSklTree27", "StrSklTree28", "StrSklTree4"},
        {"StrSklTree29", "StrSklTree4"},
    };
    firstSpell = 221;
    lastSpell  = 251;
  }},
  ASSASSIN   ("as", 6) {{
    name = 4013;
    description = 22519;
    spellsBackground = "skltree_i_back";
    spellIcons = "AsSkillicon";
    spellTree = new String[][]{
        {"StrSklTree30"},
        {"StrSklTree31", "StrSklTree32"},
        {"StrSklTree33", "StrSklTree34"},
    };
    firstSpell = 251;
    lastSpell  = 281;
  }},
  ;

  public final String shortName;
  public final int id;
  public int name, description;
  public boolean nu3s = false, fws = false, bws = false;
  public int blendSpecial = BlendMode.NONE;
  public String spellsBackground;
  public String spellIcons;
  public String spellTree[][];
  public int firstSpell, lastSpell;

  CharacterClass(String shortName, int id) {
    this.shortName = shortName;
    this.id = id;
  }

  public static CharacterClass get(int classId) {
    switch (classId) {
      case 0:  return AMAZON;
      case 1:  return SORCERESS;
      case 2:  return NECROMANCER;
      case 3:  return PALADIN;
      case 4:  return BARBARIAN;
      case 5:  return DRUID;
      case 6:  return ASSASSIN;
      default: throw new GdxRuntimeException("Invalid class id: " + classId);
    }
  }
}
