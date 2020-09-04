package com.riiablo.attributes;

import org.apache.commons.lang3.math.NumberUtils;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.ItemStatCost;

@SuppressWarnings("unused")
public class Stat {
  public static final short strength = 0;
  public static final short energy = 1;
  public static final short dexterity = 2;
  public static final short vitality = 3;
  public static final short statpts = 4;
  public static final short newskills = 5;
  public static final short hitpoints = 6;
  public static final short maxhp = 7;
  public static final short mana = 8;
  public static final short maxmana = 9;
  public static final short stamina = 10;
  public static final short maxstamina = 11;
  public static final short level = 12;
  public static final short experience = 13;
  public static final short gold = 14;
  public static final short goldbank = 15;
  public static final short item_armor_percent = 16;
  public static final short item_maxdamage_percent = 17;
  public static final short item_mindamage_percent = 18;
  public static final short tohit = 19;
  public static final short toblock = 20;
  public static final short mindamage = 21;
  public static final short maxdamage = 22;
  public static final short secondary_mindamage = 23;
  public static final short secondary_maxdamage = 24;
  public static final short damagepercent = 25;
  public static final short manarecovery = 26;
  public static final short manarecoverybonus = 27;
  public static final short staminarecoverybonus = 28;
  public static final short lastexp = 29;
  public static final short nextexp = 30;
  public static final short armorclass = 31;
  public static final short armorclass_vs_missile = 32;
  public static final short armorclass_vs_hth = 33;
  public static final short normal_damage_reduction = 34;
  public static final short magic_damage_reduction = 35;
  public static final short damageresist = 36;
  public static final short magicresist = 37;
  public static final short maxmagicresist = 38;
  public static final short fireresist = 39;
  public static final short maxfireresist = 40;
  public static final short lightresist = 41;
  public static final short maxlightresist = 42;
  public static final short coldresist = 43;
  public static final short maxcoldresist = 44;
  public static final short poisonresist = 45;
  public static final short maxpoisonresist = 46;
  public static final short damageaura = 47;
  public static final short firemindam = 48;
  public static final short firemaxdam = 49;
  public static final short lightmindam = 50;
  public static final short lightmaxdam = 51;
  public static final short magicmindam = 52;
  public static final short magicmaxdam = 53;
  public static final short coldmindam = 54;
  public static final short coldmaxdam = 55;
  public static final short coldlength = 56;
  public static final short poisonmindam = 57;
  public static final short poisonmaxdam = 58;
  public static final short poisonlength = 59;
  public static final short lifedrainmindam = 60;
  public static final short lifedrainmaxdam = 61;
  public static final short manadrainmindam = 62;
  public static final short manadrainmaxdam = 63;
  public static final short stamdrainmindam = 64;
  public static final short stamdrainmaxdam = 65;
  public static final short stunlength = 66;
  public static final short velocitypercent = 67;
  public static final short attackrate = 68;
  public static final short other_animrate = 69;
  public static final short quantity = 70;
  public static final short value = 71;
  public static final short durability = 72;
  public static final short maxdurability = 73;
  public static final short hpregen = 74;
  public static final short item_maxdurability_percent = 75;
  public static final short item_maxhp_percent = 76;
  public static final short item_maxmana_percent = 77;
  public static final short item_attackertakesdamage = 78;
  public static final short item_goldbonus = 79;
  public static final short item_magicbonus = 80;
  public static final short item_knockback = 81;
  public static final short item_timeduration = 82;
  public static final short item_addclassskills = 83;
  public static final short unsentparam1 = 84;
  public static final short item_addexperience = 85;
  public static final short item_healafterkill = 86;
  public static final short item_reducedprices = 87;
  public static final short item_doubleherbduration = 88;
  public static final short item_lightradius = 89;
  public static final short item_lightcolor = 90;
  public static final short item_req_percent = 91;
  public static final short item_levelreq = 92;
  public static final short item_fasterattackrate = 93;
  public static final short item_levelreqpct = 94;
  public static final short lastblockframe = 95;
  public static final short item_fastermovevelocity = 96;
  public static final short item_nonclassskill = 97;
  public static final short state = 98;
  public static final short item_fastergethitrate = 99;
  public static final short monster_playercount = 100;
  public static final short skill_poison_override_length = 101;
  public static final short item_fasterblockrate = 102;
  public static final short skill_bypass_undead = 103;
  public static final short skill_bypass_demons = 104;
  public static final short item_fastercastrate = 105;
  public static final short skill_bypass_beasts = 106;
  public static final short item_singleskill = 107;
  public static final short item_restinpeace = 108;
  public static final short curse_resistance = 109;
  public static final short item_poisonlengthresist = 110;
  public static final short item_normaldamage = 111;
  public static final short item_howl = 112;
  public static final short item_stupidity = 113;
  public static final short item_damagetomana = 114;
  public static final short item_ignoretargetac = 115;
  public static final short item_fractionaltargetac = 116;
  public static final short item_preventheal = 117;
  public static final short item_halffreezeduration = 118;
  public static final short item_tohit_percent = 119;
  public static final short item_damagetargetac = 120;
  public static final short item_demondamage_percent = 121;
  public static final short item_undeaddamage_percent = 122;
  public static final short item_demon_tohit = 123;
  public static final short item_undead_tohit = 124;
  public static final short item_throwable = 125;
  public static final short item_elemskill = 126;
  public static final short item_allskills = 127;
  public static final short item_attackertakeslightdamage = 128;
  public static final short ironmaiden_level = 129;
  public static final short lifetap_level = 130;
  public static final short thorns_percent = 131;
  public static final short bonearmor = 132;
  public static final short bonearmormax = 133;
  public static final short item_freeze = 134;
  public static final short item_openwounds = 135;
  public static final short item_crushingblow = 136;
  public static final short item_kickdamage = 137;
  public static final short item_manaafterkill = 138;
  public static final short item_healafterdemonkill = 139;
  public static final short item_extrablood = 140;
  public static final short item_deadlystrike = 141;
  public static final short item_absorbfire_percent = 142;
  public static final short item_absorbfire = 143;
  public static final short item_absorblight_percent = 144;
  public static final short item_absorblight = 145;
  public static final short item_absorbmagic_percent = 146;
  public static final short item_absorbmagic = 147;
  public static final short item_absorbcold_percent = 148;
  public static final short item_absorbcold = 149;
  public static final short item_slow = 150;
  public static final short item_aura = 151;
  public static final short item_indesctructible = 152;
  public static final short item_cannotbefrozen = 153;
  public static final short item_staminadrainpct = 154;
  public static final short item_reanimate = 155;
  public static final short item_pierce = 156;
  public static final short item_magicarrow = 157;
  public static final short item_explosivearrow = 158;
  public static final short item_throw_mindamage = 159;
  public static final short item_throw_maxdamage = 160;
  public static final short skill_handofathena = 161;
  public static final short skill_staminapercent = 162;
  public static final short skill_passive_staminapercent = 163;
  public static final short skill_concentration = 164;
  public static final short skill_enchant = 165;
  public static final short skill_pierce = 166;
  public static final short skill_conviction = 167;
  public static final short skill_chillingarmor = 168;
  public static final short skill_frenzy = 169;
  public static final short skill_decrepify = 170;
  public static final short skill_armor_percent = 171;
  public static final short alignment = 172;
  public static final short target0 = 173;
  public static final short target1 = 174;
  public static final short goldlost = 175;
  public static final short conversion_level = 176;
  public static final short conversion_maxhp = 177;
  public static final short unit_dooverlay = 178;
  public static final short attack_vs_montype = 179;
  public static final short damage_vs_montype = 180;
  public static final short fade = 181;
  public static final short armor_override_percent = 182;
  public static final short unused183 = 183;
  public static final short unused184 = 184;
  public static final short unused185 = 185;
  public static final short unused186 = 186;
  public static final short unused187 = 187;
  public static final short item_addskill_tab = 188;
  public static final short unused189 = 189;
  public static final short unused190 = 190;
  public static final short unused191 = 191;
  public static final short unused192 = 192;
  public static final short unused193 = 193;
  public static final short item_numsockets = 194;
  public static final short item_skillonattack = 195;
  public static final short item_skillonkill = 196;
  public static final short item_skillondeath = 197;
  public static final short item_skillonhit = 198;
  public static final short item_skillonlevelup = 199;
  public static final short unused200 = 200;
  public static final short item_skillongethit = 201;
  public static final short unused202 = 202;
  public static final short unused203 = 203;
  public static final short item_charged_skill = 204;
  public static final short unused204 = 205;
  public static final short unused205 = 206;
  public static final short unused206 = 207;
  public static final short unused207 = 208;
  public static final short unused208 = 209;
  public static final short unused209 = 210;
  public static final short unused210 = 211;
  public static final short unused211 = 212;
  public static final short unused212 = 213;
  public static final short item_armor_perlevel = 214;
  public static final short item_armorpercent_perlevel = 215;
  public static final short item_hp_perlevel = 216;
  public static final short item_mana_perlevel = 217;
  public static final short item_maxdamage_perlevel = 218;
  public static final short item_maxdamage_percent_perlevel = 219;
  public static final short item_strength_perlevel = 220;
  public static final short item_dexterity_perlevel = 221;
  public static final short item_energy_perlevel = 222;
  public static final short item_vitality_perlevel = 223;
  public static final short item_tohit_perlevel = 224;
  public static final short item_tohitpercent_perlevel = 225;
  public static final short item_cold_damagemax_perlevel = 226;
  public static final short item_fire_damagemax_perlevel = 227;
  public static final short item_ltng_damagemax_perlevel = 228;
  public static final short item_pois_damagemax_perlevel = 229;
  public static final short item_resist_cold_perlevel = 230;
  public static final short item_resist_fire_perlevel = 231;
  public static final short item_resist_ltng_perlevel = 232;
  public static final short item_resist_pois_perlevel = 233;
  public static final short item_absorb_cold_perlevel = 234;
  public static final short item_absorb_fire_perlevel = 235;
  public static final short item_absorb_ltng_perlevel = 236;
  public static final short item_absorb_pois_perlevel = 237;
  public static final short item_thorns_perlevel = 238;
  public static final short item_find_gold_perlevel = 239;
  public static final short item_find_magic_perlevel = 240;
  public static final short item_regenstamina_perlevel = 241;
  public static final short item_stamina_perlevel = 242;
  public static final short item_damage_demon_perlevel = 243;
  public static final short item_damage_undead_perlevel = 244;
  public static final short item_tohit_demon_perlevel = 245;
  public static final short item_tohit_undead_perlevel = 246;
  public static final short item_crushingblow_perlevel = 247;
  public static final short item_openwounds_perlevel = 248;
  public static final short item_kick_damage_perlevel = 249;
  public static final short item_deadlystrike_perlevel = 250;
  public static final short item_find_gems_perlevel = 251;
  public static final short item_replenish_durability = 252;
  public static final short item_replenish_quantity = 253;
  public static final short item_extra_stack = 254;
  public static final short item_find_item = 255;
  public static final short item_slash_damage = 256;
  public static final short item_slash_damage_percent = 257;
  public static final short item_crush_damage = 258;
  public static final short item_crush_damage_percent = 259;
  public static final short item_thrust_damage = 260;
  public static final short item_thrust_damage_percent = 261;
  public static final short item_absorb_slash = 262;
  public static final short item_absorb_crush = 263;
  public static final short item_absorb_thrust = 264;
  public static final short item_absorb_slash_percent = 265;
  public static final short item_absorb_crush_percent = 266;
  public static final short item_absorb_thrust_percent = 267;
  public static final short item_armor_bytime = 268;
  public static final short item_armorpercent_bytime = 269;
  public static final short item_hp_bytime = 270;
  public static final short item_mana_bytime = 271;
  public static final short item_maxdamage_bytime = 272;
  public static final short item_maxdamage_percent_bytime = 273;
  public static final short item_strength_bytime = 274;
  public static final short item_dexterity_bytime = 275;
  public static final short item_energy_bytime = 276;
  public static final short item_vitality_bytime = 277;
  public static final short item_tohit_bytime = 278;
  public static final short item_tohitpercent_bytime = 279;
  public static final short item_cold_damagemax_bytime = 280;
  public static final short item_fire_damagemax_bytime = 281;
  public static final short item_ltng_damagemax_bytime = 282;
  public static final short item_pois_damagemax_bytime = 283;
  public static final short item_resist_cold_bytime = 284;
  public static final short item_resist_fire_bytime = 285;
  public static final short item_resist_ltng_bytime = 286;
  public static final short item_resist_pois_bytime = 287;
  public static final short item_absorb_cold_bytime = 288;
  public static final short item_absorb_fire_bytime = 289;
  public static final short item_absorb_ltng_bytime = 290;
  public static final short item_absorb_pois_bytime = 291;
  public static final short item_find_gold_bytime = 292;
  public static final short item_find_magic_bytime = 293;
  public static final short item_regenstamina_bytime = 294;
  public static final short item_stamina_bytime = 295;
  public static final short item_damage_demon_bytime = 296;
  public static final short item_damage_undead_bytime = 297;
  public static final short item_tohit_demon_bytime = 298;
  public static final short item_tohit_undead_bytime = 299;
  public static final short item_crushingblow_bytime = 300;
  public static final short item_openwounds_bytime = 301;
  public static final short item_kick_damage_bytime = 302;
  public static final short item_deadlystrike_bytime = 303;
  public static final short item_find_gems_bytime = 304;
  public static final short item_pierce_cold = 305;
  public static final short item_pierce_fire = 306;
  public static final short item_pierce_ltng = 307;
  public static final short item_pierce_pois = 308;
  public static final short item_damage_vs_monster = 309;
  public static final short item_damage_percent_vs_monster = 310;
  public static final short item_tohit_vs_monster = 311;
  public static final short item_tohit_percent_vs_monster = 312;
  public static final short item_ac_vs_monster = 313;
  public static final short item_ac_percent_vs_monster = 314;
  public static final short firelength = 315;
  public static final short burningmin = 316;
  public static final short burningmax = 317;
  public static final short progressive_damage = 318;
  public static final short progressive_steal = 319;
  public static final short progressive_other = 320;
  public static final short progressive_fire = 321;
  public static final short progressive_cold = 322;
  public static final short progressive_lightning = 323;
  public static final short item_extra_charges = 324;
  public static final short progressive_tohit = 325;
  public static final short poison_count = 326;
  public static final short damage_framerate = 327;
  public static final short pierce_idx = 328;
  public static final short passive_fire_mastery = 329;
  public static final short passive_ltng_mastery = 330;
  public static final short passive_cold_mastery = 331;
  public static final short passive_pois_mastery = 332;
  public static final short passive_fire_pierce = 333;
  public static final short passive_ltng_pierce = 334;
  public static final short passive_cold_pierce = 335;
  public static final short passive_pois_pierce = 336;
  public static final short passive_critical_strike = 337;
  public static final short passive_dodge = 338;
  public static final short passive_avoid = 339;
  public static final short passive_evade = 340;
  public static final short passive_warmth = 341;
  public static final short passive_mastery_melee_th = 342;
  public static final short passive_mastery_melee_dmg = 343;
  public static final short passive_mastery_melee_crit = 344;
  public static final short passive_mastery_throw_th = 345;
  public static final short passive_mastery_throw_dmg = 346;
  public static final short passive_mastery_throw_crit = 347;
  public static final short passive_weaponblock = 348;
  public static final short passive_summon_resist = 349;
  public static final short modifierlist_skill = 350;
  public static final short modifierlist_level = 351;
  public static final short last_sent_hp_pct = 352;
  public static final short source_unit_type = 353;
  public static final short source_unit_id = 354;
  public static final short shortparam1 = 355;
  public static final short questitemdifficulty = 356;
  public static final short passive_mag_mastery = 357;
  public static final short passive_mag_pierce = 358;

