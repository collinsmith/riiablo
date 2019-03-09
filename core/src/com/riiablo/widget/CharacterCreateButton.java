package com.riiablo.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.codec.util.BBox;

public class CharacterCreateButton extends Widget implements Disposable {
  private static final String TAG = "CharButton";

  public enum State { NU1, FW, NU3, BW }

  private final AssetDescriptor<DC6> bwDesc;
  private final AssetDescriptor<DC6> bwsDesc;
  private final AssetDescriptor<DC6> fwDesc;
  private final AssetDescriptor<DC6> fwsDesc;
  private final AssetDescriptor<DC6> nu1Desc;
  private final AssetDescriptor<DC6> nu2Desc;
  private final AssetDescriptor<DC6> nu3Desc;
  private final AssetDescriptor<DC6> nu3sDesc;

  private final AssetDescriptor<Sound> selectDesc;
  private final AssetDescriptor<Sound> deselectDesc;

  public final CharacterClass charClass;
  private State state = State.NU1;
  private float x, y;

  private Animation active;
  private Animation nu1; // state 0
  private Animation fw;  // state 1
  private Animation bw;  // state 3
  private Animation nu2; // state 0
  private Animation nu3; // state 2

  private Sound select;
  private Sound deselect;

  private final ClickListener clickListener;

