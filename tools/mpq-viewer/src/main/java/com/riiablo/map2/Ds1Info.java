package com.riiablo.map2;

import com.kotcrab.vis.ui.widget.ScrollableTextArea;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.map2.DT1.Tile.Index;
import com.riiablo.util.DebugUtils;

public class Ds1Info extends VisTable {
  DS1 ds1;
  VisTable header, objectsTable, groupsTable, pathsTable, specialsTable;
  // dependencies

  public Ds1Info() {}

  public Ds1Info setDS1(DS1 ds1) {
    if (this.ds1 == ds1) return this;
    this.ds1 = ds1;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "version: ", ds1.version);
    add(header, "width: ", ds1.width);
    add(header, "height: ", ds1.height);
    add(header, "act: ", ds1.act);
    add(header, "tag type: ", ds1.tagType);
    if (ds1.unknown.length > 0) {
      add(header, "unknown: ", DebugUtils.toByteArray(ds1.unknown));
    }
    add(header, "wall layers: ", ds1.numWalls);
    add(header, "orient layers: ", ds1.numWalls);
    add(header, "floor layers: ", ds1.numFloors);
    add(header, "shadow layers: ", ds1.numShadows);
    add(header, "tag layers: ", ds1.numTags);
    add(header).top().spaceRight(8);

    if (ds1.numObjects > 0) {
      objectsTable = createObjectsTable();
      add(objectsTable).growY().spaceRight(8);
    }

    if (ds1.numGroups > 0) {
      groupsTable = createGroupsTable();
      add(groupsTable).growY().spaceRight(8);
    }

    if (ds1.numPaths > 0) {
      pathsTable = createPathsTable();
      add(pathsTable).growY().spaceRight(8);
    }

    if (ds1.specialTiles.size > 0) {
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

  VisTable createObjectsTable() {
    VisTable objectsTable = new VisTable();
    objectsTable.add("Objects:").left().row();
    objectsTable.add(createScrollableTextArea(generateObjectsDump())).minWidth(200f).growY();
    return objectsTable;
  }

  String generateObjectsDump() {
    StringBuilder builder = new StringBuilder(1024);
    for (int i = 0, s = ds1.numObjects; i < s; i++) {
      DS1.Object object = ds1.objects[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("type: ").append(DS1.Object.Type.toString(object.type)).append('\n');
      builder.append("id: ").append(object.id).append('\n');
      builder.append("position: ").append(object.position).append('\n');
      builder.append("flags: ").append(String.format("0x%08x", object.flags)).append('\n');
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
    for (int i = 0, s = ds1.numGroups; i < s; i++) {
      DS1.Group group = ds1.groups[i];
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
    for (int i = 0, s = ds1.numPaths; i < s; i++) {
      DS1.Path path = ds1.paths[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("position: ").append(path.position).append('\n');
      for (int j = 0, s2 = path.numWaypoints; j < s2; j++) {
        DS1.Path.Waypoint wp = path.waypoints[j];
        builder.append("    ").append('[').append(j).append(']').append('\n');
        builder.append("    ").append("position: ").append(wp).append('\n');
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
    for (IntMap.Entry<Vector2> entry : ds1.specialTiles.entries()) {
      builder.append('[').append(i).append(']').append('\n');
      builder.append(IndexDefs.toString(entry.key)).append('\n');
      builder.append("m,s,o: ")
          .append(Index.mainIndex(entry.key)).append(' ')
          .append(Index.subIndex(entry.key)).append(' ')
          .append(Index.orientation(entry.key)).append('\n');
      builder.append("position: ").append(entry.value).append('\n');
      builder.append('\n');
      i++;
    }

    return builder.toString();
  }
}
