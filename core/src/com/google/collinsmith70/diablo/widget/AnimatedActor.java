package com.google.collinsmith70.diablo.widget;

import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class AnimatedActor extends Actor {

private final Animation<TextureRegion> ANIMATION;
private final Texture TEXTURE;

private float timeInState;

private static boolean renderBounds;
static {
    Cvars.Client.Render.AnimationBounds.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            AnimatedActor.renderBounds = toValue;
        }
    });
}

public AnimatedActor(Texture sheet, int numFrames, int frameWidth, int frameHeight, float frameDuration) {
    TEXTURE = sheet;
    TextureRegion[][] tmp = TextureRegion.split(TEXTURE, frameWidth, frameHeight);
    TextureRegion[] frames = new TextureRegion[numFrames];
    for (int i = 0, k = 0; i < tmp.length; i++) {
        for (int j = 0; j < tmp[i].length; j++) {
            frames[k++] = tmp[i][j];
        }
    }

    ANIMATION = new Animation<TextureRegion>(frameDuration, Array.with(frames), Animation.PlayMode.LOOP);
    timeInState = 0.0f;

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
    timeInState += delta;
}

@Override
public void draw(Batch batch, float parentAlpha) {
    if (AnimatedActor.renderBounds) {
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 0);
        shapeRenderer.rect(getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        shapeRenderer.end();
    }

    batch.draw(ANIMATION.getKeyFrame(timeInState),
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation());

}

}
