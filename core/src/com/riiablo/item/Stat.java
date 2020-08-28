package com.riiablo.item;

import com.google.common.primitives.UnsignedInts;
import java.util.Arrays;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.io.BitInput;
import com.riiablo.io.BitOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.save.CharData;

@SuppressWarnings("unused")
public class Stat implements Comparable<Stat> {
  private static final Logger log = LogManager.getLogger(Stat.class);

  public static final int strength                        = 0;
  public static final int energy                          = 1;
  public static final int dexterity                       = 2;
  public static final int vitality                        = 3;
  public static final int statpts                         = 4;
  public static final int newskills                       = 5;
  public static final int hitpoints                       = 6;
  public static final int maxhp                           = 7;
  public static final int mana                            = 8;
  public static final int maxmana                         = 9;
  public static final int stamina                         = 10;
  public static final int maxstamina                      = 11;
  public static final int level                           = 12;
  public static final int experience                      = 13;
  public static final int gold                            = 14;
  public static final int goldbank                        = 15;
  public static final int item_armor_percent              = 16;
  public static final int item_maxdamage_percent          = 17;
  public static final int item_mindamage_percent          = 18;
  public static final int tohit                           = 19;
  public static final int toblock                         = 20;
  public static final int mindamage                       = 21;
  public static final int maxdamage                       = 22;
  public static final int secondary_mindamage             = 23;
  public static final int secondary_maxdamage             = 24;
  public static final int damagepercent                   = 25;
  public static final int manarecovery                    = 26;
  public static final int manarecoverybonus               = 27;
  public static final int staminarecoverybonus            = 28;
  public static final int lastexp                         = 29;
  public static final int nextexp                         = 30;
  public static final int armorclass                      = 31;
  public static final int armorclass_vs_missile           = 32;
  public static final int armorclass_vs_hth               = 33;
  public static final int normal_damage_reduction         = 34;
  public static final int magic_damage_reduction          = 35;
  public static final int damageresist                    = 36;
  public static final int magicresist                     = 37;
  public static final int maxmagicresist                  = 38;
  public static final int fireresist                      = 39;
  public static final int maxfireresist                   = 40;
  public static final int lightresist                     = 41;
  public static final int maxlightresist                  = 42;
  public static final int coldresist                      = 43;
  public static final int maxcoldresist                   = 44;
  public static final int poisonresist                    = 45;
  public static final int maxpoisonresist                 = 46;
  public static final int damageaura                      = 47;
  public static final int firemindam                      = 48;
  public static final int firemaxdam                      = 49;
  public static final int lightmindam                     = 50;
  public static final int lightmaxdam                     = 51;
  public static final int magicmindam                     = 52;
  public static final int magicmaxdam                     = 53;
  public static final int coldmindam                      = 54;
  public static final int coldmaxdam                      = 55;
  public static final int coldlength                      = 56;
  public static final int poisonmindam                    = 57;
  public static final int poisonmaxdam                    = 58;
  public static final int poisonlength                    = 59;
  public static final int lifedrainmindam                 = 60;
  public static final int lifedrainmaxdam                 = 61;
  public static final int manadrainmindam                 = 62;
  public static final int manadrainmaxdam                 = 63;
  public static final int stamdrainmindam                 = 64;
  public static final int stamdrainmaxdam                 = 65;
  public static final int stunlength                      = 66;
  public static final int velocitypercent                 = 67;
  public static final int attackrate                      = 68;
  public static final int other_animrate                  = 69;
  public static final int quantity                        = 70;
  public static final int value                           = 71;
  public static final int durability                      = 72;
  public static final int maxdurability                   = 73;
  public static final int hpregen                         = 74;
  public static final int item_maxdurability_percent      = 75;
  public static final int item_maxhp_percent              = 76;
  public static final int item_maxmana_percent            = 77;
  public static final int item_attackertakesdamage        = 78;
  public static final int item_goldbonus                  = 79;
  public static final int item_magicbonus                 = 80;
  public static final int item_knockback                  = 81;
  public static final int item_timeduration               = 82;
  public static final int item_addclassskills             = 83;
  public static final int unsentparam1                    = 84;
  public static final int item_addexperience              = 85;
  public static final int item_healafterkill              = 86;
  public static final int item_reducedprices              = 87;
  public static final int item_doubleherbduration         = 88;
  public static final int item_lightradius                = 89;
  public static final int item_lightcolor                 = 90;
  public static final int item_req_percent                = 91;
  public static final int item_levelreq                   = 92;
  public static final int item_fasterattackrate           = 93;
  public static final int item_levelreqpct                = 94;
  public static final int lastblockframe                  = 95;
  public static final int item_fastermovevelocity         = 96;
  public static final int item_nonclassskill              = 97;
  public static final int state                           = 98;
  public static final int item_fastergethitrate           = 99;
  public static final int monster_playercount             = 100;
  public static final int skill_poison_override_length    = 101;
  public static final int item_fasterblockrate            = 102;
  public static final int skill_bypass_undead             = 103;
  public static final int skill_bypass_demons             = 104;
  public static final int item_fastercastrate             = 105;
  public static final int skill_bypass_beasts             = 106;
  public static final int item_singleskill                = 107;
  public static final int item_restinpeace                = 108;
  public static final int curse_resistance                = 109;
  public static final int item_poisonlengthresist         = 110;
  public static final int item_normaldamage               = 111;
  public static final int item_howl                       = 112;
  public static final int item_stupidity                  = 113;
  public static final int item_damagetomana               = 114;
  public static final int item_ignoretargetac             = 115;
  public static final int item_fractionaltargetac         = 116;
  public static final int item_preventheal                = 117;
  public static final int item_halffreezeduration         = 118;
  public static final int item_tohit_percent              = 119;
  public static final int item_damagetargetac             = 120;
  public static final int item_demondamage_percent        = 121;
  public static final int item_undeaddamage_percent       = 122;
  public static final int item_demon_tohit                = 123;
  public static final int item_undead_tohit               = 124;
  public static final int item_throwable                  = 125;
  public static final int item_elemskill                  = 126;
  public static final int item_allskills                  = 127;
  public static final int item_attackertakeslightdamage   = 128;
  public static final int ironmaiden_level                = 129;
  public static final int lifetap_level                   = 130;
  public static final int thorns_percent                  = 131;
  public static final int bonearmor                       = 132;
  public static final int bonearmormax                    = 133;
  public static final int item_freeze                     = 134;
  public static final int item_openwounds                 = 135;
  public static final int item_crushingblow               = 136;
  public static final int item_kickdamage                 = 137;
  public static final int item_manaafterkill              = 138;
  public static final int item_healafterdemonkill         = 139;
  public static final int item_extrablood                 = 140;
  public static final int item_deadlystrike               = 141;
  public static final int item_absorbfire_percent         = 142;
  public static final int item_absorbfire                 = 143;
  public static final int item_absorblight_percent        = 144;
  public static final int item_absorblight                = 145;
  public static final int item_absorbmagic_percent        = 146;
  public static final int item_absorbmagic                = 147;
  public static final int item_absorbcold_percent         = 148;
  public static final int item_absorbcold                 = 149;
  public static final int item_slow                       = 150;
  public static final int item_aura                       = 151;
  public static final int item_indesctructible            = 152;
  public static final int item_cannotbefrozen             = 153;
  public static final int item_staminadrainpct            = 154;
  public static final int item_reanimate                  = 155;
  public static final int item_pierce                     = 156;
  public static final int item_magicarrow                 = 157;
  public static final int item_explosivearrow             = 158;
  public static final int item_throw_mindamage            = 159;
  public static final int item_throw_maxdamage            = 160;
  public static final int skill_handofathena              = 161;
  public static final int skill_staminapercent            = 162;
  public static final int skill_passive_staminapercent    = 163;
  public static final int skill_concentration             = 164;
  public static final int skill_enchant                   = 165;
  public static final int skill_pierce                    = 166;
  public static final int skill_conviction                = 167;
  public static final int skill_chillingarmor             = 168;
  public static final int skill_frenzy                    = 169;
  public static final int skill_decrepify                 = 170;
  public static final int skill_armor_percent             = 171;
  public static final int alignment                       = 172;
  public static final int target0                         = 173;
  public static final int target1                         = 174;
  public static final int goldlost                        = 175;
  public static final int conversion_level                = 176;
  public static final int conversion_maxhp                = 177;
  public static final int unit_dooverlay                  = 178;
  public static final int attack_vs_montype               = 179;
  public static final int damage_vs_montype               = 180;
  public static final int fade                            = 181;
  public static final int armor_override_percent          = 182;
  public static final int unused183                       = 183;
  public static final int unused184                       = 184;
  public static final int unused185                       = 185;
  public static final int unused186                       = 186;
  public static final int unused187                       = 187;
  public static final int item_addskill_tab               = 188;
  public static final int unused189                       = 189;
  public static final int unused190                       = 190;
  public static final int unused191                       = 191;
  public static final int unused192                       = 192;
  public static final int unused193                       = 193;
  public static final int item_numsockets                 = 194;
  public static final int item_skillonattack              = 195;
  public static final int item_skillonkill                = 196;
  public static final int item_skillondeath               = 197;
  public static final int item_skillonhit                 = 198;
  public static final int item_skillonlevelup             = 199;
  public static final int unused200                       = 200;
  public static final int item_skillongethit              = 201;
  public static final int unused202                       = 202;
  public static final int unused203                       = 203;
  public static final int item_charged_skill              = 204;
  public static final int unused204                       = 205;
  public static final int unused205                       = 206;
  public static final int unused206                       = 207;
  public static final int unused207                       = 208;
  public static final int unused208                       = 209;
  public static final int unused209                       = 210;
  public static final int unused210                       = 211;
  public static final int unused211                       = 212;
  public static final int unused212                       = 213;
  public static final int item_armor_perlevel             = 214;
  public static final int item_armorpercent_perlevel      = 215;
  public static final int item_hp_perlevel                = 216;
  public static final int item_mana_perlevel              = 217;
  public static final int item_maxdamage_perlevel         = 218;
  public static final int item_maxdamage_percent_perlevel = 219;
  public static final int item_strength_perlevel          = 220;
  public static final int item_dexterity_perlevel         = 221;
  public static final int item_energy_perlevel            = 222;
  public static final int item_vitality_perlevel          = 223;
  public static final int item_tohit_perlevel             = 224;
  public static final int item_tohitpercent_perlevel      = 225;
  public static final int item_cold_damagemax_perlevel    = 226;
  public static final int item_fire_damagemax_perlevel    = 227;
  public static final int item_ltng_damagemax_perlevel    = 228;
  public static final int item_pois_damagemax_perlevel    = 229;
  public static final int item_resist_cold_perlevel       = 230;
  public static final int item_resist_fire_perlevel       = 231;
  public static final int item_resist_ltng_perlevel       = 232;
  public static final int item_resist_pois_perlevel       = 233;
  public static final int item_absorb_cold_perlevel       = 234;
  public static final int item_absorb_fire_perlevel       = 235;
  public static final int item_absorb_ltng_perlevel       = 236;
  public static final int item_absorb_pois_perlevel       = 237;
  public static final int item_thorns_perlevel            = 238;
  public static final int item_find_gold_perlevel         = 239;
  public static final int item_find_magic_perlevel        = 240;
  public static final int item_regenstamina_perlevel      = 241;
  public static final int item_stamina_perlevel           = 242;
  public static final int item_damage_demon_perlevel      = 243;
  public static final int item_damage_undead_perlevel     = 244;
  public static final int item_tohit_demon_perlevel       = 245;
  public static final int item_tohit_undead_perlevel      = 246;
  public static final int item_crushingblow_perlevel      = 247;
  public static final int item_openwounds_perlevel        = 248;
  public static final int item_kick_damage_perlevel       = 249;
  public static final int item_deadlystrike_perlevel      = 250;
  public static final int item_find_gems_perlevel         = 251;
  public static final int item_replenish_durability       = 252;
  public static final int item_replenish_quantity         = 253;
  public static final int item_extra_stack                = 254;
  public static final int item_find_item                  = 255;
  public static final int item_slash_damage               = 256;
  public static final int item_slash_damage_percent       = 257;
  public static final int item_crush_damage               = 258;
  public static final int item_crush_damage_percent       = 259;
  public static final int item_thrust_damage              = 260;
  public static final int item_thrust_damage_percent      = 261;
  public static final int item_absorb_slash               = 262;
  public static final int item_absorb_crush               = 263;
  public static final int item_absorb_thrust              = 264;
  public static final int item_absorb_slash_percent       = 265;
  public static final int item_absorb_crush_percent       = 266;
  public static final int item_absorb_thrust_percent      = 267;
  public static final int item_armor_bytime               = 268;
  public static final int item_armorpercent_bytime        = 269;
  public static final int item_hp_bytime                  = 270;
  public static final int item_mana_bytime                = 271;
  public static final int item_maxdamage_bytime           = 272;
  public static final int item_maxdamage_percent_bytime   = 273;
  public static final int item_strength_bytime            = 274;
  public static final int item_dexterity_bytime           = 275;
  public static final int item_energy_bytime              = 276;
  public static final int item_vitality_bytime            = 277;
  public static final int item_tohit_bytime               = 278;
  public static final int item_tohitpercent_bytime        = 279;
  public static final int item_cold_damagemax_bytime      = 280;
  public static final int item_fire_damagemax_bytime      = 281;
  public static final int item_ltng_damagemax_bytime      = 282;
  public static final int item_pois_damagemax_bytime      = 283;
  public static final int item_resist_cold_bytime         = 284;
  public static final int item_resist_fire_bytime         = 285;
  public static final int item_resist_ltng_bytime         = 286;
  public static final int item_resist_pois_bytime         = 287;
  public static final int item_absorb_cold_bytime         = 288;
  public static final int item_absorb_fire_bytime         = 289;
  public static final int item_absorb_ltng_bytime         = 290;
  public static final int item_absorb_pois_bytime         = 291;
  public static final int item_find_gold_bytime           = 292;
  public static final int item_find_magic_bytime          = 293;
  public static final int item_regenstamina_bytime        = 294;
  public static final int item_stamina_bytime             = 295;
  public static final int item_damage_demon_bytime        = 296;
  public static final int item_damage_undead_bytime       = 297;
  public static final int item_tohit_demon_bytime         = 298;
  public static final int item_tohit_undead_bytime        = 299;
  public static final int item_crushingblow_bytime        = 300;
  public static final int item_openwounds_bytime          = 301;
  public static final int item_kick_damage_bytime         = 302;
  public static final int item_deadlystrike_bytime        = 303;
  public static final int item_find_gems_bytime           = 304;
  public static final int item_pierce_cold                = 305;
  public static final int item_pierce_fire                = 306;
  public static final int item_pierce_ltng                = 307;
  public static final int item_pierce_pois                = 308;
  public static final int item_damage_vs_monster          = 309;
  public static final int item_damage_percent_vs_monster  = 310;
  public static final int item_tohit_vs_monster           = 311;
  public static final int item_tohit_percent_vs_monster   = 312;
  public static final int item_ac_vs_monster              = 313;
  public static final int item_ac_percent_vs_monster      = 314;
  public static final int firelength                      = 315;
  public static final int burningmin                      = 316;
  public static final int burningmax                      = 317;
  public static final int progressive_damage              = 318;
  public static final int progressive_steal               = 319;
  public static final int progressive_other               = 320;
  public static final int progressive_fire                = 321;
  public static final int progressive_cold                = 322;
  public static final int progressive_lightning           = 323;
  public static final int item_extra_charges              = 324;
  public static final int progressive_tohit               = 325;
  public static final int poison_count                    = 326;
  public static final int damage_framerate                = 327;
  public static final int pierce_idx                      = 328;
  public static final int passive_fire_mastery            = 329;
  public static final int passive_ltng_mastery            = 330;
  public static final int passive_cold_mastery            = 331;
  public static final int passive_pois_mastery            = 332;
  public static final int passive_fire_pierce             = 333;
  public static final int passive_ltng_pierce             = 334;
  public static final int passive_cold_pierce             = 335;
  public static final int passive_pois_pierce             = 336;
  public static final int passive_critical_strike         = 337;
  public static final int passive_dodge                   = 338;
  public static final int passive_avoid                   = 339;
  public static final int passive_evade                   = 340;
  public static final int passive_warmth                  = 341;
  public static final int passive_mastery_melee_th        = 342;
  public static final int passive_mastery_melee_dmg       = 343;
  public static final int passive_mastery_melee_crit      = 344;
  public static final int passive_mastery_throw_th        = 345;
  public static final int passive_mastery_throw_dmg       = 346;
  public static final int passive_mastery_throw_crit      = 347;
  public static final int passive_weaponblock             = 348;
  public static final int passive_summon_resist           = 349;
  public static final int modifierlist_skill              = 350;
  public static final int modifierlist_level              = 351;
  public static final int last_sent_hp_pct                = 352;
  public static final int source_unit_type                = 353;
  public static final int source_unit_id                  = 354;
  public static final int shortparam1                     = 355;
  public static final int questitemdifficulty             = 356;
  public static final int passive_mag_mastery             = 357;
  public static final int passive_mag_pierce              = 358;