  public CharacterCreateButton(final CharacterClass charClass) {
    this.charClass = charClass;
    bwDesc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "bw.dc6", DC6.class);
    bwsDesc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "bws.dc6", DC6.class);
    fwDesc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "fw.dc6", DC6.class);
    fwsDesc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "fws.dc6", DC6.class);
    nu1Desc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "nu1.dc6", DC6.class);
    nu2Desc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "nu2.dc6", DC6.class);
    nu3Desc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "nu3.dc6", DC6.class);
    nu3sDesc = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\" + charClass + "\\" + charClass.shortName + "nu3s.dc6", DC6.class);

    selectDesc = new AssetDescriptor<>("data\\global\\sfx\\cursor\\intro\\" + charClass + " select.wav", Sound.class);
    deselectDesc = new AssetDescriptor<>("data\\global\\sfx\\cursor\\intro\\" + charClass + " deselect.wav", Sound.class);

    Riiablo.assets.load(nu1Desc);

    addListener(clickListener = new ClickListener(Input.Buttons.LEFT) {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        switch (state) {
          case NU1:
            select();
            break;
          case NU3:
            deselect();
            break;
          default:
            // do nothing
        }
      }

      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (state == State.NU1) {
          if (nu2 == null) {
            Riiablo.assets.finishLoadingAsset(nu2Desc);
            nu2 = Animation.newAnimation(Riiablo.assets.get(nu2Desc));
            nu2.setFrameDuration(1 / 16f);
          }
          setActive(nu2);
          nu2.setFrame(nu1.getFrame());
        }
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (state == State.NU1) {
          assert nu1 != null;
          setActive(nu1);
          nu1.setFrame(nu2.getFrame());
        }
      }
    });

    Riiablo.assets.finishLoadingAsset(nu1Desc);
    nu1 = Animation.newAnimation(Riiablo.assets.get(nu1Desc));
    nu1.setFrameDuration(1 / 16f);
    setActive(nu1);
  }

  public void loadRemainingAssets() {
    Riiablo.assets.load(bwDesc);
    Riiablo.assets.load(fwDesc);
    Riiablo.assets.load(nu2Desc);
    Riiablo.assets.load(nu3Desc);
    if (charClass.bws)  Riiablo.assets.load(bwsDesc);
    if (charClass.fws)  Riiablo.assets.load(fwsDesc);
    if (charClass.nu3s) Riiablo.assets.load(nu3sDesc);

    Riiablo.assets.load(selectDesc);
    Riiablo.assets.load(deselectDesc);
  }

  @Override
  public void dispose() {
    if (select != null) select.stop();
    if (deselect != null) deselect.stop();

    Riiablo.assets.unload(bwDesc.fileName);
    Riiablo.assets.unload(fwDesc.fileName);
    Riiablo.assets.unload(nu1Desc.fileName);
    Riiablo.assets.unload(nu2Desc.fileName);
    Riiablo.assets.unload(nu3Desc.fileName);
    if (charClass.bws)  Riiablo.assets.unload(bwsDesc.fileName);
    if (charClass.fws)  Riiablo.assets.unload(fwsDesc.fileName);
    if (charClass.nu3s) Riiablo.assets.unload(nu3sDesc.fileName);

    Riiablo.assets.unload(selectDesc.fileName);
    Riiablo.assets.unload(deselectDesc.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    active.act();
    BBox offset = active.getBox();
    active.draw(batch, getX() - offset.xMin, getY() + offset.yMax);
  }

  @Override
  public void drawDebug(ShapeRenderer shapes) {
    if (state == State.NU1 || state == State.NU3) {
      super.drawDebug(shapes);
    }
  }

  private void setActive(Animation animation) {
    if (active != animation) {
      active = animation;
      animation.setFrame(0);
      BBox offset = animation.getBox();
      setSize(offset.width, offset.height);
      super.setPosition(x + offset.xMin, y - offset.yMax);
    }
  }

  @Override
  public void setPosition(float x, float y) {
    this.x = x;
    this.y = y;
    BBox offset = active.getBox();
    super.setPosition(x + offset.xMin, y - offset.yMax);
  }

  public boolean isSelected() {
    return state == State.NU3;
  }

  public boolean isAnimating() {
    return state == State.FW || state == State.BW;
  }

  public State getState() {
    return state;
  }

  public void select() {
    state = State.FW;
    if (fw == null) {
      Riiablo.assets.finishLoadingAsset(fwDesc);
      Animation.Builder builder = Animation.builder()
          .layer(Riiablo.assets.get(fwDesc));
      if (charClass.fws) {
        Riiablo.assets.finishLoadingAsset(fwsDesc);
        builder.layer(Riiablo.assets.get(fwsDesc), charClass.blendSpecial);
      }
      fw = builder.build();
      fw.setLooping(false);
      fw.addAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onFinished(Animation fw) {
          state = State.NU3;
          if (nu3 == null) {
            Riiablo.assets.finishLoadingAsset(nu3Desc);
            Animation.Builder builder = Animation.builder()
                .layer(Riiablo.assets.get(nu3Desc));
            if (charClass.nu3s) {
              Riiablo.assets.finishLoadingAsset(nu3sDesc);
              builder.layer(Riiablo.assets.get(nu3sDesc), charClass.blendSpecial);
            }
            nu3 = builder.build();
          }
          setActive(nu3);
        }
      });
    }
    setActive(fw);
    if (deselect != null) deselect.stop();
    if (select == null) {
      Riiablo.assets.finishLoadingAsset(selectDesc);
      select = Riiablo.assets.get(selectDesc);
    }
    select.play();
    CharacterCreateButton.super.toFront();
  }

  public void deselect() {
    state = State.BW;
    if (bw == null) {
      Riiablo.assets.finishLoadingAsset(bwDesc);
      Animation.Builder builder = Animation.builder()
          .layer(Riiablo.assets.get(bwDesc));
      if (charClass.bws) {
        Riiablo.assets.finishLoadingAsset(bwsDesc);
        builder.layer(Riiablo.assets.get(bwsDesc), charClass.blendSpecial);
      }
      bw = builder.build();
      bw.setLooping(false);
      bw.addAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onFinished(Animation bw) {
          state = State.NU1;
          if (clickListener.isOver()) {
            assert nu2 != null;
            setActive(nu2);
          } else {
            setActive(nu1);
          }
        }
      });
    }
    setActive(bw);
    select.stop();
    if (deselect == null) {
      Riiablo.assets.finishLoadingAsset(deselectDesc);
      deselect = Riiablo.assets.get(deselectDesc);
    }
    deselect.play();
  }
}
