package gdx.diablo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;

import java.util.ArrayList;
import java.util.Collection;

import gdx.diablo.key.KeyMapper;
import gdx.diablo.key.MappedKey;

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
  public static final MappedKey Stash = new MappedKey("Stash", "stash", Input.Keys.NUMPAD_1);
  public static final MappedKey SwapWeapons = new MappedKey("SwapWeapons", "swap", Input.Keys.W);
  public static final MappedKey Enter = new MappedKey("Enter", "enter", Input.Keys.ENTER);

}
