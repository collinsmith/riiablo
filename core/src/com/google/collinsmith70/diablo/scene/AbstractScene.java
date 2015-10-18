package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;

import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.util.EventHandler;

public abstract class AbstractScene extends WidgetGroup implements Disposable, InputProcessor {
    public abstract void render(float delta);
    public abstract void show();
    public abstract void pause();
    public abstract void resume();
    public abstract void loadAssets();

    private final Client CLIENT;

    public AbstractScene(Client client) {
        this.CLIENT = client;

        setFillParent(true);
        addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                return EventHandler.handle(event, AbstractScene.this);
            }
        });
    }

    public Client getClient() {
        return CLIENT;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        drawBackground(batch);
        super.draw(batch, parentAlpha);
    }

    protected void drawBackground(Batch batch) {
    }
}
