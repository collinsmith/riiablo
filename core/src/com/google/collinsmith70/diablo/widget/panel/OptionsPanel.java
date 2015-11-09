package com.google.collinsmith70.diablo.widget.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.asset.Assets;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.lang.Langs;
import com.google.collinsmith70.diablo.widget.ActorUtils;
import com.google.collinsmith70.diablo.widget.ButtonUtils;

import java.util.Locale;

public class OptionsPanel extends AbstractPanel {

private static final AssetDescriptor<TextureAtlas> BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR
        = new AssetDescriptor<TextureAtlas>("textures/buttons.pack", TextureAtlas.class);
private TextureAtlas buttonsTextureAtlas;
private Skin buttonsSkin;

private Sound buttonSoundAsset;

private ButtonGroup<TextButton> tabsButtonGroup;
private TextButton btnTabGeneral;
private TextButton btnTabGraphics;
private TextButton btnTabSound;

private Slider soundVolumeSlider;

public OptionsPanel(Client client) {
    super(client);
    setModal(true);
}

@Override
public void loadAssets(AssetManager assetManager) {
    assetManager.load(BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR);
    assetManager.finishLoading();
}

@Override
public void disposeAssets(AssetManager assetManager) {
    assetManager.unload(BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR.fileName);
}

@Override
public void create() {
    super.create();

    AssetManager assetManager = getClient().getAssetManager();

    buttonSoundAsset = assetManager.get(Assets.Client.Sound.BUTTON);

    BitmapFont exocetBitmapFont = assetManager.get(Assets.Client.Font.Exocet._16);
    buttonsTextureAtlas = assetManager.get(BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR);
    buttonsSkin = new Skin(Gdx.files.internal("textures/buttons.json"), buttonsTextureAtlas);
    TextButton.TextButtonStyle textButtonStyle = buttonsSkin.get("tall", TextButton.TextButtonStyle.class);
    textButtonStyle.font = exocetBitmapFont;
    textButtonStyle.checked = textButtonStyle.down;
    textButtonStyle.checkedOver = textButtonStyle.checked;

    final float xPos = getClient().getVirtualWidth()/4;
    float yPos = getClient().getVirtualHeight()/2;

    btnTabGeneral = new TextButton(
            Langs.OptionsPanel.tab_general,
            buttonsSkin, "tall");
    ButtonUtils.playSoundOnClicked(btnTabGeneral, buttonSoundAsset);
    ActorUtils.centerAt(btnTabGeneral, xPos, yPos);
    addActor(btnTabGeneral);
    final float yAdjust = btnTabGeneral.getHeight()+2;
    yPos += yAdjust;

    btnTabGraphics = new TextButton(
            Langs.OptionsPanel.tab_graphics,
            buttonsSkin, "tall");
    ButtonUtils.playSoundOnClicked(btnTabGraphics, buttonSoundAsset);
    ActorUtils.centerAt(btnTabGraphics, xPos, yPos);
    addActor(btnTabGraphics);
    yPos += yAdjust;

    btnTabSound = new TextButton(
            Langs.OptionsPanel.tab_sound,
            buttonsSkin, "tall");
    ButtonUtils.playSoundOnClicked(btnTabSound, buttonSoundAsset);
    ActorUtils.centerAt(btnTabSound, xPos, yPos);
    addActor(btnTabSound);
    yPos += yAdjust;

    tabsButtonGroup = new ButtonGroup<TextButton>(btnTabGeneral, btnTabGraphics, btnTabSound);
    tabsButtonGroup.setMaxCheckCount(1);
    tabsButtonGroup.setChecked(Langs.OptionsPanel.tab_general);

    Cvars.Client.Locale.addCvarChangeListener(new CvarChangeListener<Locale>() {
        @Override
        public void onCvarChanged(Cvar<Locale> cvar, Locale fromValue, Locale toValue) {
            FileHandle i18nBundleFileHandle = Gdx.files.internal("lang/OptionsPanel");
            I18NBundle i18nBundle = I18NBundle.createBundle(i18nBundleFileHandle, toValue);

            OptionsPanel.this.btnTabGeneral.setText(i18nBundle.get(Langs.OptionsPanel.tab_general));
            OptionsPanel.this.btnTabGraphics.setText(i18nBundle.get(Langs.OptionsPanel.tab_graphics));
            OptionsPanel.this.btnTabSound.setText(i18nBundle.get(Langs.OptionsPanel.tab_sound));
        }
    });

    Skin sliderSkin = new Skin();
    sliderSkin.add("background", new Texture(Gdx.files.internal("textures/sliderBackground.png")));
    sliderSkin.add("knob", new Texture(Gdx.files.internal("textures/sliderSelector.png")));
    Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
    sliderStyle.background = sliderSkin.getDrawable("background");
    sliderStyle.knob = sliderSkin.getDrawable("knob");

    soundVolumeSlider = new Slider(0.0f, 1.0f, 0.01f, false, sliderStyle);
    soundVolumeSlider.setWidth(290.0f);
    soundVolumeSlider.setValue(Cvars.Client.Sound.Music.Volume.getValue());
    soundVolumeSlider.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            Cvars.Client.Sound.Music.Volume.setValue(soundVolumeSlider.getValue());
        }
    });
    ActorUtils.centerAt(soundVolumeSlider, getClient().getVirtualWidth()/2, getClient().getVirtualHeight()/2);
    addActor(soundVolumeSlider);

}

@Override
public boolean keyDown(int keycode) {
    switch(keycode) {
        case Input.Keys.BACK:
        case Input.Keys.ESCAPE:
            dispose();
            return true;
        default:
            return false;
    }
}

}
