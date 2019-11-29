package com.riiablo.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
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
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.PathComponent;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.map.DS1;
import com.riiablo.widget.NpcDialogBox;
import com.riiablo.widget.NpcMenu;

import org.apache.commons.lang3.ArrayUtils;

public class Npc extends AI {
  private static final String TAG = "Npc";

  private static final ComponentMapper<InteractableComponent> interactableComponent = ComponentMapper.getFor(InteractableComponent.class);
  private static final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private static final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);
  private static final ComponentMapper<PathComponent> pathComponent = ComponentMapper.getFor(PathComponent.class);
  private static final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private static final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);

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

  final Vector2 tmpVec2 = new Vector2();

  static final int NULL_TARGET = ArrayUtils.INDEX_NOT_FOUND;
  int targetId = NULL_TARGET;
  float actionTimer = 0;
  boolean actionPerformed = false;
  NpcMenu menu;
  String state = "";

  String name;
  MonStats.Entry monstats;

  public Npc(Entity entity) {
    super(entity);
    monstats = monsterComponent.monstats;
    name = monstats.NameStr.equalsIgnoreCase("dummy") ? monstats.Id : Riiablo.string.lookup(monstats.NameStr);
  }

  @Override
  public void interact(Entity src, final Entity entity) {
    entity.remove(PathfindComponent.class);
    velocityComponent.get(entity).velocity.setZero();
    lookAt(src);

    if (menu == null) {
      menu = new NpcMenu(entity, name);

      final int entType = monstats.hcIdx;
      if (TALKERS.contains(entType)) {
        // talk
        menu.addItem(3381, new NpcMenu(3381)
            // introduction
            .addItem(3399, new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                String name = Npc.this.name.toLowerCase();
                String id = name + "_act1_intro";
                Riiablo.game.setDialog(new NpcDialogBox(id, new NpcDialogBox.DialogCompletionListener() {
                  @Override
                  public void onCompleted(NpcDialogBox d) {
                    Riiablo.game.setDialog(null);
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
                Riiablo.game.setDialog(new NpcDialogBox(id, new NpcDialogBox.DialogCompletionListener() {
                  @Override
                  public void onCompleted(NpcDialogBox d) {
                    Riiablo.game.setDialog(null);
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
            interactableComponent.get(entity).count--;
            actionTimer = 4;
          }
        })
        .build();
    }

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
    Riiablo.game.setMenu(menu, entity);
    interactableComponent.get(entity).count++;
  }

  @Override
  public void update(float delta) {
    if (interactableComponent.get(entity).count > 0) {
      state = "INTERACTING";
      return;
    }

    PathfindComponent pathfindComponent = this.pathfindComponent.get(entity);
    if (pathfindComponent == null) {
      PathComponent pathComponent = this.pathComponent.get(entity);
      DS1.Path path = pathComponent.path;
      if (targetId == NULL_TARGET) {
        targetId = 0;
      } else if (actionTimer > 0) {
        actionTimer -= delta;
        actionPerformed = actionTimer < 0;
        if (!actionPerformed) {
          Entity player = Riiablo.game.player;
          Vector2 targetPos = positionComponent.get(player).position;
          Vector2 entityPos = positionComponent.get(entity).position;
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
      setPath(dst);
    }
  }

  // FIXME: some actions must be too close to the border -- path finding seems to be tossing them
  private void setPath(DS1.Path.Point dst) {
    setPath(tmpVec2.set(dst.x, dst.y));
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
        CofComponent cofComponent = this.cofComponent.get(entity);
        cofComponent.mode = Engine.Monster.MODE_S1;
      }
        state = "S1";
        return 10f;
      case 5: {
        CofComponent cofComponent = this.cofComponent.get(entity);
        cofComponent.mode = Engine.Monster.MODE_S2;
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
