package com.riiablo.engine.client;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.engine.server.event.ZoneChangeEvent;
import com.riiablo.graphics.BlendMode;
import com.riiablo.loader.DC6Loader;
import com.riiablo.map.Map;
import com.riiablo.widget.DCWrapper;

import net.mostlyoriginal.api.event.common.Subscribe;

public class ZoneEntryDisplayer extends BaseSystem {
  @Wire(name = "stage")
  protected Stage stage;

  private static final String[] ACT_NAME = {"act1", "act2", "act3", "act4", "expansion"};

  private boolean loaded;
  private AssetDescriptor<DC6> entryDescriptor;
  private DCWrapper entryImage;

  @Subscribe
  public void onZoneChanged(ZoneChangeEvent event) {
    if (event.entityId != Riiablo.game.player) return;
    displayEntry(event.zone);
  }

  @Override
  protected void initialize() {
    entryImage = new DCWrapper();
    entryImage.setScaling(Scaling.none);
    entryImage.setAlign(Align.center);
    entryImage.setBlendMode(BlendMode.DARKEN);
    entryImage.setColor(Riiablo.colors.darkenRed);
    entryImage.setVisible(false);
    stage.addActor(entryImage);
  }

  @Override
  protected void processSystem() {
    if (loaded || entryDescriptor == null || !Riiablo.assets.isLoaded(entryDescriptor)) return;
    loaded = true;

    entryImage.setDrawable(Riiablo.assets.get(entryDescriptor));
    entryImage.setPosition(stage.getWidth() / 2, stage.getHeight() * 0.8f, Align.center);
    entryImage.clearActions();
    entryImage.addAction(Actions.sequence(
        Actions.show(),
        Actions.alpha(1),
        Actions.delay(4, Actions.fadeOut(1, Interpolation.pow2In)),
        Actions.hide()));
  }

  private void displayEntry(Map.Zone zone) {
    if (entryDescriptor != null) Riiablo.assets.unload(entryDescriptor.fileName);
    if (entryImage != null) {
      entryImage.clearActions();
      entryImage.setVisible(false);
    }
    loaded = false;

    // TODO: i18n? Not sure if these have translations.
    String entryFile = zone.level.Id == 8 ? "A1Q1" : zone.level.EntryFile;
    entryDescriptor = new AssetDescriptor<>("data\\local\\ui\\eng\\" + ACT_NAME[zone.map.getAct()] + "\\" + entryFile + ".dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
    Riiablo.assets.load(entryDescriptor);
  }
}
