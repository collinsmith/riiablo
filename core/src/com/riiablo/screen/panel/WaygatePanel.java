package com.riiablo.screen.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Levels;
import com.riiablo.graphics.BlendMode;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.Button;
import com.riiablo.widget.IconTextButton;

import java.util.Comparator;

public class WaygatePanel extends WidgetGroup implements Disposable {
  private static final String TAG = "WaygatePanel";

  final AssetDescriptor<DC6> waygatebackgroundDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\waygatebackground.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion waygatebackground;

  final AssetDescriptor<DC6> expwaygatetabsDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\expwaygatetabs.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  DC expwaygatetabs;

  final AssetDescriptor<DC6> waygateiconsDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\waygateicons.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  DC waygateicons;
  Button.ButtonStyle waygateButtonStyle;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  Button btnExit;

  public WaygatePanel() {
    Riiablo.assets.load(waygatebackgroundDescriptor);
    Riiablo.assets.finishLoadingAsset(waygatebackgroundDescriptor);
    waygatebackground = Riiablo.assets.get(waygatebackgroundDescriptor).getTexture();
    setSize(waygatebackground.getRegionWidth(), waygatebackground.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    btnExit = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(buysellbtnDescriptor);
      Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(272, 14);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    Riiablo.assets.load(expwaygatetabsDescriptor);
    Riiablo.assets.finishLoadingAsset(expwaygatetabsDescriptor);
    expwaygatetabs = Riiablo.assets.get(expwaygatetabsDescriptor);

    Riiablo.assets.load(waygateiconsDescriptor);
    Riiablo.assets.finishLoadingAsset(waygateiconsDescriptor);
    waygateicons = Riiablo.assets.get(waygateiconsDescriptor);

    waygateButtonStyle = new Button.ButtonStyle() {{
      disabled = new TextureRegionDrawable(waygateicons.getTexture(0));
      up       = new TextureRegionDrawable(waygateicons.getTexture(3));
      down     = new TextureRegionDrawable(waygateicons.getTexture(4));
    }};

    @SuppressWarnings("unchecked")
    Array<Levels.Entry>[] waypoints = (Array<Levels.Entry>[]) new Array[5];
    for (int i = 0; i < waypoints.length; i++) waypoints[i] = new Array<>(9);
    for (Levels.Entry level : Riiablo.files.Levels) {
      if (level.Waypoint != 0xFF) {
        waypoints[level.Act].add(level);
      }
    }
    Comparator<Levels.Entry> comparator = new Comparator<Levels.Entry>() {
      @Override
      public int compare(Levels.Entry o1, Levels.Entry o2) {
        return o1.Waypoint - o2.Waypoint;
      }
    };
    for (Array<Levels.Entry> waypoint : waypoints) {
      waypoint.sort(comparator);
    }

    final Tab[] tabs = new Tab[5];
    for (int i = 0; i < tabs.length; i++) {
      Tab tab = tabs[i] = new Tab();
      for (Levels.Entry entry : waypoints[i]) {
        tab.addWaypoint(entry.LevelName);
      }

      tab.pack();
      tab.setWidth(290);
      tab.layout();
      //tab.setDebug(true, true);
      tab.setPosition(16, getHeight() - 58, Align.topLeft);
      tab.setVisible(false);
      addActor(tab);
    }

    float x = 2, y = getHeight() - 3;
    Button[] actors = new Button[5];
    for (int i = 0; i < actors.length; i++) {
      final int j = i << 1;
      final Button actor = actors[i] = new Button(new Button.ButtonStyle() {{
        down = new TextureRegionDrawable(expwaygatetabs.getTexture(j));
        up   = new TextureRegionDrawable(expwaygatetabs.getTexture(j + 1));
        checked = down;
      }});
      actor.setHighlightedBlendMode(BlendMode.ID, Color.WHITE);
      actor.setPosition(x, y, Align.topLeft);
      actor.setUserObject(tabs[i]);
      actor.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          for (Tab tab : tabs) if (tab != null) tab.setVisible(false);
          Tab tab = (Tab) actor.getUserObject();
          tab.setVisible(true);
        }
      });
      addActor(actor);
      x += actor.getWidth();
    }

    ButtonGroup<Button> tabGroup = new ButtonGroup<>();
    tabGroup.add(actors);
    tabGroup.setMinCheckCount(1);
    tabGroup.setMaxCheckCount(1);
    tabs[0].setVisible(true);

    //setDebug(true, true);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.draw(waygatebackground, getX(), getY());
    super.draw(batch, parentAlpha);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(waygatebackgroundDescriptor.fileName);
    Riiablo.assets.unload(waygateiconsDescriptor.fileName);
    Riiablo.assets.unload(expwaygatetabsDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  private class Tab extends Table {
    Tab() {
      columnDefaults(0).height(32).spaceBottom(4).growX();
    }

    void addWaypoint(String descId) {
      add(new IconTextButton(waygateButtonStyle, Riiablo.string.lookup(descId), Riiablo.fonts.font16)).row();
    }
  }
}
