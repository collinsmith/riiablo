package com.riiablo.codec.excel;

import com.riiablo.CharacterClass;

@Excel.Binned
public class Skills extends Excel<Skills.Entry> {
  public static int getClassId(String charClass) {
    if (charClass.isEmpty()) return -1;
    switch (charClass.charAt(0)) {
      case 'a': return charClass.charAt(1) == 'm' ? CharacterClass.AMAZON.id : CharacterClass.ASSASSIN.id;
      case 'b': return CharacterClass.BARBARIAN.id;
      case 'd': return CharacterClass.DRUID.id;
      case 'n': return CharacterClass.NECROMANCER.id;
      case 'p': return CharacterClass.PALADIN.id;
      case 's': return CharacterClass.SORCERESS.id;
      default:  return -1;
    }
  }

  public static CharacterClass getClass(String charClass) {
    int classId = getClassId(charClass);
    return classId != -1 ? CharacterClass.get(classId) : null;
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return skill;
    }

    @Key
    @Column
    public int     Id;
    @Column public String  skill;
    @Column public String  charclass;
    @Column public String  skilldesc;
    @Column public String  stsound;
    @Column public String  stsoundclass;
    @Column public String  dosound;
    @Column public String  castoverlay;
    @Column public String  anim;
    @Column public String  seqtrans;
    @Column public String  monanim;
    @Column public int     seqnum;
    @Column public int     seqinput;
    @Column public int     reqlevel;
    @Column public int     startmana;
    @Column public int     minmana;
    @Column public int     manashift;
    @Column public int     mana;
    @Column public int     lvlmana;
    @Column(startIndex = 1, endIndex = 9)
    public int     Param[];
    @Column public boolean leftskill;
    @Column public boolean passive;
    @Column public boolean aura;
    @Column public int     srvstfunc;
    @Column public int     srvdofunc;
    @Column public int     cltstfunc;
    @Column public int     cltdofunc;
  }
}
