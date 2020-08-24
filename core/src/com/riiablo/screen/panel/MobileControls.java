package com.riiablo.screen.panel;

import net.mostlyoriginal.api.event.common.EventSystem;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.engine.client.event.InteractEvent;
import com.riiablo.engine.server.Actioneer;
import com.riiablo.widget.Button;
import com.riiablo.widget.HotkeyButton;

public class MobileControls extends WidgetGroup implements Disposable {
  final AssetDescriptor<DC6> SkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\Skillicon.DC6", DC6.class);
  DC6 Skillicon;

  final AssetDescriptor<DC6> SoSkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\SoSkillicon.DC6", DC6.class);
  DC6 SoSkillicon;

  protected EventSystem events;
  protected Actioneer actioneer;

  Button interact;
  HotkeyButton skills[];

  final float SIZE = 64;

  public MobileControls() {
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
        events.dispatch(InteractEvent.obtain());
      }
    });

    skills = new HotkeyButton[5];
    for (int i = 0; i < skills.length; i++) {
      skills[i] = new HotkeyButton(Skillicon, 0, -1);
      skills[i].setSize(SIZE, SIZE);
    }

    ActorGestureListener gestureListener = new ActorGestureListener() {
      private final Vector2 tmpVec2 = new Vector2();

      @Override
      public boolean longPress(Actor actor, float x, float y) {
        SpellsQuickPanel spellsQuickPanel = Riiablo.game.spellsQuickPanelR;
        final boolean visible = !spellsQuickPanel.isVisible();
        if (visible) {
          HotkeyButton dst = (HotkeyButton) actor;
          spellsQuickPanel.setObserver(dst);
          // FIXME: Scale this better
          tmpVec2.set(actor.getWidth(), actor.getHeight());
          actor.localToStageCoordinates(tmpVec2);
          spellsQuickPanel.setPosition(tmpVec2.x, tmpVec2.y, Align.bottomRight);
        }
        spellsQuickPanel.setVisible(visible);
        return true;
      }

      @Override
      public void tap(InputEvent event, float x, float y, int count, int button) {
        if (Riiablo.game.spellsQuickPanelR.isVisible()) {
          Riiablo.game.spellsQuickPanelR.setVisible(false);
          return;
        }

        HotkeyButton actor = (HotkeyButton) event.getListenerActor();
        final int skillId = actor.getSkill();
        if (skillId == -1) return;
        actioneer.cast(Riiablo.game.player, skillId, Vector2.Zero);
        // TODO: above target is placeholder
      }
    };
    gestureListener.getGestureDetector().setLongPressSeconds(0.5f);
    for (Actor button : skills) button.addListener(gestureListener);

    addActor(interact);
    for (Actor button : skills) addActor(button);

    setSize(256, 256);
    setTouchable(Touchable.childrenOnly);

    interact.setPosition(getWidth(), 0, Align.bottomRight);
    skills[0].setPosition(getWidth(), 72, Align.bottomRight);
    skills[1].setPosition(getWidth() - 72, 72, Align.bottomRight);
    skills[2].setPosition(getWidth() - 72, 0, Align.bottomRight);
    skills[3].setPosition(getWidth() - 144, 72, Align.bottomRight);
    skills[4].setPosition(getWidth() - 144, 0, Align.bottomRight);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(SkilliconDescriptor.fileName);
    Riiablo.assets.unload(SoSkilliconDescriptor.fileName);
  }
}
