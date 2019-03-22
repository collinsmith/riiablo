package com.riiablo.item;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.codec.util.BitStream;

public enum Stat {
  strength,
  energy,
  dexterity,
  vitality,
  statpts,
  newskills,
  hitpoints,
  maxhp,
  mana,
  maxmana,
  stamina,
  maxstamina,
  level,
  experience,
  gold,
  goldbank,
  item_armor_percent,
  item_maxdamage_percent,
  item_mindamage_percent,
  tohit,
  toblock,
  mindamage,
  maxdamage,
  secondary_mindamage,
  secondary_maxdamage,
  damagepercent,
  manarecovery,
  manarecoverybonus,
  staminarecoverybonus,
  lastexp,
  nextexp,
  armorclass,
  armorclass_vs_missile,
  armorclass_vs_hth,
  normal_damage_reduction,
  magic_damage_reduction,
  damageresist,
  magicresist,
  maxmagicresist,
  fireresist,
  maxfireresist,
  lightresist,
  maxlightresist,
  coldresist,
  maxcoldresist,
  poisonresist,
  maxpoisonresist,
  damageaura,
  firemindam,
  firemaxdam,
  lightmindam,
  lightmaxdam,
  magicmindam,
  magicmaxdam,
  coldmindam,
  coldmaxdam,
  coldlength,
  poisonmindam,
  poisonmaxdam,
  poisonlength,
  lifedrainmindam,
  lifedrainmaxdam,
  manadrainmindam,
  manadrainmaxdam,
  stamdrainmindam,
  stamdrainmaxdam,
  stunlength,
  velocitypercent,
  attackrate,
  other_animrate,
  quantity,
  value,
  durability,
  maxdurability,
  hpregen,
  item_maxdurability_percent,
  item_maxhp_percent,
  item_maxmana_percent,
  item_attackertakesdamage,
  item_goldbonus,
  item_magicbonus,
  item_knockback,
  item_timeduration,
  item_addclassskills,
  unsentparam1,
  item_addexperience,
  item_healafterkill,
  item_reducedprices,
  item_doubleherbduration,
  item_lightradius,
  item_lightcolor,
  item_req_percent,
  item_levelreq,
  item_fasterattackrate,
  item_levelreqpct,
  lastblockframe,
  item_fastermovevelocity,
  item_nonclassskill,
  state,
  item_fastergethitrate,
  monster_playercount,
  skill_poison_override_length,
  item_fasterblockrate,
  skill_bypass_undead,
  skill_bypass_demons,
  item_fastercastrate,
  skill_bypass_beasts,
  item_singleskill,
  item_restinpeace,
  curse_resistance,
  item_poisonlengthresist,
  item_normaldamage,
  item_howl,
  item_stupidity,
  item_damagetomana,
  item_ignoretargetac,
  item_fractionaltargetac,
  item_preventheal,
  item_halffreezeduration,
  item_tohit_percent,
  item_damagetargetac,
  item_demondamage_percent,
  item_undeaddamage_percent,
  item_demon_tohit,
  item_undead_tohit,
  item_throwable,
  item_elemskill,
  item_allskills,
  item_attackertakeslightdamage,
  ironmaiden_level,
  lifetap_level,
  thorns_percent,
  bonearmor,
  bonearmormax,
  item_freeze,
  item_openwounds,
  item_crushingblow,
  item_kickdamage,
  item_manaafterkill,
  item_healafterdemonkill,
  item_extrablood,
  item_deadlystrike,
  item_absorbfire_percent,
  item_absorbfire,
  item_absorblight_percent,
  item_absorblight,
  item_absorbmagic_percent,
  item_absorbmagic,
  item_absorbcold_percent,
  item_absorbcold,
  item_slow,
  item_aura,
  item_indesctructible,
  item_cannotbefrozen,
  item_staminadrainpct,
  item_reanimate,
  item_pierce,
  item_magicarrow,
  item_explosivearrow,
  item_throw_mindamage,
  item_throw_maxdamage,
  skill_handofathena,
  skill_staminapercent,
  skill_passive_staminapercent,
  skill_concentration,
  skill_enchant,
  skill_pierce,
  skill_conviction,
  skill_chillingarmor,
  skill_frenzy,
  skill_decrepify,
  skill_armor_percent,
  alignment,
  target0,
  target1,
  goldlost,
  conversion_level,
  conversion_maxhp,
  unit_dooverlay,
  attack_vs_montype,
  damage_vs_montype,
  fade,
  armor_override_percent,
  unused183,
  unused184,
  unused185,
  unused186,
  unused187,
  item_addskill_tab,
  unused189,
  unused190,
  unused191,
  unused192,
  unused193,
  item_numsockets,
  item_skillonattack,
  item_skillonkill,
  item_skillondeath,
  item_skillonhit,
  item_skillonlevelup,
  unused200,
  item_skillongethit,
  unused202,
  unused203,
  item_charged_skill,
  unused204,
  unused205,
  unused206,
  unused207,
  unused208,
  unused209,
  unused210,
  unused211,
  unused212,
  item_armor_perlevel,
  item_armorpercent_perlevel,
  item_hp_perlevel,
  item_mana_perlevel,
  item_maxdamage_perlevel,
  item_maxdamage_percent_perlevel,
  item_strength_perlevel,
  item_dexterity_perlevel,
  item_energy_perlevel,
  item_vitality_perlevel,
  item_tohit_perlevel,
  item_tohitpercent_perlevel,
  item_cold_damagemax_perlevel,
  item_fire_damagemax_perlevel,
  item_ltng_damagemax_perlevel,
  item_pois_damagemax_perlevel,
  item_resist_cold_perlevel,
  item_resist_fire_perlevel,
  item_resist_ltng_perlevel,
  item_resist_pois_perlevel,
  item_absorb_cold_perlevel,
  item_absorb_fire_perlevel,
  item_absorb_ltng_perlevel,
  item_absorb_pois_perlevel,
  item_thorns_perlevel,
  item_find_gold_perlevel,
  item_find_magic_perlevel,
  item_regenstamina_perlevel,
  item_stamina_perlevel,
  item_damage_demon_perlevel,
  item_damage_undead_perlevel,
  item_tohit_demon_perlevel,
  item_tohit_undead_perlevel,
  item_crushingblow_perlevel,
  item_openwounds_perlevel,
  item_kick_damage_perlevel,
  item_deadlystrike_perlevel,
  item_find_gems_perlevel,
  item_replenish_durability,
  item_replenish_quantity,
  item_extra_stack,
  item_find_item,
  item_slash_damage,
  item_slash_damage_percent,
  item_crush_damage,
  item_crush_damage_percent,
  item_thrust_damage,
  item_thrust_damage_percent,
  item_absorb_slash,
  item_absorb_crush,
  item_absorb_thrust,
  item_absorb_slash_percent,
  item_absorb_crush_percent,
  item_absorb_thrust_percent,
  item_armor_bytime,
  item_armorpercent_bytime,
  item_hp_bytime,
  item_mana_bytime,
  item_maxdamage_bytime,
  item_maxdamage_percent_bytime,
  item_strength_bytime,
  item_dexterity_bytime,
  item_energy_bytime,
  item_vitality_bytime,
  item_tohit_bytime,
  item_tohitpercent_bytime,
  item_cold_damagemax_bytime,
  item_fire_damagemax_bytime,
  item_ltng_damagemax_bytime,
  item_pois_damagemax_bytime,
  item_resist_cold_bytime,
  item_resist_fire_bytime,
  item_resist_ltng_bytime,
  item_resist_pois_bytime,
  item_absorb_cold_bytime,
  item_absorb_fire_bytime,
  item_absorb_ltng_bytime,
  item_absorb_pois_bytime,
  item_find_gold_bytime,
  item_find_magic_bytime,
  item_regenstamina_bytime,
  item_stamina_bytime,
  item_damage_demon_bytime,
  item_damage_undead_bytime,
  item_tohit_demon_bytime,
  item_tohit_undead_bytime,
  item_crushingblow_bytime,
  item_openwounds_bytime,
  item_kick_damage_bytime,
  item_deadlystrike_bytime,
  item_find_gems_bytime,
  item_pierce_cold,
  item_pierce_fire,
  item_pierce_ltng,
  item_pierce_pois,
  item_damage_vs_monster,
  item_damage_percent_vs_monster,
  item_tohit_vs_monster,
  item_tohit_percent_vs_monster,
  item_ac_vs_monster,
  item_ac_percent_vs_monster,
  firelength,
  burningmin,
  burningmax,
  progressive_damage,
  progressive_steal,
  progressive_other,
  progressive_fire,
  progressive_cold,
  progressive_lightning,
  item_extra_charges,
  progressive_tohit,
  poison_count,
  damage_framerate,
  pierce_idx,
  passive_fire_mastery,
  passive_ltng_mastery,
  passive_cold_mastery,
  passive_pois_mastery,
  passive_fire_pierce,
  passive_ltng_pierce,
  passive_cold_pierce,
  passive_pois_pierce,
  passive_critical_strike,
  passive_dodge,
  passive_avoid,
  passive_evade,
  passive_warmth,
  passive_mastery_melee_th,
  passive_mastery_melee_dmg,
  passive_mastery_melee_crit,
  passive_mastery_throw_th,
  passive_mastery_throw_dmg,
  passive_mastery_throw_crit,
  passive_weaponblock,
  passive_summon_resist,
  modifierlist_skill,
  modifierlist_level,
  last_sent_hp_pct,
  source_unit_type,
  source_unit_id,
  shortparam1,
  questitemdifficulty,
  passive_mag_mastery,
  passive_mag_pierce;

