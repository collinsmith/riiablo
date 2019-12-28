package com.riiablo.ai;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntSet;
import com.riiablo.Riiablo;
import com.riiablo.audio.Audio;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.DialogManager;
import com.riiablo.engine.client.MenuManager;
import com.riiablo.engine.server.component.MenuWrapper;
import com.riiablo.engine.server.component.PathWrapper;
import com.riiablo.engine.server.component.Pathfind;
import com.riiablo.engine.server.event.NpcInteractionEvent;
import com.riiablo.map.DS1;
import com.riiablo.widget.NpcDialogBox;
import com.riiablo.widget.NpcMenu;

import net.mostlyoriginal.api.event.common.EventSystem;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

public class Npc extends AI {
  private static final String TAG = "Npc";

  static final IntSet TALKERS    = new IntSet();
  static final IntSet REPAIRERS  = new IntSet();
  static final IntSet TRADERS    = new IntSet();
  static final IntSet GAMBLERS   = new IntSet();
  static final IntSet HIRERERS   = new IntSet();
  static {
    TALKERS.addAll(146, 147, 148, 150, 154, 155);
    REPAIRERS.addAll(154);
    TRADERS.addAll(147, 148, 154);
    GAMBLERS.addAll(147);
    HIRERERS.addAll(150);
  }

  protected ComponentMapper<MenuWrapper> mMenuWrapper;

  protected EventSystem event;

  final Vector2 tmpVec2 = new Vector2();

  static final int NULL_TARGET = ArrayUtils.INDEX_NOT_FOUND;
  int targetId = NULL_TARGET;
  float actionTimer = 0;
  boolean actionPerformed = false;
  NpcMenu menu;
  String state = "";

  String name;
  MonStats.Entry monstats;

  public Npc(int entityId) {
    super(entityId);
  }

  @Override
  public void initialize() {
    super.initialize();
    monstats = monster.monstats;
    name = monstats.NameStr.equalsIgnoreCase("dummy") ? monstats.Id : Riiablo.string.lookup(monstats.NameStr);
    mSize.get(entityId).size = 1; // fixes pathfinding issues
  }

  public void createMenu(MenuManager menuManager, final DialogManager dialogManager) {
    Validate.validState(menu == null, "menu already initialized!");
    menu = mMenuWrapper.create(entityId).menu = new NpcMenu(menuManager, entityId, name);

    final int entType = monstats.hcIdx;
    if (TALKERS.contains(entType)) {
      // talk
      menu.addItem(3381, new NpcMenu(menuManager, 3381)
          // introduction
          .addItem(3399, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              String name = Npc.this.name.toLowerCase();
              String id = name + "_act1_intro";
              dialogManager.setDialog(new NpcDialogBox(id, new NpcDialogBox.DialogCompletionListener() {
                @Override
                public void onCompleted(NpcDialogBox d) {
                  dialogManager.setDialog(null);
                }
              }));
            }
          })
          // gossip
          .addItem(3395, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              String name = Npc.this.name.toLowerCase();
              String id = name + "_act1_gossip_1";
              dialogManager.setDialog(new NpcDialogBox(id, new NpcDialogBox.DialogCompletionListener() {
                @Override
                public void onCompleted(NpcDialogBox d) {
                  dialogManager.setDialog(null);
                }
              }));
            }
          })
          .addCancel(null)
          .build());
    }

    if (REPAIRERS.contains(entType)) {
      menu.addItem(3334, new ClickListener()); // trade/repair
    } else if (TRADERS.contains(entType)) {
      menu.addItem(3396, new ClickListener()); // trade
    }

    if (HIRERERS.contains(entType)) {
      menu.addItem(3397, new ClickListener()); // gamble
    }

    if (GAMBLERS.contains(entType)) {
      menu.addItem(3398, new ClickListener()); // gamble
    }

    menu.addCancel(new NpcMenu.CancellationListener() {
        @Override
        public void onCancelled() {
          mInteractable.get(entityId).count--;
          actionTimer = 4;
        }
      })
      .build();
  }

  @Override
  public void interact(int src, final int entityId) {
    pathfinder.findPath(entityId, null);
    lookAt(src);

    // TODO: need some kind of static method that can take in some state params, e.g., character
    //       class, player mode and spit out the proper file index.
    //       I.e., akara_act1_intro -> akara_act1_intro_sor automatically if it exists
    String name = Npc.this.name.toLowerCase();
    String id = name + "_greeting_1";
    Audio.Instance instance = Riiablo.audio.play(id, false);
    if (instance == null) {
      id = name + "_greeting_inactive_1";
      Riiablo.audio.play(id, false);
    }

    actionTimer = Float.POSITIVE_INFINITY;
    actionPerformed = false;
    event.dispatch(NpcInteractionEvent.obatin(src, entityId));
    mInteractable.get(entityId).count++;
  }

  @Override
  public void update(float delta) {
    if (mInteractable.get(entityId).count > 0) {
      state = "INTERACTING";
      return;
    }

    Pathfind pathfind = mPathfind.get(entityId);
    if (pathfind == null) {
      PathWrapper pathWrapper = mPathWrapper.get(entityId);
      DS1.Path path = pathWrapper.path;
      if (targetId == NULL_TARGET) {
        targetId = 0;
      } else if (actionTimer > 0) {
        actionTimer -= delta;
        actionPerformed = actionTimer < 0;
        if (!actionPerformed && Riiablo.game != null) { // FIXME: checking Riiablo.game != null for server instances
          int player = Riiablo.game.player;
          Vector2 targetPos = mPosition.get(player).position;
          Vector2 entityPos = mPosition.get(entityId).position;
          if (entityPos.dst(targetPos) <= 8) {
            lookAt(player);
          }
        }

        state = "IDLE";
        return;
      } else if (actionPerformed) {
        actionPerformed = false;
        targetId = MathUtils.random(path.numPoints - 1);
      } else {
        int actionId = path.points[targetId].action;
        actionTimer = action(actionId);
        actionPerformed = actionTimer < 0;
        return;
      }

      state = "PATHING";
      DS1.Path.Point dst = path.points[targetId];
      pathfinder.findPath(entityId, tmpVec2.set(dst.x, dst.y), true);
    }
  }


  private float action(int actionId) {
    // path.actions == look at nearest player, chill, hold time, quest?
    // 1 = 4 second hold
    // 2 = 6 second hold
    // 3 = 4 second hold
    // 4 = special action at end of 10 second warriv jamella spell
    // 4 = special action at end of 8  second charsi jamella book
    // 4? fara = 5 seconds
    // 4 == S1
    // 5 == S2
    // etc

    switch (actionId) {
      case 1:
      case 3:
        state = "WAITING";
        return 4f;
      case 2:
        state = "WAITING";
        return 6f;
      // TODO: play anim only once, after timer, ending the action
      case 4: {
        cofs.setMode(entityId, Engine.Monster.MODE_S1);
      }
        state = "S1";
        return 10f;
      case 5: {
        cofs.setMode(entityId, Engine.Monster.MODE_S2);
      }
        state = "S2";
        return 10f;
      default:
        Gdx.app.error(TAG, "Unknown action index: " + actionId);
        state = "ERROR - WAITING";
        return 4f;
    }
  }

  @Override
  public String getState() {
    return state;
  }
}
