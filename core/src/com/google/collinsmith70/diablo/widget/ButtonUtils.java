package com.google.collinsmith70.diablo.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.collinsmith70.diablo.cvar.Cvars;

public class ButtonUtils {

public static <T extends Button> T playSoundOnClicked(T button, final Sound PRESSED) {
    button.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            PRESSED.play();
            if (Cvars.Client.Input.Vibrations.getValue()) {
                Gdx.input.vibrate(25);
            }
        }
    });

    return button;
}

}