  private static final int NONE = 0x1FF;

  private static final int[] ENCODED_COUNT = {
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
      0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 1, 2, 1, 2, 1, 3, 1, 1, 3, 1, 1,
      1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
      2, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3,
      3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
      3, 3, 3, 3, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1
  };

  private final ItemStatCost.Entry entry;

  Stat() {
    entry = Riiablo.files.ItemStatCost.get(ordinal());
  }

  public static int getStatCount(int stat) {
    return ENCODED_COUNT[stat];
  }

  private static final Stat[] values = values();

  public static Stat valueOf(int i) {
    return i == NONE ? null : values[i];
  }

  public ItemStatCost.Entry entry() {
    return entry;
  }

  public Instance read(BitStream bitStream) {
    int param = bitStream.readUnsigned31OrLess(entry.Save_Param_Bits);
    int value = bitStream.readUnsigned31OrLess(entry.Save_Bits) - entry.Save_Add; // TODO: Support entry.ValShift
    System.out.println(this + " " + param + " " + value);
    return new Instance(this, value, param);
  }

  public static class Instance {
    final Stat stat;
    final int  value;
    final int  param;

    Instance(Stat stat, int value, int param) {
      this.stat  = stat;
      this.value = value;
      this.param = param;
    }

    public String format() {
      CharStats.Entry entry;
      Skills.Entry skill;
      SkillDesc.Entry desc;
      switch (stat.entry.descfunc) {
        case 1:  return String.format("+%d %s", value, descstr());
        case 2:  return String.format("%d%% %s", value, descstr());
        case 3:  return String.format("%d %s", value, descstr());
        case 4:  return String.format("+%d%% %s", value, descstr());
        case 5:  return String.format("%d%% %s", value * 100 / 128, descstr()); // TODO: item_howl -- verify
        case 6:  return String.format("+%d %s %s", value, descstr(), descstr2());
        case 7:  return String.format("%d%% %s %s", value, descstr(), descstr2());
        case 8:  return String.format("+%d%% %s %s", value, descstr(), descstr2());
        case 9:  return String.format("%d %s %s", value, descstr(), descstr2());
        case 11: return Riiablo.string.format("ModStre9u", 1, value / 100);
        case 12: return String.format("+%d %s", value, descstr());
        case 13: return String.format("+%d %s", value, Riiablo.string.lookup(CharacterClass.get(param).entry().StrAllSkills));
        case 14:
          entry = CharacterClass.get((param >>> 3) & 0x3).entry();
          return String.format("%s %s", Riiablo.string.format(entry.StrSkillTab[param & 0x7], value), Riiablo.string.lookup(entry.StrClassOnly));
        case 15:
          int e2p1 = (param >>> 6) & 0x3FF;
          int e2p2 = param & 0x3F;
          int e2p3 = value;
          skill = Riiablo.files.skills.get(e2p1);
          desc = Riiablo.files.skilldesc.get(skill.skilldesc);
          return Riiablo.string.format(stat.entry.descstrpos, e2p3, e2p2, Riiablo.string.lookup(desc.str_name));
        case 16:
          skill = Riiablo.files.skills.get(param);
          desc = Riiablo.files.skilldesc.get(skill.skilldesc);
          return Riiablo.string.format(stat.entry.descstrpos, value, Riiablo.string.lookup(desc.str_name));
        case 20: return String.format("%d%% %s", -value, descstr());
        case 22: return toString();
        case 23: return toString();
        case 24:
          int e3p1 = (param >>> 6) & 0x3FF;
          int e3p2 = param & 0x3F;
          int e3p3 = (value >>> 8) & 0xFF;
          int e3p4 = value & 0xFF;
          skill = Riiablo.files.skills.get(e3p1);
          desc = Riiablo.files.skilldesc.get(skill.skilldesc);
          return String.format("%s %d %s %s", Riiablo.string.lookup("ModStre10b"), e3p2, Riiablo.string.lookup(desc.str_name), Riiablo.string.format(stat.entry.descstrpos, e3p3, e3p4));
        case 27:
          skill = Riiablo.files.skills.get(param);
          desc = Riiablo.files.skilldesc.get(skill.skilldesc);
          entry = Riiablo.files.skills.getClass(skill.charclass).entry();
          return String.format("+%d %s %s %s", value, Riiablo.string.lookup("ItemStast1k"), Riiablo.string.lookup(desc.str_name), Riiablo.string.lookup(entry.StrClassOnly));
        case 28:
          skill = Riiablo.files.skills.get(param);
          desc = Riiablo.files.skilldesc.get(skill.skilldesc);
          return String.format("+%d %s %s", value, Riiablo.string.lookup("ItemStast1k"), Riiablo.string.lookup(desc.str_name));
        default: return toString();
      }
    }

