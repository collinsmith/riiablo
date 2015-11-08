package com.google.collinsmith70.diablo.widget.panel;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.scene.AbstractScene;

public abstract class AbstractPanel extends AbstractScene {

private Texture modelBackgroundTexture;
private boolean modal;

public AbstractPanel(Client client) {
    super(client);
    setFillParent(true);
    setTouchable(Touchable.enabled);
    setModal(false);
}

@Override
public void create() {
    super.create();

    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 0.5f);
    solidColorPixmap.fill();
    modelBackgroundTexture = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();
    solidColorPixmap = null;
}

public boolean isModal() {
    return modal;
}

public void setModal(boolean b) {
    this.modal = b;
}

@Override
public void dispose() {
    super.dispose();
    remove();
}

@Override
protected void drawBackground(Batch batch) {
    if (isModal()) {
        batch.draw(modelBackgroundTexture, 0.0f, 0.0f, getClient().getVirtualWidth(), getClient().getVirtualHeight());
    }
}

}
