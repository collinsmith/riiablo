package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;

import java.lang.reflect.Field;

public class Commands {

public static void addTo(CommandManager commandManager) {
    addTo(commandManager, Cvars.class);
}

private static void addTo(CommandManager commandManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
        if (Command.class.isAssignableFrom(field.getType())) {
            try {
                commandManager.add((Command)field.get(null));
            } catch (IllegalAccessException e) {
                Gdx.app.log(Cvars.class.getSimpleName(), "Unable to access cvar: " + e.getMessage());
            }
        }
    }

    for (Class<?> subclass : clazz.getClasses()) {
        addTo(commandManager, subclass);
    }
}

}
