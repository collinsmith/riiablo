package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.entity.Entity;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;

public class MobileControls extends WidgetGroup implements Disposable {
  final AssetDescriptor<DC6> SkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\Skillicon.DC6", DC6.class);
  DC6 Skillicon;

  final AssetDescriptor<DC6> SoSkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\SoSkillicon.DC6", DC6.class);
  DC6 SoSkillicon;

  GameScreen gameScreen;

  Button interact;
  Button teleport;
  Button foo1, foo2;

  final float SIZE = 64;

  public MobileControls(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    Riiablo.assets.load(SkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(SkilliconDescriptor);
    Skillicon = Riiablo.assets.get(SkilliconDescriptor);

    Riiablo.assets.load(SoSkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(SoSkilliconDescriptor);
    SoSkillicon = Riiablo.assets.get(SoSkilliconDescriptor);

    interact = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Skillicon.getTexture(8));
      down = new TextureRegionDrawable(Skillicon.getTexture(9));
    }});
    interact.setSize(SIZE, SIZE);
    interact.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (!gameScreen.labels.isEmpty()) {
          for (Actor label : gameScreen.labels) {
            Object obj = label.getUserObject();
            if (obj instanceof Entity) {
              Entity entity = (Entity) obj;
              if (entity.isSelectable()) entity.interact(gameScreen);
              break;
            }
          }
        }
      }
    });

    teleport = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(SoSkillicon.getTexture(36));
      down = new TextureRegionDrawable(SoSkillicon.getTexture(37));
    }});
    teleport.setSize(SIZE, SIZE);

    foo1 = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Skillicon.getTexture(0));
      down = new TextureRegionDrawable(Skillicon.getTexture(1));
    }});
    foo1.setSize(SIZE, SIZE);

    foo2 = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Skillicon.getTexture(0));
      down = new TextureRegionDrawable(Skillicon.getTexture(1));
    }});
    foo2.setSize(SIZE, SIZE);

    addActor(interact);
    addActor(teleport);
    addActor(foo1);
    addActor(foo2);

    setSize(256, 256);
    setTouchable(Touchable.childrenOnly);

    interact.setPosition(getWidth(), 0, Align.bottomRight);
    teleport.setPosition(getWidth(), 72, Align.bottomRight);
    foo1.setPosition(getWidth() - 72, 72, Align.bottomRight);
    foo2.setPosition(getWidth() - 72, 0, Align.bottomRight);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(SkilliconDescriptor.fileName);
    Riiablo.assets.unload(SoSkilliconDescriptor.fileName);
  }
}