    private String descstr() {
      return Riiablo.string.lookup(value < 0 ? stat.entry.descstrneg : stat.entry.descstrpos);
    }

    private String descstr2() {
      return Riiablo.string.lookup(stat.entry.descstr2);
    }

    @Override
    public String toString() {
      switch (stat.entry.Encode) {
        case 0: return stat + "=" + (stat.entry.Save_Param_Bits == 0 ? Integer.toString(value) : value + ":" + param);
        case 1: return stat + "=" + (stat.entry.Save_Param_Bits == 0 ? Integer.toString(value) : value + ":" + param); // no encoding?
        case 2:
          int e2p1 = (param >>> 6) & 0x3FF;
          int e2p2 = param & 0x3F;
          int e2p3 = value;
          return stat + "=" + e2p1 + ":" + e2p2 + ":" + e2p3;
        case 3:
          int e3p1 = (param >>> 6) & 0x3FF;
          int e3p2 = param & 0x3F;
          int e3p3 = (value >>> 8) & 0xFF;
          int e3p4 = value & 0xFF;
          return stat + "=" + e3p1 + ":" + e3p2 + ":" + e3p3 + ":" + e3p4;
        case 4: // item by-time -- not used by game, e.g., str based on time of day
          // fall-through
        default:
          return stat + "=" + (stat.entry.Save_Param_Bits == 0 ? Integer.toString(value) : value + ":" + param);
      }
    }
  }
}
