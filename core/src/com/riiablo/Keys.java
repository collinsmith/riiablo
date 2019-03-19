package com.riiablo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.riiablo.key.KeyMapper;
import com.riiablo.key.MappedKey;

import java.util.ArrayList;
import java.util.Collection;

public class Keys {
  public static Collection<Throwable> addTo(KeyMapper keyMapper) {
    return addTo(keyMapper, Keys.class, new ArrayList<Throwable>(0));
  }

  private static Collection<Throwable> addTo(KeyMapper keyMapper, Class<?> clazz, Collection<Throwable> throwables) {
    for (Field field : ClassReflection.getFields(clazz)) {
      if (MappedKey.class.isAssignableFrom(field.getType())) {
        try {
          keyMapper.add((MappedKey) field.get(null));
        } catch (Throwable t) {
          throwables.add(t);
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(keyMapper, subclass, throwables);
    }

    return throwables;
  }

  private Keys() {}

  public static final MappedKey Console = new MappedKey("Console", "console", Input.Keys.GRAVE);
  public static final MappedKey Esc = new MappedKey("Esc", "esc", Input.Keys.ESCAPE, Input.Keys.BACK);
  public static final MappedKey Inventory = new MappedKey("Inventory", "inventory", Input.Keys.I, Input.Keys.B);
  public static final MappedKey Character = new MappedKey("Character", "character", Input.Keys.C, Input.Keys.A);
  public static final MappedKey Spells = new MappedKey("Spells", "spells", Input.Keys.S, Input.Keys.T);
  public static final MappedKey Stash = new MappedKey("Stash", "stash", Input.Keys.NUMPAD_1);
  public static final MappedKey SwapWeapons = new MappedKey("SwapWeapons", "swap", Input.Keys.W);
  public static final MappedKey Enter = new MappedKey("Enter", "enter", Input.Keys.ENTER);

  public static final MappedKey Skill1  = new MappedKey("Skill 1",  "skill1",  Input.Keys.F1);
  public static final MappedKey Skill2  = new MappedKey("Skill 2",  "skill2",  Input.Keys.F2);
  public static final MappedKey Skill3  = new MappedKey("Skill 3",  "skill3",  Input.Keys.F3);
  public static final MappedKey Skill4  = new MappedKey("Skill 4",  "skill4",  Input.Keys.F4);
  public static final MappedKey Skill5  = new MappedKey("Skill 5",  "skill5",  Input.Keys.F5);
  public static final MappedKey Skill6  = new MappedKey("Skill 6",  "skill6",  Input.Keys.F6);
  public static final MappedKey Skill7  = new MappedKey("Skill 7",  "skill7",  Input.Keys.F7);
  public static final MappedKey Skill8  = new MappedKey("Skill 8",  "skill8",  Input.Keys.F8);
  public static final MappedKey Skill9  = new MappedKey("Skill 9",  "skill9",  MappedKey.NOT_MAPPED);
  public static final MappedKey Skill10 = new MappedKey("Skill 10", "skill10", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill11 = new MappedKey("Skill 11", "skill11", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill12 = new MappedKey("Skill 12", "skill12", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill13 = new MappedKey("Skill 13", "skill13", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill14 = new MappedKey("Skill 14", "skill14", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill15 = new MappedKey("Skill 15", "skill15", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill16 = new MappedKey("Skill 16", "skill16", MappedKey.NOT_MAPPED);
  public static final MappedKey Skill[] = new MappedKey[] {
      Skill1, Skill2, Skill3, Skill4, Skill5, Skill6, Skill7, Skill8,
      Skill9, Skill10, Skill11, Skill12, Skill13, Skill14, Skill15, Skill16
  };
}
