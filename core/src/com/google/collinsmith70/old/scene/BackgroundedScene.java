package com.google.collinsmith70.old.scene;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.collinsmith70.old.Client;

public abstract class BackgroundedScene extends AbstractScene {
	private TextureRegion background;
	private float x, y, width, height;

	public BackgroundedScene(Client client) {
		super(client);
	}
	
	public void setBackground(TextureRegion background) {
		this.background = background;
		width = (float)((float)background.getRegionWidth()/background.getRegionHeight())*getClient().getVirtualHeight();
		height = getClient().getVirtualHeight();
		x = (getClient().getVirtualWidth()-width)/2;
		y = 0;
	}
	
	public TextureRegion getBackground() {
		return background;
	}

	@Override
	protected void drawBackground(Batch batch) {
		if (background != null) {
			batch.draw(background, x, y, width, height);
		}
	}
}
