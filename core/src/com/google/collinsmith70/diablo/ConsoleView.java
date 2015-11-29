package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;

public class ConsoleView extends Console implements Disposable {

private final Caret CARET;
private final Texture modelBackgroundTexture;

private boolean isVisible;
private BitmapFont font;
private float outputOffset;

public ConsoleView(Client client) {
    super(client);
    this.isVisible = false;

    this.CARET = new Caret();

    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 0.5f);
    solidColorPixmap.fill();
    modelBackgroundTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();
    solidColorPixmap = null;

    final CvarChangeListener<Float> consoleFontColorCvarListener = new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.a)) {
                font.getColor().a = toValue;
            } else if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.r)) {
                font.getColor().r = toValue;
            } else if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.g)) {
                font.getColor().g = toValue;
            } else if (cvar.equals(Cvars.Client.Overlay.ConsoleFontColor.b)) {
                font.getColor().b = toValue;
            }
        }
    };

    Cvars.Client.Overlay.ConsoleFont.addCvarChangeListener(new CvarChangeListener<AssetDescriptor<BitmapFont>>() {
        @Override
        public void onCvarChanged(Cvar<AssetDescriptor<BitmapFont>> cvar, AssetDescriptor<BitmapFont> fromValue, AssetDescriptor<BitmapFont> toValue) {
            AssetManager assetManager = getClient().getAssetManager();
            assetManager.load(toValue);
            assetManager.finishLoading();

            ConsoleView.this.font = assetManager.get(Cvars.Client.Overlay.ConsoleFont.getValue());
            Cvars.Client.Overlay.ConsoleFontColor.r.addCvarChangeListener(consoleFontColorCvarListener);
            Cvars.Client.Overlay.ConsoleFontColor.g.addCvarChangeListener(consoleFontColorCvarListener);
            Cvars.Client.Overlay.ConsoleFontColor.b.addCvarChangeListener(consoleFontColorCvarListener);
            Cvars.Client.Overlay.ConsoleFontColor.a.addCvarChangeListener(consoleFontColorCvarListener);
        }
    });
}

public boolean isVisible() {
    return isVisible;
}

public void setVisible(boolean b) {
    this.isVisible = b;
    Gdx.input.setOnscreenKeyboardVisible(isVisible());
    if (isVisible()) {
        //...
    }
}

public BitmapFont getFont() {
    return font;
}

public void clear() {
    // TODO: add abstraction to clear the console (which is separate from stream)
}

@Override
public void setPosition(int position) {
    super.setPosition(position);
    if (CARET != null) {
        CARET.setVisible(true);
    }
}

public void render(Batch b) {
    if (!isVisible()) {
        return;
    }

    b.draw(modelBackgroundTexture, 0.0f, 0.0f, getClient().getVirtualWidth(), getClient().getVirtualHeight());
    GlyphLayout glyphs = font.draw(b, getBufferPrefix() + " " + getBuffer(), 0, getClient().getVirtualHeight());

    glyphs.setText(font, getBufferPrefix() + " " + getBuffer().substring(0, getPosition()));
    CARET.render(b, font, glyphs, getClient().getVirtualHeight() - 1);
}

@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    Gdx.input.setOnscreenKeyboardVisible(true);
    return super.touchDown(screenX, screenY, pointer, button);
}

public boolean scrolled(int amount) {
    switch (amount) {
        case -1:
            //outputOffset = Math.min(
            //        outputOffset + font.getLineHeight(),
            //        getClient().getVirtualHeight() - 2*font.getLineHeight());
            break;
        case 1:
            //outputOffset = Math.max(
            //        outputOffset - font.getLineHeight(),
            //        getClient().getVirtualHeight() - (getOutputStream() * font.getLineHeight()));
            break;
    }

    return true;
}

@Override
public void dispose() {
    font.dispose();
}

private static class Caret {

    static final float DEFAULT_HOLD_DELAY = 1.0f;
    static final float DEFAULT_BLINK_DELAY = 0.5f;

    final Timer TIMER;
    final Timer.Task BLINK_TASK;

    float holdDelay;
    float blinkDelay;

    boolean isVisible;

    Caret() {
        this(DEFAULT_HOLD_DELAY, DEFAULT_BLINK_DELAY);
    }

    Caret(float holdDelay, float blinkDelay) {
        if (holdDelay < 0.1f) {
            throw new IllegalArgumentException("holdDelay should be >= 0.1f");
        }

        if (blinkDelay < 0.1f) {
            throw new IllegalArgumentException("blinkDelay should be >= 0.1f");
        }

        this.holdDelay = holdDelay;
        this.blinkDelay = blinkDelay;

        this.isVisible = true;

        TIMER = new Timer();
        BLINK_TASK = new Timer.Task() {
            @Override
            public void run() {
                Caret.this.setVisible(!Caret.this.isVisible());
            }
        };

        TIMER.start();
        setVisible(true);
    }

    boolean isVisible() {
        return isVisible;
    }

    void setVisible(boolean b) {
        this.isVisible = b;
        BLINK_TASK.cancel();
        TIMER.schedule(BLINK_TASK, getHoldDelay(), getBlinkDelay());
    }

    float getHoldDelay() {
        return holdDelay;
    }

    void setHoldDelay(float holdDelay) {
        this.holdDelay = holdDelay;
    }

    float getBlinkDelay() {
        return blinkDelay;
    }

    void setBlinkDelay(float blinkDelay) {
        this.blinkDelay = blinkDelay;
    }

    void render(Batch batch, BitmapFont font, GlyphLayout glyphs, float height) {
        if (!isVisible()) {
            return;
        }

        // TODO: underscore should match width of above character
        font.draw(batch, "_", glyphs.width - 4, height);
    }

}

}
