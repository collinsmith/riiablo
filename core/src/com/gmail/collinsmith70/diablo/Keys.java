package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.gmail.collinsmith70.key.Key;
import com.gmail.collinsmith70.key.KeyManager;

import java.lang.reflect.Field;

public class Keys {

public static <T> void addTo(KeyManager<T> keyManager) {
    addTo(keyManager, Keys.class);
}

@SuppressWarnings("unchecked")
private static <T> void addTo(KeyManager<T> keyManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
        if (Key.class.isAssignableFrom(field.getType())) {
            try {
                Key<T> key = (Key<T>)field.get(null);
                keyManager.add(key);
            } catch (IllegalAccessException e) {
                Gdx.app.log(Keys.class.getSimpleName(), "Unable to access key: " + e.getMessage());
            }
        }
    }

    for (Class<?> subclass : clazz.getClasses()) {
        addTo(keyManager, subclass);
    }
}

private Keys() {}

public static final Key<Integer> Console = new Key<Integer>("Console", "console", Input.Keys.GRAVE);

}
