package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;

import com.google.collinsmith70.diablo.Assets;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.EffectivelyFinal;
import com.google.collinsmith70.diablo.util.Actors;
import com.google.collinsmith70.diablo.widget.AnimationActor;
import com.google.collinsmith70.diablo.widget.StrobingLabel;

public class SplashScene extends BackgroundedScene {
	@EffectivelyFinal
	private Texture BACKGROUND_TEX;

	@EffectivelyFinal
	private Music INTRO;
	
	@EffectivelyFinal
	private TextureAtlas EXOCET_ATLAS;
	@EffectivelyFinal
	private BitmapFont EXOCET;

	@EffectivelyFinal
	private Texture MAINTITLE_ANIM_TEX;
	@EffectivelyFinal
	private AnimationActor animMainTitle;

	@EffectivelyFinal
	private Sound PRESSED;

	@EffectivelyFinal
	private StrobingLabel lblPressToContinue;
	private boolean continued;
	
	@Override
	public void loadAssets() {
        AssetManager assetManager = getClient().getAssetManager();
        assetManager.load(Assets.Textures.Backgrounds.ASSET_SPLASH);
        assetManager.load(Assets.Audio.Musics.ASSET_INTRO);
        assetManager.load(Assets.Fonts.ASSET_EXOCET16);
        assetManager.load(Assets.Animations.ASSET_LOGO);
        assetManager.load(Assets.Audio.Cursor.ASSET_SELECT);

        assetManager.load(Assets.Audio.Musics.ASSET_INTRO);
        assetManager.load(Assets.Fonts.ASSET_EXOCET16);
        assetManager.load(Assets.Textures.ASSET_BUTTONS);
        assetManager.load(Assets.Animations.ASSET_LOGO);
        assetManager.load(Assets.Audio.Cursor.ASSET_BUTTON);

        assetManager.load(Assets.Textures.ASSET_OPTIONS_PANEL);
        assetManager.finishLoading();
	}

	public SplashScene(Client client) {
		super(client);
	}

	@Override
	public void show() {
		this.BACKGROUND_TEX = getClient().getAssetManager().get(Assets.Textures.Backgrounds.ASSET_SPLASH);
		setBackground(new TextureRegion(BACKGROUND_TEX));

		INTRO = getClient().getAssetManager().get(Assets.Audio.Musics.ASSET_INTRO);
		INTRO.setOnCompletionListener(new Music.OnCompletionListener() {
			@Override
			public void onCompletion(Music music) {
				getClient().getAssetManager().unload(Assets.Audio.Musics.INTRO.toString());
				music.dispose();
			}
		});

		INTRO.play();

		String press_to_continue;
		I18NBundle bundle = I18NBundle.createBundle(Gdx.files.internal("lang/splash"), getClient().getSettingManager().getLocale());
		if (getClient().getSettingManager().isUsingController()) {
			press_to_continue = bundle.format("press_any_button");
			getClient().getSettingManager().getController().addListener(new ControllerAdapter() {
				@Override
				public boolean buttonDown(Controller controller, int buttonIndex) {
					doPress();
					return false;
				}
			});
		} else {
			if (Gdx.app.getType() == Application.ApplicationType.Android) {
				press_to_continue = bundle.format("touch_the_screen");
			} else {
				press_to_continue = bundle.format("press_any_key");
			}
		}

		EXOCET = getClient().getAssetManager().get(Assets.Fonts.ASSET_EXOCET16);
		LabelStyle labelStyle = new LabelStyle(EXOCET, Color.WHITE);
		lblPressToContinue = new StrobingLabel(press_to_continue, labelStyle, StrobingLabel.DEFAULT_STROBE_FLOOR, StrobingLabel.DEFAULT_STROBE_CEILING, StrobingLabel.DEFAULT_STROBE_MOD);
        Actors.centerAt(lblPressToContinue,
                getClient().getVirtualWidth() / 2,
                getClient().getVirtualHeight() / 4
        );

		MAINTITLE_ANIM_TEX = getClient().getAssetManager().get(Assets.Animations.ASSET_LOGO);
		animMainTitle = new AnimationActor(MAINTITLE_ANIM_TEX, 30, 374, 170, 1/30f);
		animMainTitle.setScale(getClient().getVirtualWidth() / 1280f, getClient().getVirtualHeight() / 720f);
		animMainTitle.setPosition(
                (getClient().getVirtualWidth() - animMainTitle.getWidth() * animMainTitle.getScaleX()) / 2,
                getClient().getVirtualHeight() - animMainTitle.getHeight() * animMainTitle.getScaleY()
        );

		PRESSED = getClient().getAssetManager().get(Assets.Audio.Cursor.ASSET_SELECT);

		addActor(lblPressToContinue);
		addActor(animMainTitle);
	}

	@Override
	public boolean keyDown(int keycode) {
		//if (getClient().getSettingManager().isUsingController()) {
		//	return true;
		//}

		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			return true;
		}

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //if (getClient().getSettingManager().isUsingController()) {
        //	return true;
        //}

		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			if (doPress()) {
				Gdx.input.vibrate(25);
			}

			return false;
		} else {
			doPress();
			return false;
		}
	}

	private boolean doPress() {
		if (continued) {
			return false;
		}

		PRESSED.play();
		continued = true;
		lblPressToContinue.setVisible(false);
		getClient().setScene(new HomeScene(getClient(), INTRO, animMainTitle));
		return true;
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
	public void dispose() {
		try {
		getClient().getAssetManager().unload(Assets.Textures.Backgrounds.SPLASH.toString());
		getClient().getAssetManager().unload(Assets.Fonts.EXOCET16.toString());
		getClient().getAssetManager().unload(Assets.Animations.LOGO.toString());
		//getClient().getAssetManager().unload(PRESSED_PATH);
		} catch (GdxRuntimeException e) {
		}
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
