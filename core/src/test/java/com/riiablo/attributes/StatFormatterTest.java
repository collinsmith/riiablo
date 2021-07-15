package com.riiablo.attributes;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class StatFormatterTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.attributes", Level.TRACE);
  }
  
  private static StatFormatter newInstance() {
    return new StatFormatter();
  }
  
  private static StatList newStatList() {
    return new StatList().reset(1);
  }

  @Test
  public void strength() { // func 1
    StatRef stat = newStatList().buildList().put(Stat.strength, 5);
    assertEquals("+5 to Strength", newInstance().format(stat, null));
  }

  @Test
  public void toblock() { // func 2
    StatRef stat = newStatList().buildList().put(Stat.toblock, 25);
    assertEquals("25% Increased Chance of Blocking", newInstance().format(stat, null));
  }

  @Test
  public void item_maxdamage_percent() { // func 3, valmode 0
    StatRef stat = newStatList().buildList().put(Stat.item_maxdamage_percent, 300);
    assertEquals("Enhanced Maximum Damage", newInstance().format(stat, null));
  }

  @Test
  public void magic_damage_reduction() { // func 3, valmode 2
    StatRef stat = newStatList().buildList().put(Stat.magic_damage_reduction, 10);
    assertEquals("Magic Damage Reduced by 10", newInstance().format(stat, null));
  }

  @Test
  public void fireresist() { // func 4, valmode 2
    StatRef stat = newStatList().buildList().put(Stat.fireresist, 45);
    assertEquals("Fire Resist +45%", newInstance().format(stat, null));
  }

  @Test
  public void maxfireresist() { // func 4, valmode 1
    StatRef stat = newStatList().buildList().put(Stat.maxfireresist, 5);
    assertEquals("+5% to Maximum Fire Resist", newInstance().format(stat, null));
  }

  @Test
  public void item_addclassskills() { // func 13, valmode 1
    StatRef stat = newStatList().buildList().putEncoded(Stat.item_addclassskills, Riiablo.BARBARIAN, 3);
    assertEquals("+3 to Barbarian Skill Levels", newInstance().format(stat, null));
  }

  @Test
  public void item_nonclassskill() { // func 28, valmost 0
    StatRef stat = newStatList().buildList().putEncoded(Stat.item_nonclassskill, 54, 1);
    assertEquals("+1 to Teleport", newInstance().format(stat, null));
  }

  @Test
  public void item_hp_perlevel() { // func 6, valmode 1, op 2, op_param 3
    Attributes attrs = Attributes.obtainLarge();
    attrs.base().put(Stat.level, 5);
    attrs.reset();
    StatRef stat = newStatList().buildList().put(Stat.item_hp_perlevel, 10 << 3);
    assertEquals("+50 to Life (Based on Character Level)", newInstance().format(stat, attrs));
  }
}
