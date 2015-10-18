package com.google.collinsmith70.diablo.widget;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class AnimationActor extends Actor {
	private final Animation ANIMATION;
	private final Texture TEXTURE;
	
	private float stateTime;

	public AnimationActor(Texture sheet, int numFrames, int frameWidth, int frameHeight, float frameDuration) {
		TEXTURE = sheet;
		TextureRegion[][] tmp = TextureRegion.split(TEXTURE, frameWidth, frameHeight);
		TextureRegion[] frames = new TextureRegion[numFrames];
		for (int i = 0, k = 0; i < tmp.length; i++) {
			for (int j = 0; j < tmp[i].length; j++) {
				frames[k++] = tmp[i][j];
			}
		}

		ANIMATION = new Animation(frameDuration, Array.with(frames), Animation.PlayMode.LOOP);
		stateTime = 0.0f;

		setSize(frameWidth, frameHeight);
	}

	public Animation getAnimation() {
		return ANIMATION;
	}

	public Texture getTexture() {
		return TEXTURE;
	}

	@Override
	public void act(float delta) {
		stateTime += delta;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.draw(ANIMATION.getKeyFrame(stateTime), getX(), getY(), getOriginX(), getOriginY(),
			getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}
}