  public static final int BITS                            = 9;
  public static final int NONE                            = (1 << BITS) - 1; // 0x1FF

  // These don't actually exist in the game
  public static final int reqstr                          = NONE - 1;
  public static final int reqdex                          = NONE - 2;
  public static final int all_attributes                  = NONE - 3;
  public static final int all_resistances                 = NONE - 4;
  public static final int mindam                          = NONE - 5;
  public static final int enhanceddam                     = NONE - 6;
  public static final int firedam                         = NONE - 7;
  public static final int lightdam                        = NONE - 8;
  public static final int magicdam                        = NONE - 9;
  public static final int colddam                         = NONE - 10;
  public static final int poisondam                       = NONE - 11;

  static final int[] ENCODED_COUNT = {
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

  static int getNumEncoded(int stat) {
    return ENCODED_COUNT[stat];
  }

  static int hash(int stat, int param) {
    return stat | (param << 16);
  }

  static int encodeValue(int encoding, int... values) {
    int value1, value2, value3;
    switch (encoding) {
      case 3:
        value1 = Math.min(values[0], (1 << 8) - 1);
        value2 = Math.min(values[1], (1 << 8) - 1);
        return (value2 << 8) | value1;
      case 4:
        // TODO: see issue #24
        value2 = Math.min(values[1], (1 << 10) - 1);
        value3 = Math.min(values[2], (1 << 10) - 1);
        return (value3 << 12) | (value2 << 2) | (values[0] & 0x3);
      case 0:
      case 1:
      case 2:
      default:
        assert values.length == 1;
        return values[0];
    }
  }

  static int encodeParam(int encoding, int... params) {
    int param1, param2;
    switch (encoding) {
      case 2:
      case 3:
        param1 = Math.min(params[0], (1 <<  6) - 1);
        param2 = Math.min(params[1], (1 << 10) - 1);
        return (param2 << 6) | param1;
      case 0:
      case 1:
      case 4:
      default:
        assert params.length == 1;
        return params[0];
    }
  }

  static Stat obtain() {
    return new Stat(); // POOL.obtain();
  }

  static Stat obtain(int stat, BitInput bitStream) {
    return obtain()._obtain(stat, bitStream);
  }

  static Stat obtain(int stat, int value) {
    return obtain(stat, 0, value);
  }

  static Stat obtain(int stat, int param, int value) {
    return obtain()._obtain(stat, param, value);
  }

  static Stat obtain(Stat src) {
    return obtain()._obtain(src);
  }

  public int id;
  public int param;
  public ItemStatCost.Entry entry;
  public boolean modified;
  int hash;
  int val;

  Stat() {}

  Stat _obtain(int stat, BitInput bitStream) {
    this.id  = stat;
    entry    = Riiablo.files.ItemStatCost.get(stat);
    param    = bitStream.read31u(entry.Save_Param_Bits);
    val      = bitStream.read31u(entry.Save_Bits) - entry.Save_Add;
    hash     = hash(stat, param);
    modified = false;
    log.trace(toString());
    return this;
  }

  Stat _obtain(int stat, int param, int value) {
    this.id    = stat;
    this.param = param;
    this.val   = value;
    entry      = Riiablo.files.ItemStatCost.get(stat);
    hash       = Stat.hash(stat, param);
    modified   = false;
    log.trace(toString());
    return this;
  }

  Stat _obtain(Stat src) {
    id       = src.id;
    param    = src.param;
    hash     = src.hash;
    entry    = src.entry;
    val      = src.val;
    modified = src.modified;
    return this;
  }

  void write(BitOutput bitStream) {
    bitStream.write31u(param, entry.Save_Param_Bits);
    bitStream.write31u(val + entry.Save_Add, entry.Save_Bits);
  }

  Stat copy() {
    return obtain(this);
  }

  @Override
  public int compareTo(Stat other) {
    return other.entry.descpriority - entry.descpriority;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  public boolean isModified() {
    return modified;
  }

  /**
   * Encodings: param -- value (in bits)
   * 0 : 0    | X
   * 1 : Y    | X
   * 2 : 6,10 | X
   * 3 : 6,10 | 8,8
   * 4 : 0    | 2,10,10
   */
  public Stat add(Stat other) {
    switch (entry.Encode) {
      case 3: {
        int value1 = Math.min(value1() + other.value1(), (1 << 8) - 1);
        int value2 = Math.min(value2() + other.value2(), (1 << 8) - 1);
        val = (value2 << 8) | value1;
        break;
      }
      case 4: {
        // TODO: see issue #24
        int value1 = value1();
        int value2 = Math.min(value2() + other.value2(), (1 << 10) - 1);
        int value3 = Math.min(value3() + other.value3(), (1 << 10) - 1);
        val = (value3 << 12) | (value2 << 2) | value1;
        break;
      }
      case 0:
      case 1:
      case 2:
      default:
        val += other.val;
    }
    modified = true;
    return this;
  }

  public Stat add(int value) {
    switch (entry.Encode) {
      case 3:
      case 4:
        log.error("add unsupported when Encoding = {}", entry.Encode);
        break;
      case 0:
      case 1:
      case 2:
      default:
        val += value;
    }
    modified = true;
    return this;
  }

  // Intended for correcting maxhp, maxstamina and maxmana, see note in CharData
  public Stat set(Stat other) {
    val = other.val;
    other.modified = modified;
    modified = false;
    return this;
  }

  private static final StringBuilder builder = new StringBuilder(32);
  private static final CharSequence SPACE   = Riiablo.string.lookup("space");
  private static final CharSequence DASH    = Riiablo.string.lookup("dash");
  private static final CharSequence PERCENT = Riiablo.string.lookup("percent");
  private static final CharSequence PLUS    = Riiablo.string.lookup("plus");
  private static final CharSequence TO      = Riiablo.string.lookup("ItemStast1k");

  private static final String[] BY_TIME = {
      "ModStre9e", "ModStre9g", "ModStre9d", "ModStre9f",
  };

  public String format(CharData charData) {
    return format(charData, entry.descfunc, entry.descval, entry.descstrpos, entry.descstrneg, entry.descstr2);
  }

  @Deprecated
  public String format(CharData charData, boolean group) {
    return group
        ? format(charData, entry.dgrpfunc, entry.dgrpval, entry.dgrpstrpos, entry.dgrpstrneg, entry.dgrpstr2)
        : format(charData, entry.descfunc, entry.descval, entry.descstrpos, entry.descstrneg, entry.descstr2);
  }

  public String format(CharData charData, int func, int valmode, String strpos, String strneg, String str2) {
    int value;
    CharStats.Entry entry;
    Skills.Entry skill;
    SkillDesc.Entry desc;
    builder.setLength(0);
    switch (func) {
      case 1: // +%d %s1
        value = value();
        if (valmode == 1) builder.append(PLUS).append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value);
        return builder.toString();
      case 2: // %d%% %s1
        value = value();
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      case 3: // %d %s1
        value = value();
        if (valmode == 1) builder.append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value);
        return builder.toString();
      case 4: // +%d%% %s1
        value = value();
        if (valmode == 1) builder.append(PLUS).append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value).append(PERCENT);
        return builder.toString();
      case 5: // %d%% %s1
        value = value() * 100 / 128;
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      case 6: // +%d %s1 %s2
        value = op(charData);
        if (valmode == 1) builder.append(PLUS).append(value).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value);
        return builder.toString();
      case 7: // %d%% %s1 %s2
        value = op(charData);
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      case 8: // +%d%% %s1 %s2
        value = op(charData);
        if (valmode == 1) builder.append(PLUS).append(value).append(PERCENT).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value).append(PERCENT);
        return builder.toString();
      case 9: // %d %s1 %s2
        value = op(charData);
        if (valmode == 1) builder.append(value).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(value);
        return builder.toString();
      case 10: // %d%% %s1 %s2
        value = value() * 100 / 128;
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      case 11: // Repairs 1 Durability in %d Seconds
        value = 100 / value();
        return Riiablo.string.format("ModStre9u", 1, value);
      case 12: // +%d %s1
        value = value();
        if (valmode == 1) builder.append(PLUS).append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value);
        return builder.toString();
      case 13: // +%d %s | +1 to Paladin Skills
        value = value();
        builder
            .append(PLUS).append(value)
            .append(SPACE)
            .append(Riiablo.string.lookup(CharacterClass.get(param).entry().StrAllSkills));
        return builder.toString();
      case 14: // %s %s | +1 to Fire Skills (Sorceress Only)
        value = value();
        entry = CharacterClass.get((param >>> 3) & 0x3).entry();
        builder
            .append(Riiablo.string.format(entry.StrSkillTab[param & 0x7], value))
            .append(SPACE)
            .append(Riiablo.string.lookup(entry.StrClassOnly));
        return builder.toString();
      case 15: // 15% chance to cast Level 5 Life Tap on Striking
        value = value();
        skill = Riiablo.files.skills.get(param2());
        desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        return Riiablo.string.format(strpos, value, param1(), Riiablo.string.lookup(desc.str_name));
      case 16: // Level 16 Defiance Aura When Equipped
        value = value();
        skill = Riiablo.files.skills.get(param);
        desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        return Riiablo.string.format(strpos, value, Riiablo.string.lookup(desc.str_name));
      case 17: // +10 to Dexterity (Increases Near Dawn) // TODO: untested
        // value needs to update based on time of day
        if (valmode == 1) builder.append(PLUS).append(value3()).append(SPACE);
        builder.append(Riiablo.string.lookup(strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value3());
        builder.append(SPACE).append(Riiablo.string.lookup(BY_TIME[value1()]));
        return builder.toString();
      case 18: // 50% Enhanced Defense (Increases Near Dawn) // TODO: untested
        // value needs to update based on time of day
        if (valmode == 1) builder.append(value3()).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(strpos));
        if (valmode == 2) builder.append(SPACE).append(value3()).append(PERCENT);
        builder.append(SPACE).append(Riiablo.string.lookup(BY_TIME[value1()]));
        return builder.toString();
      case 19: // Formats strpos/strneg with value
        value = value();
        return Riiablo.string.format(value < 0 ? strneg : strpos, value);
      case 20: // -%d%% %s1
        value = -value();
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      case 21: // -%d %s1
        value = -value();
        if (valmode == 1) builder.append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value);
        return builder.toString();
      case 22: // +%d%% %s1 %s | +3% Attack Rating Versus: %s // TODO: unsupported for now
        return "ERROR 22";
      case 23: // %d%% %s1 %s | 3% ReanimateAs: %s // TODO: unsupported for now
        return "ERROR 23";
      case 24:
        skill = Riiablo.files.skills.get(param2());
        desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        builder
            .append(Riiablo.string.lookup("ModStre10b")).append(SPACE)
            .append(param1()).append(SPACE)
            .append(Riiablo.string.lookup(desc.str_name)).append(SPACE)
            .append(Riiablo.string.format(strpos, value1(), value2()));
        return builder.toString();
      case 25: // TODO: unsupported
        return "ERROR 25";
      case 26: // TODO: unsupported
        return "ERROR 26";
      case 27: // +1 to Lightning (Sorceress Only)
        value = value();
        skill = Riiablo.files.skills.get(param);
        desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        entry = Riiablo.files.skills.getClass(skill.charclass).entry();
        builder
            .append(PLUS).append(value).append(SPACE)
            .append(TO).append(SPACE)
            .append(Riiablo.string.lookup(desc.str_name)).append(SPACE)
            .append(Riiablo.string.lookup(entry.StrClassOnly));
        return builder.toString();
      case 28: // +1 to Teleport
        value = value();
        skill = Riiablo.files.skills.get(param);
        desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        builder
            .append(PLUS).append(value).append(SPACE)
            .append(TO).append(SPACE)
            .append(Riiablo.string.lookup(desc.str_name));
        return builder.toString();
      default:
        return null;
    }
  }

  public int value() {
    return value1();
  }

  // Looks like this is unused outside character stats
  // fortitude is calculated using OP (statvalue * basevalue) / (2 ^ param) -- (10 * 89) / (2 ^ 3) = 111.25
  public float toFloat() {
    int shift = entry.ValShift;
    int pow   = (1 << shift);
    int mask  = pow - 1;
    return ((val >>> shift) + ((val & mask) / (float) pow));
  }

  public long toLong() {
    return UnsignedInts.toLong(val);
  }

  public int param() {
    return param1();
  }

  public int value1() {
    switch (entry.Encode) {
      case 0:  return val;
      case 1:  return val;
      case 2:  return val;
      case 3:  return val & 0xFF;
      case 4:  return val & 0x3;
      default: return val;
    }
  }

  public int value2() {
    switch (entry.Encode) {
      case 0:  return 0;
      case 1:  return 0;
      case 2:  return 0;
      case 3:  return (val >>> 8) & 0xFF;
      case 4:  return (val >>> 2) & 0x3FF;
      default: return 0;
    }
  }

  public int value3() {
    switch (entry.Encode) {
      case 0:  return 0;
      case 1:  return 0;
      case 2:  return 0;
      case 3:  return 0;
      case 4:  return (val >>> 12) & 0x3FF;
      default: return 0;
    }
  }

  public int param1() {
    switch (entry.Encode) {
      case 0:  return param;
      case 1:  return param;
      case 2:  return param & 0x3F;
      case 3:  return param & 0x3F;
      case 4:  return param;
      default: return param;
    }
  }

  public int param2() {
    switch (entry.Encode) {
      case 0:  return 0;
      case 1:  return 0;
      case 2:  return (param >>> 6) & 0x3FF;
      case 3:  return (param >>> 6) & 0x3FF;
      case 4:  return 0;
      default: return 0;
    }
  }

  private int op(CharData charData, int value) {
    int op_base = entry.op_param > 0
        ? Riiablo.charData.getStats().get(Riiablo.files.ItemStatCost.index(entry.op_base)).value()
        : 1;
    switch (entry.op) {
      case 1:  return value;
      case 2:  return value * op_base / (1 << entry.op_param);
      case 3:  return value;
      case 4:  return value * op_base / (1 << entry.op_param);
      case 5:  return value * op_base / (1 << entry.op_param);
      case 6:  return value; // Unsupported -- time of day
      case 7:  return value; // Unsupported -- time of day %
      case 8:  return value;
      case 9:  return value;
      case 10: return value;
      case 11: return value;
      case 12: return value;
      case 13: return value;
      default: return value;
    }
  }

  public int op(CharData charData) {
    return op1(charData);
  }

  public int op1(CharData charData) {
    return op(charData, value1());
  }

  public int op2(CharData charData) {
    return op(charData, value2());
  }

  public int op3(CharData charData) {
    return op(charData, value3());
  }

  @Override
  public String toString() {
    switch (entry.Encode) {
      case 0:  return id + "(" + entry + ")" + "=" + (entry.Save_Param_Bits == 0 ? Integer.toString(val) : val + ":" + param);
      case 1:  return id + "(" + entry + ")" + "=" + param() + ":" + value();
      case 2:  return id + "(" + entry + ")" + "=" + param1() + ":" + param2() + ":" + value();
      case 3:  return id + "(" + entry + ")" + "=" + param1() + ":" + param2() + ":" + value1() + ":" + value2();
      case 4:  return id + "(" + entry + ")" + "=" + value1() + ":" + value2() + ":" + value3();
      default: return id + "(" + entry + ")" + "=" + (entry.Save_Param_Bits == 0 ? Integer.toString(val) : val + ":" + param);
    }
  }

  static class Aggregate extends Stat {
    int encoding = 19;
    String str;
    String str2;
    Stat[] stats;

    Aggregate(int stat, String str, String str2, Stat... stats) {
      id    = stat;
      param = 0;
      val   = 0;
      entry = Riiablo.files.ItemStatCost.get(stat);
      this.stats = stats;
      this.str = str;
      this.str2 = str2;
    }

    @Override
    public String format(CharData unused0, int unused1, int unused2, String unused3, String unused4, String unused5) {
      if (stats.length == 2) {
        if (stats[0].val == stats[1].val) {
          return Riiablo.string.format(str, stats[1].val);
        } else {
          return Riiablo.string.format(str2, stats[0].val, stats[1].val);
        }
      } else {
        assert stats.length == 3;
        if (stats[0].val == stats[1].val) {
          return Riiablo.string.format(str, stats[1].val, stats[2].val);
        } else {
          return Riiablo.string.format(str2, stats[0].val, stats[1].val, stats[2].val);
        }
      }
    }

    @Override
    public String toString() {
      return id + "(" + entry + ")" + "=" + Arrays.toString(stats) + " : " + format(null, 0, 0, null, null, null);
    }
  }
}
