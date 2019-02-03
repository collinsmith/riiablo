package gdx.diablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.screen.MenuScreen;

public class EscapePanel extends WidgetGroup implements Disposable {

  final AssetDescriptor<DC6> optionsDescriptor = new AssetDescriptor<>("data\\local\\ui\\eng\\options.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  final AssetDescriptor<DC6> exitDescriptor = new AssetDescriptor<>("data\\local\\ui\\eng\\exit.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  final AssetDescriptor<DC6> returntogameDescriptor = new AssetDescriptor<>("data\\local\\ui\\eng\\returntogame.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  EscapeButton options;
  EscapeButton exit;
  EscapeButton returntogame;

  final AssetDescriptor<DC6> pentspinDescriptor = new AssetDescriptor<>("data\\global\\ui\\CURSOR\\pentspin.DC6", DC6.class);
  Animation pentspin;
  FocusActor[] focusActor;


  public EscapePanel() {
    Diablo.assets.load(optionsDescriptor);
    Diablo.assets.finishLoadingAsset(optionsDescriptor);
    options = new EscapeButton(Diablo.assets.get(optionsDescriptor).getTexture(0));

    Diablo.assets.load(exitDescriptor);
    Diablo.assets.finishLoadingAsset(exitDescriptor);
    exit = new EscapeButton(Diablo.assets.get(exitDescriptor).getTexture(0));
    exit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Diablo.client.clearAndSet(new MenuScreen());
      }
    });

    Diablo.assets.load(returntogameDescriptor);
    Diablo.assets.finishLoadingAsset(returntogameDescriptor);
    returntogame = new EscapeButton(Diablo.assets.get(returntogameDescriptor).getTexture(0));
    returntogame.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });

    Diablo.assets.load(pentspinDescriptor);
    Diablo.assets.finishLoadingAsset(pentspinDescriptor);
    pentspin = Animation.newAnimation(Diablo.assets.get(pentspinDescriptor));
    focusActor = new FocusActor[6];
    for (int i = 0; i < focusActor.length; i++) {
      focusActor[i] = new FocusActor(pentspin);
    }
    ClickListener focusListener = new ClickListener() {
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        setVisible(event.getListenerActor(), true);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        setVisible(event.getListenerActor(), false);
      }

      void setVisible(Actor actor, boolean b) {
        if (actor == options) {
          focusActor[0].setVisible(b);
          focusActor[1].setVisible(b);
        } else if (actor == exit) {
          focusActor[2].setVisible(b);
          focusActor[3].setVisible(b);
        } else if (actor == returntogame) {
          focusActor[4].setVisible(b);
          focusActor[5].setVisible(b);
        }
      }
    };
    options.addListener(focusListener);
    exit.addListener(focusListener);
    returntogame.addListener(focusListener);

    final int spacing = 24;
    Table table = new Table();
    table.align(Align.center);
    table.add(focusActor[0]);
    table.add(options).fillX();
    table.add(focusActor[1]).row();
    table.add(focusActor[2]);
    table.add(exit).fillX();
    table.add(focusActor[3]).row();
    table.add(focusActor[4]);
    table.add(returntogame).fillX();
    table.add(focusActor[5]).row();

    table.setFillParent(true);
    addActor(table);

    setFillParent(true);
    setVisible(false);
    setTouchable(Touchable.childrenOnly);
    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    Diablo.assets.unload(optionsDescriptor.fileName);
    Diablo.assets.unload(exitDescriptor.fileName);
    Diablo.assets.unload(returntogameDescriptor.fileName);
    Diablo.assets.unload(pentspinDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    pentspin.act();
    super.draw(batch, a);
  }

  private static class EscapeButton extends Button {
    EscapeButton(TextureRegion region) {
      super(new TextureRegionDrawable(region));
    }

    @Override
    protected void drawBackground(Batch batch, float a, float x, float y) {
      Drawable background = getBackground();
      background.draw(batch,
          x + (getWidth() / 2) - (background.getMinWidth() / 2),
          getY(),
          background.getMinWidth(),
          background.getMinHeight());
    }
  }

  private static class FocusActor extends Actor {
    Animation pentspin;
    FocusActor(Animation pentspin) {
      this.pentspin = pentspin;
      setSize(pentspin.getMinWidth(), pentspin.getMinHeight());
      setVisible(false);
    }

    @Override
    public void draw(Batch batch, float a) {
      pentspin.draw(batch, getX(), getY());
    }
  }
}
