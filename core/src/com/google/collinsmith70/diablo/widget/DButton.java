package com.google.collinsmith70.diablo.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class DButton<T extends Button> extends Button {

public static <T> T wrap(T button) {
    return button;
}

}
