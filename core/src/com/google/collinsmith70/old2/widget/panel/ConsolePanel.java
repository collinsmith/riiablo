package com.google.collinsmith70.old2.widget.panel;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.collinsmith70.old2.Assets;
import com.google.collinsmith70.old2.Client;
import com.google.collinsmith70.util.EffectivelyFinal;

public class ConsolePanel extends AbstractPanel {
    @EffectivelyFinal
    private TextureAtlas CONSOLE_ATLAS;
    @EffectivelyFinal
    private BitmapFont CONSOLE;

    @EffectivelyFinal
    private Label COMMAND;

    private StringBuffer consoleBuffer;

    public ConsolePanel(Client client) {
        super(client);
        setModal(true);
        clearBuffer();
    }

    public void clearBuffer() {
        consoleBuffer = new StringBuffer(32);
    }

    public String getBuffer() {
        return consoleBuffer.toString();
    }

    @Override
    public void loadAssets() {
		getClient().getAssetManager().load(Assets.Fonts.ASSET_CONSOLE16);
        getClient().getAssetManager().finishLoading();
    }

    @Override public void dispose() {
        try {
            getClient().getAssetManager().unload(Assets.Fonts.ASSET_CONSOLE16.toString());
        } catch (GdxRuntimeException e) {
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            Gdx.input.setOnscreenKeyboardVisible(isVisible());
        }
    }

    @Override
    public void show() {
        this.CONSOLE = getClient().getAssetManager().get(Assets.Fonts.ASSET_CONSOLE16);
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = CONSOLE;
        titleStyle.fontColor = new Color(
                1.0f,
                1.0f,
                1.0f,
                1.0f);

        COMMAND = new Label(getBuffer() + "_", titleStyle);
        COMMAND.setPosition(0, getClient().getVirtualHeight() - CONSOLE.getLineHeight());
        addActor(COMMAND);
    }

    @Override
    public boolean keyDown(int keycode) {
        return true;
    }

    @Override
    public boolean keyTyped(char ch) {
        if (isVisible()) {
            switch (ch) {
                case '\b':
                    if (consoleBuffer.length() > 0) {
                        consoleBuffer.deleteCharAt(consoleBuffer.length() - 1);
                    }

                    COMMAND.setText(consoleBuffer + "_");
                    break;
                case '\r':
                case '\n':
                    getClient().process(consoleBuffer.toString());
                    consoleBuffer = new StringBuffer(32);
                    COMMAND.setText(consoleBuffer + "_");
                    break;
                default:
                    consoleBuffer.append(ch);
                    COMMAND.setText(consoleBuffer + "_");
                    break;
            }

            Gdx.app.debug("", "Command = " + COMMAND.getText());
            return false;
        }

        return true;
    }

    @Override public void render(float delta) {
    }
    @Override public void pause() {
    }
    @Override public void resume() {
    }

    @Override public boolean keyUp(int keycode) {
        return true;
    }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }
    @Override public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }
    @Override public boolean scrolled(int amount) {
        return true;
    }
}
