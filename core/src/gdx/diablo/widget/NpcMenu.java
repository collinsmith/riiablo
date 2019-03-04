package gdx.diablo.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.diablo.Diablo;
import gdx.diablo.entity.Entity;
import gdx.diablo.graphics.BorderedPaletteIndexedDrawable;
import gdx.diablo.screen.GameScreen;

public class NpcMenu extends Table {

  private static final float PADDING = 8;
  private static final float SPACING = 2;

  Entity     owner;
  NpcMenu    parent;
  GameScreen gameScreen;

  CancellationListener cancellationListener;

  public NpcMenu(Entity owner, GameScreen gameScreen, String header) {
    parent = null;
    this.owner = owner;
    this.gameScreen = gameScreen;
    setBackground(new BorderedPaletteIndexedDrawable());
    pad(PADDING);
    add(new Label(header, Diablo.fonts.font16) {{
      setColor(Diablo.colors.gold);
    }}).space(SPACING).row();
  }

  public NpcMenu(int id) {
    setBackground(new BorderedPaletteIndexedDrawable());
    pad(PADDING);
    add(new Label(id, Diablo.fonts.font16, Diablo.colors.gold)).space(SPACING).row();
  }

  public boolean hasParent() {
    return parent != null;
  }

  public NpcMenu getParent() {
    return parent;
  }

  public NpcMenu addItem(int id, ClickListener clickListener) {
    LabelButton button = new LabelButton(id, Diablo.fonts.font16);
    button.addListener(clickListener);
    add(button).space(SPACING).row();
    return this;
  }

  public NpcMenu addItem(int id, final NpcMenu menu) {
    menu.parent = this;
    menu.owner = owner;
    menu.gameScreen = gameScreen;
    addItem(id, new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        gameScreen.setMenu(menu, owner);
      }
    });
    return this;
  }

  public NpcMenu addCancel(CancellationListener cancellationListener) {
    this.cancellationListener = cancellationListener;
    addItem(3400, new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        cancel();
        gameScreen.setMenu(parent, owner);
      }
    });
    return this;
  }

  public NpcMenu build() {
    pack();
    return this;
  }

  public void cancel() {
    if (cancellationListener != null) cancellationListener.onCancelled();
  }

  public interface CancellationListener {
    void onCancelled();
  }
}
