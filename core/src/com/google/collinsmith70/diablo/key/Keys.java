package com.google.collinsmith70.diablo.key;

import com.badlogic.gdx.Input;

import java.lang.reflect.Field;

public class Keys {

private Keys() {
    //...
}

public static void loadAll() {
    init(Keys.class);
}

public static void init(Class<?> clazz) {
    // Hack to instantiate all Cvars
    for (Field field : clazz.getFields()) {
        //...
    }

    for (Class subclass : clazz.getClasses()) {
        init(subclass);
    }
}

public static final Key CONSOLE = new Key("console", Input.Keys.GRAVE);

}
