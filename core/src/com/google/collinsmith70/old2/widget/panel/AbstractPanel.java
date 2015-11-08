package com.google.collinsmith70.old2.widget.panel;

public abstract class AbstractPanel {
	/*private final Texture MODAL_BACKGROUND;
	private boolean modal;
	
	private Label title;
	
	public AbstractPanel(Client client) {
		super(client);
		setFillParent(false);
		setTouchable(Touchable.enabled);
		
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(0.0f, 0.0f, 0.0f, 0.50f);
		pixmap.fill();
		MODAL_BACKGROUND = new Texture(pixmap);
		pixmap.dispose();
		
		this.modal = false;
	}
	
	public void closePanel() {
		dispose();
		remove();
	}

	@Override
	public void setBackground(TextureRegion background) {
		super.setBackground(background);
		setSize(background.getRegionWidth(), background.getRegionHeight());
	}

	public void setTitle(Label title) {
		if (this.title != null) {
			removeActor(this.title);
		}

		this.title = title;
		addActor(this.title);
	}
	
	public Label getTitle() {
		return title;
	}

	@Override
	protected void drawBackground(Batch batch) {
		if (modal) {
			batch.draw(MODAL_BACKGROUND, 0.0f, 0.0f, getClient().getVirtualWidth(), getClient().getVirtualHeight());
		}

		if (getBackground() != null) {
			batch.draw(getBackground(), getX(), getY(), getWidth(), getHeight());
		}
	}
	
	public void setModal(boolean b) {
		this.modal = b;
	}
	
	public boolean isModal() {
		return modal;
	}*/
}
