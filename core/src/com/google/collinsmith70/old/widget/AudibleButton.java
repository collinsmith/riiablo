package com.google.collinsmith70.old.widget;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class AudibleButton {
	public static <T extends Button> T wrap(T parent, final Sound PRESSED) {
		parent.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				PRESSED.play();
			}
		});

		return parent;
	}
	
	private <T extends Button> AudibleButton(T parent, final Sound PRESSED) {
		parent.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				PRESSED.play();
			}
		});
	}
}