package com.google.collinsmith70.old2.scene;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;
import com.google.collinsmith70.old2.Client;
import com.google.collinsmith70.old2.widget.panel.ConsolePanel;

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

    @Override
    public boolean keyDown(int keycode) {
        final ConsolePanel CONSOLE = getClient().getConsole();
        switch (keycode) {
            case Input.Keys.MENU:
                if (!CONSOLE.isVisible()) {
                    CONSOLE.setVisible(true);
                    return false;
                }

                return true;
            case Input.Keys.BACK:
            case Input.Keys.ESCAPE:
                if (CONSOLE.isVisible()) {
                    CONSOLE.setVisible(false);
                    return false;
                }

                return true;
        }

        return true;
    }

    @Override
    public boolean keyTyped(char ch) {
        final ConsolePanel CONSOLE = getClient().getConsole();
        if (ch == '`') {
            CONSOLE.setVisible(!CONSOLE.isVisible());
            return false;
        } else if (CONSOLE.isVisible()) {
            CONSOLE.keyTyped(ch);
            return false;
        }

        return true;
    }
}
