package com.google.collinsmith70.diablo.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActorUtils {

private ActorUtils() {
    //...
}

public static void centerAt(Actor a, float x, float y) {
    a.setPosition(
            x - a.getWidth() / 2,
            y - a.getHeight() / 2);
}

}
