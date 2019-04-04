package com.riiablo.codec.excel;

public class DifficultyLevels extends Excel<DifficultyLevels.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Key
    @Column public String  Name;
    @Column public int     ResistPenalty;
    @Column public int     DeathExpPenalty;
    @Column public int     UberCodeOddsNormal;
    @Column public int     UberCodeOddsGood;
    @Column public int     UltraCodeOddsNormal;
    @Column public int     UltraCodeOddsGood;
    @Column public int     MonsterSkillBonus;
    @Column public int     MonsterFreezeDivisor;
    @Column public int     MonsterColdDivisor;
    @Column public int     AiCurseDivisor;
    @Column public int     LifeStealDivisor;
    @Column public int     ManaStealDivisor;
    @Column public int     UniqueDamageBonus;
    @Column public int     ChampionDamageBonus;
    @Column public int     HireableBossDamagePercent;
    @Column public int     MonsterCEDamagePercent;
    @Column public int     StaticFieldMin;
    @Column public int     GambleRare;
    @Column public int     GambleSet;
    @Column public int     GambleUnique;
    @Column public int     GambleUber;
    @Column public int     GambleUltra;
  }
}