  static final int BITS = 9;
  static final short NONE = (1 << BITS) - 1; // 0x1FF

  private static final byte[] GROUPED_COUNT = { 1, 4, 4, };

  static final int NUM_GROUPS = GROUPED_COUNT.length;
  static final byte MAX_GROUPED = NumberUtils.max(GROUPED_COUNT);

  static byte getNumGrouped(int dgrp) {
    return GROUPED_COUNT[dgrp];
  }

  private static final byte[] ENCODED_COUNT = {
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 0
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 2, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, // 32
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 64
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 96
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 128
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 160
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 192
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 224
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 256
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 288
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 320
      1, 1, 1, 1, 1, 1, 1,                                                                            // 352
  };

  static final byte MAX_ENCODED = NumberUtils.max(ENCODED_COUNT);

  static byte getNumEncoded(short stat) {
    return ENCODED_COUNT[stat];
  }

  static ItemStatCost.Entry entry(short stat) {
    return Riiablo.files.ItemStatCost.get(stat);
  }

  static short index(String stat) {
    return (short) Riiablo.files.ItemStatCost.index(stat);
  }

  static boolean hasParams(short stat) {
    final ItemStatCost.Entry entry = entry(stat);
    return entry.Encode >= 1 && entry.Encode <= 3; // TODO: determine if stat requires params
  }

