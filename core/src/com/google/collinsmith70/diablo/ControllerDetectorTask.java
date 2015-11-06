package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Timer;

import java.lang.reflect.Field;

public class ControllerDetectorTask extends Timer.Task {
@Override
public void run() {
    int count = 0;
    reloadControllers();
    for (Controller c : Controllers.getControllers()) {
        Gdx.app.log("TEST", String.format("[%d] %s %d", count++, c.getName(), c.hashCode()));
    }

    Gdx.app.log("TEST", String.format("%d controllers", count));
}

/**
 * @see <a href="https://github.com/libgdx/libgdx/issues/2182#issuecomment-72753138">source</a>
 */
private boolean reloadControllers() {
    try {
        Class controllersClass = Class.forName("com.badlogic.gdx.controllers.Controllers");
        Field managersField = controllersClass.getDeclaredField("managers");
        managersField.setAccessible(true);
        ObjectMap<Application, ControllerManager> managers
                = (ObjectMap<Application, ControllerManager>)managersField.get(null);

        managersField.setAccessible(false);
        managers.put(Gdx.app,
                (ControllerManager)Class.forName("com.badlogic.gdx.controllers.desktop.DesktopControllerManager")
                        .newInstance());

        Field runnablesField = Class.forName("com.badlogic.gdx.backends.lwjgl.LwjglApplication")
                .getDeclaredField("runnables");

        runnablesField.setAccessible(true);
        Array<Runnable> runnables = (Array<Runnable>)runnablesField.get(Gdx.app);
        runnablesField.setAccessible(false);
        runnables.removeIndex(runnables.size-1);
        return true;
    } catch (Exception e) {
        Gdx.app.error(ControllerDetectorTask.class.getName(), "Error reloading controllers!", e);
        return false;
    }
}
}
