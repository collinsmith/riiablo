package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.server.component.Position;
import com.riiablo.widget.NpcDialogBox;
import com.riiablo.widget.NpcMenu;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class DialogManager extends PassiveSystem {
  protected ComponentMapper<Position> mPosition;

  protected MenuManager menuManager;

  @Wire(name = "iso")
  protected IsometricCamera iso;

  @Wire(name = "scaledStage")
  protected Stage scaledStage;

  private NpcDialogBox dialog;

  private final Vector2 tmpVec2 = new Vector2();

  public NpcDialogBox getDialog() {
    return dialog;
  }

  public void setDialog(NpcDialogBox dialog) {
    if (this.dialog != dialog) {
      if (this.dialog != null) {
        this.dialog.remove();
        this.dialog.dispose();
        NpcMenu menu = menuManager.getMenu();
        if (menu != null) menu.setVisible(true);
      }

      this.dialog = dialog;
      if (dialog != null) {
        NpcMenu menu = menuManager.getMenu();
        if (menu != null) menu.setVisible(false);
        //dialog.setPosition(stage.getWidth() / 2, stage.getHeight(), Align.top | Align.center);
        tmpVec2.set(Gdx.graphics.getWidth() / 2, 0);
        scaledStage.screenToStageCoordinates(tmpVec2);
        dialog.setPosition(tmpVec2.x, tmpVec2.y, Align.top | Align.center);
        scaledStage.addActor(dialog);
      }
    }
  }
}
