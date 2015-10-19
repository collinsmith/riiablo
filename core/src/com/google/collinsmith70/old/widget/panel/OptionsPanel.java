package com.google.collinsmith70.old.widget.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.collinsmith70.old.Assets;
import com.google.collinsmith70.old.Client;
import com.google.collinsmith70.old.EffectivelyFinal;
import com.google.collinsmith70.old.util.Actors;
import com.google.collinsmith70.old.widget.AudibleButton;

public class OptionsPanel extends AbstractPanel {
//	public static final String PANEL_PATH = "textures/options_panel.pack";
	@EffectivelyFinal
	private TextureAtlas PANEL_ATLAS;
	@EffectivelyFinal
	private Skin PANEL_SKIN;

	@EffectivelyFinal
	private Sound PRESSED;

	@EffectivelyFinal
	private TextureAtlas EXOCET_ATLAS;
	@EffectivelyFinal
	private BitmapFont EXOCET;

	@EffectivelyFinal
	private ImageButton btnExit;

	@EffectivelyFinal
	private TextureRegion switchpanel;

	@EffectivelyFinal
	private TextureAtlas BUTTONS_ATLAS;
	@EffectivelyFinal
	private Skin BUTTONS_SKIN;
	@EffectivelyFinal
	private TextButton btnGeneral;
	@EffectivelyFinal
	private TextButton btnGraphics;
	@EffectivelyFinal
	private TextButton btnAudio;
	@EffectivelyFinal
	private ButtonGroup switchboardButtons;

	@Override
	public void loadAssets() {
//		getClient().getAssetManager().load(PANEL_PATH, TextureAtlas.class);
//		getClient().getAssetManager().load(BUTTON_PRESSED_PATH, Sound.class);
//		getClient().getAssetManager().load(EXOCET_PATH, TextureAtlas.class);
		getClient().getAssetManager().finishLoading();
	}


	public OptionsPanel(Client client) {
		super(client);
		setModal(true);
	}

	@Override
	public void show() {
		this.PRESSED = getClient().getAssetManager().get(Assets.Audio.Cursor.ASSET_BUTTON);

		this.PANEL_ATLAS = getClient().getAssetManager().get(Assets.Textures.ASSET_OPTIONS_PANEL);
		this.PANEL_SKIN = new Skin(PANEL_ATLAS);
		TextureRegion background = PANEL_SKIN.getRegion("panel");
		setBackground(background);
        Actors.centerAt(this, getClient().getVirtualWidth() / 2, getClient().getVirtualHeight() / 2);

		switchpanel = PANEL_SKIN.getRegion("switchboard");

		this.EXOCET = getClient().getAssetManager().get(Assets.Fonts.ASSET_EXOCET16);
		LabelStyle titleStyle = new LabelStyle();
		titleStyle.font = EXOCET;
		titleStyle.fontColor = Color.WHITE;

		Label title = new Label("Options", titleStyle);
        Actors.centerAt(title, background.getRegionWidth() / 2, background.getRegionHeight() - 20);
		setTitle(title);

		ImageButtonStyle style = new ImageButtonStyle();
		style.imageUp = PANEL_SKIN.getDrawable("button_exit_enabled");
		style.imageDown = PANEL_SKIN.getDrawable("button_exit_selected");
		style.imageOver = PANEL_SKIN.getDrawable("button_exit_rollover");
		style.imageDisabled = PANEL_SKIN.getDrawable("button_exit_disabled");

		btnExit = AudibleButton.wrap(new ImageButton(style), PRESSED);
		btnExit.setPosition(getWidth()-21, getHeight()-36);
		btnExit.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				closePanel();
			}
		});

		addActor(btnExit);

		BUTTONS_ATLAS = getClient().getAssetManager().get(Assets.Textures.ASSET_BUTTONS);
		BUTTONS_SKIN = new Skin(Gdx.files.internal("textures/buttons.json"), BUTTONS_ATLAS);
		TextButtonStyle buttonStyle = BUTTONS_SKIN.get("tall", TextButtonStyle.class);
		buttonStyle.font = EXOCET;
		buttonStyle.checked = buttonStyle.down;
		buttonStyle.checkedOver = buttonStyle.checked;

		btnGeneral = AudibleButton.wrap(new TextButton("General", BUTTONS_SKIN, "tall"), PRESSED);
		final float xPos = 28+switchpanel.getRegionWidth()/2;
		float yPos = 28+switchpanel.getRegionHeight()-12-(btnGeneral.getHeight()/2);
		final float yAdjust = btnGeneral.getHeight()+2;
        Actors.centerAt(btnGeneral, xPos, yPos);
		yPos -= yAdjust;

		addActor(btnGeneral);

		btnGraphics = AudibleButton.wrap(new TextButton("Graphics", BUTTONS_SKIN, "tall"), PRESSED);
        Actors.centerAt(btnGraphics, xPos, yPos);
		yPos -= yAdjust;

		addActor(btnGraphics);

		btnAudio = AudibleButton.wrap(new TextButton("Audio", BUTTONS_SKIN, "tall"), PRESSED);
        Actors.centerAt(btnAudio, xPos, yPos);
		yPos -= yAdjust;

		addActor(btnAudio);

		switchboardButtons = new ButtonGroup();
		switchboardButtons.setMaxCheckCount(1);
		switchboardButtons.add(btnGeneral);
		switchboardButtons.add(btnGraphics);
		switchboardButtons.add(btnAudio);
	}

	@Override
	protected void drawChildren(Batch batch, float parentAlpha) {
		batch.draw(switchpanel, 28, 28);
		super.drawChildren(batch, parentAlpha);
	}

	@Override
	public void dispose() {
//		getClient().getAssetManager().unload(PANEL_PATH);
//		getClient().getAssetManager().unload(BUTTON_PRESSED_PATH);
//		getClient().getAssetManager().unload(EXOCET_PATH);
	}

	@Override
	public void render(float delta) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.BACK:
			case Input.Keys.ESCAPE:
				closePanel();
				break;
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		return true;
	}
}
