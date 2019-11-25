package com.riiablo.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntSet;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.widget.NpcMenu;

import org.apache.commons.lang3.ArrayUtils;

public class Npc extends AI {
  private static final String TAG = "Npc";

  private static final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private static final ComponentMapper<AngleComponent> angleComponent = ComponentMapper.getFor(AngleComponent.class);

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

  int targetId = ArrayUtils.INDEX_NOT_FOUND;
  float actionTimer = 0;
  boolean actionPerformed = false;
  NpcMenu menu;

  String name;
  MonStats.Entry monstats;

  public Npc(Entity entity) {
    super(entity);
    monstats = monsterComponent.monstats;
    name = monstats.NameStr.equalsIgnoreCase("dummy") ? monstats.Id : Riiablo.string.lookup(monstats.NameStr);
  }

  @Override
  public void interact(Entity src, Entity entity) {
    Vector2 srcPos = positionComponent.get(src).position;
    Vector2 entityPos = positionComponent.get(entity).position;
    tmpVec2.set(srcPos).sub(entityPos);
    angleComponent.get(entity).target.set(tmpVec2).nor();

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
              }
            })
            // gossip
            .addItem(3395, new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
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
            actionTimer = 4;
            //entity.target().setZero();
          }
        })
        .build();
    }

    Riiablo.game.setMenu(menu, entity);
  }
}
