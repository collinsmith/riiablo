package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.collinsmith70.diablo.Assets;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.EffectivelyFinal;
import com.google.collinsmith70.diablo.util.Actors;
import com.google.collinsmith70.diablo.widget.AnimationActor;
import com.google.collinsmith70.diablo.widget.AudibleButton;

public class HomeScene extends BackgroundedScene {
	@EffectivelyFinal
	private Texture BACKGROUND_TEX;

	@EffectivelyFinal
	private TextureAtlas EXOCET_ATLAS;
	@EffectivelyFinal
	private BitmapFont EXOCET;

	@EffectivelyFinal
	private Music INTRO;
	@EffectivelyFinal
	private Sound PRESSED;
	@EffectivelyFinal
	private AnimationActor animMainTitle;

//	public static final String BUTTONS_PATH = "textures/buttons.pack";
	@EffectivelyFinal
	private TextureAtlas BUTTONS_ATLAS;
	@EffectivelyFinal
	private Skin BUTTONS_SKIN;
	@EffectivelyFinal
	private TextButton btnPlay;
	@EffectivelyFinal
	private TextButton btnOptions;
	@EffectivelyFinal
	private TextButton btnExit;

	@Override
	public void loadAssets() {
		getClient().getAssetManager().load(Assets.Textures.Backgrounds.ASSET_HOME);
//		getClient().getAssetManager().load(INTRO_PATH, Music.class);
//		getClient().getAssetManager().load(EXOCET_PATH, TextureAtlas.class);
//		getClient().getAssetManager().load(BUTTONS_PATH, TextureAtlas.class);
//		getClient().getAssetManager().load(MAINTITLE_ANIM_PATH, Texture.class);
//		getClient().getAssetManager().load(BUTTON_PRESSED_PATH, Sound.class);
		getClient().getAssetManager().finishLoading();
	}

	public HomeScene(Client client, Music intro, AnimationActor animMainTitle) {
		super(client);
		this.INTRO = intro;
		this.animMainTitle = animMainTitle;
	}

	@Override
	public void show() {
		this.BACKGROUND_TEX = getClient().getAssetManager().get(Assets.Textures.Backgrounds.ASSET_HOME);
		setBackground(new TextureRegion(BACKGROUND_TEX));

		if (INTRO != null) {
			INTRO = getClient().getAssetManager().get(Assets.Audio.Musics.ASSET_INTRO);
			INTRO.setOnCompletionListener(new Music.OnCompletionListener() {
				@Override
				public void onCompletion(Music music) {
					getClient().getAssetManager().unload(Assets.Audio.Musics.INTRO.toString());
					music.dispose();
				}
			});

			INTRO.play();
		}

		if (animMainTitle != null) {
			Texture MAINTITLE_ANIM_TEX = getClient().getAssetManager().get(Assets.Animations.ASSET_LOGO);
			animMainTitle = new AnimationActor(MAINTITLE_ANIM_TEX, 30, 374, 170, 1/30f);
			animMainTitle.setScale(getClient().getVirtualWidth()/1280f, getClient().getVirtualHeight()/720f);
			animMainTitle.setPosition(
				(getClient().getVirtualWidth()-animMainTitle.getWidth()*animMainTitle.getScaleX())/2,
				getClient().getVirtualHeight()-animMainTitle.getHeight()*animMainTitle.getScaleY()
			);
		}

		PRESSED = getClient().getAssetManager().get(Assets.Audio.Cursor.ASSET_BUTTON);

		EXOCET = getClient().getAssetManager().get(Assets.Fonts.ASSET_EXOCET16);
		BUTTONS_ATLAS = getClient().getAssetManager().get(Assets.Textures.ASSET_BUTTONS);
		BUTTONS_SKIN = new Skin(Gdx.files.internal("textures/buttons.json"), BUTTONS_ATLAS);
		TextButton.TextButtonStyle style = BUTTONS_SKIN.get("wide", TextButton.TextButtonStyle.class);
		style.font = EXOCET;

		final float xPos = getClient().getVirtualWidth()/2;
		float yPos = getClient().getVirtualHeight()/4;

		btnExit = AudibleButton.wrap(new TextButton("Exit Diablo in Java", BUTTONS_SKIN, "wide"), PRESSED);
		final float yAdjust = btnExit.getHeight()+2;
		Actors.centerAt(btnExit, xPos, yPos);
		yPos += yAdjust;

		btnExit.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				closeApplication();
			}
		});

		btnOptions = AudibleButton.wrap(new TextButton("Options", BUTTONS_SKIN, "wide"), PRESSED);
		Actors.centerAt(btnOptions, xPos, yPos);
		yPos += yAdjust;

		btnPlay = AudibleButton.wrap(new TextButton("Single Player", BUTTONS_SKIN, "wide"), PRESSED);
		Actors.centerAt(btnPlay, xPos, yPos);
		yPos += yAdjust;

		btnPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
			}
		});

		addActor(btnPlay);
		addActor(btnOptions);
		addActor(btnExit);
		addActor(animMainTitle);
	}

	private void closeApplication() {
		Gdx.app.exit();
	}

	@Override
	public void dispose() {
		try {
		getClient().getAssetManager().unload(Assets.Textures.Backgrounds.HOME.toString());
//		getClient().getAssetManager().unload(EXOCET_PATH);
//		getClient().getAssetManager().unload(BUTTONS_PATH);
//		getClient().getAssetManager().unload(MAINTITLE_ANIM_PATH);
//		getClient().getAssetManager().unload(BUTTON_PRESSED_PATH);
//		getClient().getAssetManager().unload(PRESSED_PATH);
		} catch (GdxRuntimeException e) {
		}
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
				closeApplication();
				break;
		}

		return true;
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
