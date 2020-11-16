package com.riiablo.engine.client;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.component.Label;
import com.riiablo.engine.server.component.MenuWrapper;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.event.NpcInteractionEvent;
import com.riiablo.widget.NpcMenu;

import net.mostlyoriginal.api.event.common.Subscribe;

public class MenuManager extends BaseSystem {
  protected ComponentMapper<Label> mLabel;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<MenuWrapper> mMenuWrapper;

  @Wire(name = "iso")
  protected IsometricCamera iso;

  @Wire(name = "scaledStage")
  protected Stage scaledStage;

  private NpcMenu menu;

  private final Vector2 tmpVec2 = new Vector2();

  @Subscribe
  public void onNpcInteraction(NpcInteractionEvent event) {
    MenuWrapper menuWrapper = mMenuWrapper.get(event.npcId);
    if (menuWrapper == null) return;
    setMenu(menuWrapper.menu, event.npcId);
  }

  @Override
  protected void processSystem() {}

  public NpcMenu getMenu() {
    return menu;
  }

  // TODO: notify menu open/close to set AI for owner to not move
  public void setMenu(NpcMenu menu, int owner) {
    if (this.menu == menu) return;
    if (this.menu != null) {
      // FIXME: Validate that cancel is only called if upnav, downnav -- looks good at a glance
      if (menu == null || menu.getParent() != this.menu) {
        NpcMenu parent = this.menu;
        do parent.cancel(); while ((parent = parent.getParent()) != menu);
      }
      scaledStage.getRoot().removeActor(this.menu);
    }

    this.menu = menu;
    if (menu != null && owner != Engine.INVALID_ENTITY) {
      scaledStage.addActor(menu);

      Label label = mLabel.get(owner);
      Vector2 position = mPosition.get(owner).position;
      iso.toScreen(tmpVec2.set(position));
      tmpVec2.add(label.offset);
      iso.project(tmpVec2);
      tmpVec2.y = iso.viewportHeight - tmpVec2.y; // stage coords expect y-down coords
      scaledStage.screenToStageCoordinates(tmpVec2);
      menu.setPosition(tmpVec2.x, tmpVec2.y, Align.center | Align.bottom);
    }
  }
}
