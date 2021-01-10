package com.riiablo.table.schema;

import com.riiablo.CharacterClass;
import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Skills {
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

  @Override
  public String toString() {
    return skill;
  }

  @PrimaryKey
  public int Id;
  public String skill;
  public String charclass;
  public String skilldesc;
  public String stsound;
  public String stsoundclass;
  public String dosound;
  public String castoverlay;
  public String anim;
  public String seqtrans;
  public String monanim;
  public int seqnum;
  public int seqinput;
  public int reqlevel;
  public int startmana;
  public int minmana;
  public int manashift;
  public int mana;
  public int lvlmana;

  @Format(
      startIndex = 1,
      endIndex = 9)
  public int Param[];

  public boolean leftskill;
  public boolean passive;
  public boolean aura;
  public int srvstfunc;
  public int srvdofunc;
  public int cltstfunc;
  public int cltdofunc;
  public String cltmissilea;
  public String cltmissileb;
  public String cltmissilec;
  public String cltmissiled;
}
