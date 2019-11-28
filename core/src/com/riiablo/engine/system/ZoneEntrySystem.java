package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.ZoneUpdate;
import com.riiablo.graphics.BlendMode;
import com.riiablo.loader.DC6Loader;
import com.riiablo.map.Map;
import com.riiablo.map.RenderSystem;
import com.riiablo.widget.DCWrapper;

public class ZoneEntrySystem extends EntitySystem {
  private final ComponentMapper<ZoneUpdate> zoneUpdateComponent = ComponentMapper.getFor(ZoneUpdate.class);
  private final ComponentMapper<MapComponent> mapComponent = ComponentMapper.getFor(MapComponent.class);

  private final RenderSystem renderer;
  private final Stage stage;

  boolean loaded;
  AssetDescriptor<DC6> entryDescriptor;
  final String[] ACT_NAME = { "act1", "act2", "act3", "act4", "expansion" };
  DCWrapper entryImage;

  public ZoneEntrySystem(RenderSystem renderer, Stage stage) {
    this.renderer = renderer;
    this.stage = stage;
  }

  @Override
  public void update(float delta) {
    Entity src = renderer.getSrc();
    if (zoneUpdateComponent.has(src)) {
      MapComponent mapComponent = this.mapComponent.get(src);
      assert mapComponent != null;
      displayEntry(mapComponent.map, mapComponent.zone);
    }

    if (!loaded && entryDescriptor != null && Riiablo.assets.isLoaded(entryDescriptor)) {
      loaded = true;
      if (entryImage == null) {
        entryImage = new DCWrapper();
        entryImage.setScaling(Scaling.none);
        entryImage.setAlign(Align.center);
        entryImage.setBlendMode(BlendMode.DARKEN);
        entryImage.setColor(Riiablo.colors.darkenRed);
        stage.addActor(entryImage);
      }

      entryImage.setDrawable(Riiablo.assets.get(entryDescriptor));
      entryImage.setPosition(stage.getWidth() / 2, stage.getHeight() * 0.8f, Align.center);
      entryImage.clearActions();
      entryImage.addAction(Actions.sequence(
          Actions.show(),
          Actions.alpha(1),
          Actions.delay(4, Actions.fadeOut(1, Interpolation.pow2In)),
          Actions.hide()));
    }
  }

  private void displayEntry(Map map, Map.Zone zone) {
    if (entryDescriptor != null) Riiablo.assets.unload(entryDescriptor.fileName);
    if (entryImage != null) {
      entryImage.clearActions();
      entryImage.setVisible(false);
    }
    loaded = false;

    // TODO: i18n? Not sure if these have translations.
    String entryFile = zone.level.Id == 8 ? "A1Q1" : zone.level.EntryFile;
    entryDescriptor = new AssetDescriptor<>("data\\local\\ui\\eng\\" + ACT_NAME[map.getAct()] + "\\" + entryFile + ".dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
    Riiablo.assets.load(entryDescriptor);
  }
}
