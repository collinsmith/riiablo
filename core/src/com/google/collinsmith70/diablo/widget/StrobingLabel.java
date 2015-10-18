package com.google.collinsmith70.diablo.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class StrobingLabel extends Label {
	public static final float DEFAULT_STROBE_FLOOR = 0.50f;
	public static final float DEFAULT_STROBE_CEILING = 1.00f;
	public static final float DEFAULT_STROBE_MOD = 0.25f;
	
	private final float strobeFloor;
	private final float strobeCeiling;
	private final float strobeMod;
	
	private boolean fadingOut;
	private float alpha;

	public StrobingLabel(CharSequence text, LabelStyle style, float strobeFloor, float strobeCeiling, float strobeMod) {
		super(text, style);
		
		this.strobeFloor = strobeFloor;
		this.strobeCeiling = strobeCeiling;
		this.strobeMod = strobeMod;
		
		alpha = 1.0f;
		fadingOut = true;
		
		Color fontColor = getStyle().fontColor;
		fontColor.set(fontColor.r, fontColor.g, fontColor.b, alpha);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (fadingOut) {
			alpha -= (Gdx.graphics.getDeltaTime() * strobeMod);
			if (alpha <= strobeFloor) {
				fadingOut = false;
				alpha = strobeFloor;
			}
		} else {
			alpha += (Gdx.graphics.getDeltaTime() * strobeMod);
			if (strobeCeiling <= alpha) {
				fadingOut = true;
				alpha = strobeCeiling;
			}
		}

		Color fontColor = getStyle().fontColor;
		fontColor.set(fontColor.r, fontColor.g, fontColor.b, alpha);
		super.draw(batch, parentAlpha);
	}
}