  static int encodeValue(int encoding, int value1, int value2, int value3) {
    switch (encoding) {
      default: // fall-through
      case 0: // fall-through
      case 1: // fall-through
      case 2:
        assert value2 == 0 : "value2(" + value2 + ") != " + 0;
        assert value3 == 0 : "value3(" + value3 + ") != " + 0;
        return value1;
      case 3:
        value1 = Math.min(value1, (1 << 8) - 1);
        value2 = Math.min(value2, (1 << 8) - 1);
        return (value2 << 8) | value1;
      case 4:
        // TODO: see issue #24
        value2 = Math.min(value2, (1 << 10) - 1);
        value3 = Math.min(value3, (1 << 10) - 1);
        return (value3 << 12) | (value2 << 2) | (value1 & 0x3);
    }
  }

  static int encodeParam(int encoding, int param1, int param2) {
    switch (encoding) {
      default:
      case 0: // fall-through
      case 1: // fall-through
      case 4:
        assert param2 == 0 : "param2(" + param2 + ") != " + 0;
        return param1;
      case 2: // fall-through
      case 3:
        param1 = Math.min(param1, (1 <<  6) - 1);
        param2 = Math.min(param2, (1 << 10) - 1);
        return (param2 << 6) | param1;
    }
  }

  private Stat() {}
}
