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

public static final Key SKILL_1 = new Key("skill_1", Input.Keys.Q);
public static final Key SKILL_2 = new Key("skill_2", Input.Keys.W);
public static final Key SKILL_3 = new Key("skill_3", Input.Keys.E);
public static final Key SKILL_4 = new Key("skill_4", Input.Keys.R);
public static final Key SKILL_5 = new Key("skill_5", Input.Keys.T);
public static final Key SKILL_6 = new Key("skill_6", Input.Keys.A);
public static final Key SKILL_7 = new Key("skill_7", Input.Keys.S);
public static final Key SKILL_8 = new Key("skill_8", Input.Keys.D);
public static final Key SKILL_9 = new Key("skill_9", Input.Keys.F);
public static final Key SKILL_0 = new Key("skill_0", Input.Keys.G);

public static final Key BELT_1 = new Key("belt_1", Input.Keys.NUM_1);
public static final Key BELT_2 = new Key("belt_2", Input.Keys.NUM_2);
public static final Key BELT_3 = new Key("belt_3", Input.Keys.NUM_3);
public static final Key BELT_4 = new Key("belt_4", Input.Keys.NUM_4);

}
