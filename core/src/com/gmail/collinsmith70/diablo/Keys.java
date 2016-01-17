package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.key.Key;
import com.gmail.collinsmith70.key.KeyManager;

import java.lang.reflect.Field;

public class Keys {

public static <T> void addTo(KeyManager<T> keyManager) {
    addTo(keyManager, Keys.class);
}

private static <T> void addTo(KeyManager<T> keyManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
        if (Key.class.isAssignableFrom(field.getType())) {
            try {
                keyManager.add((Key<T>)field.get(null));
            } catch (IllegalAccessException e) {
                Gdx.app.log(Keys.class.getSimpleName(), "Unable to access key: " + e.getMessage());
            }
        }
    }

    for (Class<?> subclass : clazz.getClasses()) {
        addTo(keyManager, subclass);
    }
}

private Keys() {
    //...
}

}
