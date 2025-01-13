package com.riiablo.map5;

import com.kotcrab.vis.ui.widget.ScrollableTextArea;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.Riiablo;
import com.riiablo.map2.IndexDefs;

public class Ds1Info extends VisTable {
  Ds1 ds1;
  VisTable header, dt1sTable, objectsTable, groupsTable, pathsTable, specialsTable;

  public Ds1Info() {}

  public Ds1Info setDs1(Ds1 ds1) {
    if (this.ds1 == ds1) return this;
    this.ds1 = ds1;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "version: ", ds1.version);
    add(header, "width: ", ds1.width);
    add(header, "height: ", ds1.height);
    add(header, "act: ", Riiablo.actToString(ds1.act));
    add(header, "subst: ", Ds1.substToString(ds1.substMethod));
    add(header, "wall layers: ", ds1.numWalls);
    add(header, "orient layers: ", ds1.numWalls);
    add(header, "floor layers: ", ds1.numFloors);
    add(header, "shadow layers: ", ds1.numShadows);
    add(header, "tag layers: ", ds1.numTags);
    add(header).top().spaceRight(8);

    if (!ds1.dependencies.isEmpty()) {
      dt1sTable = createDt1sTable();
      add(dt1sTable).growY().spaceRight(8);
    }

    if (!ds1.spawners.isEmpty()) {
      objectsTable = createSpawnersTable();
      add(objectsTable).growY().spaceRight(8);
    }

    if (!ds1.groups.isEmpty()) {
      groupsTable = createGroupsTable();
      add(groupsTable).growY().spaceRight(8);
    }

    if (!ds1.paths.isEmpty()) {
      pathsTable = createPathsTable();
      add(pathsTable).growY().spaceRight(8);
    }

    if (!ds1.specials.isEmpty()) {
      specialsTable = createSpecialsTable();
      add(specialsTable).growY().spaceRight(8);
    }

    return this;
  }

  private static VisTable add(VisTable table, String label, int value) {
    return add(table, label, String.valueOf(value));
  }

  private static VisTable add(VisTable table, String label, String format, Object... args) {
    table.add(label).right();
    table.add(String.format(format, args)).left();
    table.row();
    return table;
  }

  VisScrollPane createScrollableTextArea(String text) {
    ScrollableTextArea textArea = new ScrollableTextArea(text);
    final VisScrollPane scrollPane = (VisScrollPane) textArea.createCompatibleScrollPane();
    scrollPane.addListener(new ClickListener() {
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        getStage().setScrollFocus(scrollPane);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        getStage().setScrollFocus(null);
      }
    });
    return scrollPane;
  }

  VisTable createDt1sTable() {
    VisTable objectsTable = new VisTable();
    objectsTable.add("Tile Libraries:").left().row();
    objectsTable.add(createScrollableTextArea(generateDt1sDump())).minWidth(400f).growY();
    return objectsTable;
  }

  String generateDt1sDump() {
    StringBuilder builder = new StringBuilder(1024);
    int i = 0;
    for (String dt1 : ds1.dependencies) {
      builder.append('[').append(i).append(']').append(dt1).append('\n');
      i++;
    }
    return builder.toString();
  }

  VisTable createSpawnersTable() {
    VisTable objectsTable = new VisTable();
    objectsTable.add("Spawners:").left().row();
    objectsTable.add(createScrollableTextArea(generateSpawnersDump())).minWidth(200f).growY();
    return objectsTable;
  }

  String generateSpawnersDump() {
    StringBuilder builder = new StringBuilder(1024);
    Object[] spawners = ds1.spawners.items;
    for (int i = 0, s = ds1.spawners.size; i < s; i++) {
      Ds1.Spawner spawner = (Ds1.Spawner) spawners[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("type: ").append(Ds1.Spawner.Type.toString(spawner.type)).append('\n');
      builder.append("id: ").append(spawner.id).append('\n');
      builder.append("position: ").append(spawner.position).append('\n');
      builder.append("flags: ").append(String.format("0x%08x", spawner.flags)).append('\n');
      builder.append('\n');
    }
    return builder.toString();
  }

  VisTable createGroupsTable() {
    VisTable groupsTable = new VisTable();
    groupsTable.add("Groups:").left().row();
    groupsTable.add(createScrollableTextArea(generateGroupsDump())).minWidth(200f).growY();
    return groupsTable;
  }

  String generateGroupsDump() {
    StringBuilder builder = new StringBuilder(1024);
    Object[] groups = ds1.groups.items;
    for (int i = 0, s = ds1.groups.size; i < s; i++) {
      Ds1.Group group = (Ds1.Group) groups[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("bounds: ").append(group.bounds).append('\n');
      builder.append("unknown: ").append(String.format("0x%08x", group.unk)).append('\n');
      builder.append('\n');
    }
    return builder.toString();
  }

  VisTable createPathsTable() {
    VisTable pathTable = new VisTable();
    pathTable.add("Paths:").left().row();
    pathTable.add(createScrollableTextArea(generatePathsDump())).minWidth(200f).growY();
    return pathTable;
  }

  String generatePathsDump() {
    StringBuilder builder = new StringBuilder(1024);
    Object[] paths = ds1.paths.items;
    for (int i = 0, s = ds1.paths.size; i < s; i++) {
      Ds1.Path path = (Ds1.Path) paths[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("position: ").append(path.position).append('\n');
      Object[] nodes = path.nodes.items;
      for (int j = 0, s2 = path.nodes.size; j < s2; j++) {
        Ds1.Path.Node wp = (Ds1.Path.Node) nodes[j];
        builder.append("    ").append('[').append(j).append(']').append('\n');
        builder.append("    ").append("position: ").append(wp.cpy()).append('\n');
        builder.append("    ").append("action: ").append(wp.action).append('\n');
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  VisTable createSpecialsTable() {
    VisTable specialsTable = new VisTable();
    specialsTable.add("Specials:").left().row();
    specialsTable.add(createScrollableTextArea(generateSpecialsDump())).minWidth(200f).growY();
    return specialsTable;
  }

  String generateSpecialsDump() {
    int i = 0;
    StringBuilder builder = new StringBuilder(1024);
    for (IntMap.Entry<Vector2> entry : ds1.specials.entries()) {
      builder.append('[').append(i).append(']').append('\n');
      builder.append(IndexDefs.toString(entry.key)).append('\n');
      builder.append("m,s,o: ")
          .append(Tile.Index.mainIndex(entry.key)).append(' ')
          .append(Tile.Index.subIndex(entry.key)).append(' ')
          .append(Tile.Index.tileType(entry.key)).append('\n');
      builder.append("position: ").append(entry.value).append('\n');
      builder.append('\n');
      i++;
    }

    return builder.toString();
  }
}
