package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class DifficultyLevels {
  @Override
  public String toString() {
    return Name;
  }

  @PrimaryKey
  public String Name;
  public int ResistPenalty;
  public int DeathExpPenalty;
  public int UberCodeOddsNormal;
  public int UberCodeOddsGood;
  public int UltraCodeOddsNormal;
  public int UltraCodeOddsGood;
  public int MonsterSkillBonus;
  public int MonsterFreezeDivisor;
  public int MonsterColdDivisor;
  public int AiCurseDivisor;
  public int LifeStealDivisor;
  public int ManaStealDivisor;
  public int UniqueDamageBonus;
  public int ChampionDamageBonus;
  public int HireableBossDamagePercent;
  public int MonsterCEDamagePercent;
  public int StaticFieldMin;
  public int GambleRare;
  public int GambleSet;
  public int GambleUnique;
  public int GambleUber;
  public int GambleUltra;
}
