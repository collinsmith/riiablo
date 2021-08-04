package com.riiablo.map2.util;

import java.util.NoSuchElementException;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.codec.util.BBox;
import com.riiablo.map2.Zone;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge;
import static com.riiablo.map2.DT1.Tile.SUBTILE_SIZE;
import static com.riiablo.map2.util.DebugMode.TILE;
import static com.riiablo.map2.util.DebugMode.UNSET;

public class ZoneGraph implements Disposable {
  final BBox box = new BBox();
  Texture texture;
  DebugMode mode = UNSET;

  final Array<Node> nodes = new Array<>();

  static class Node implements Disposable {
    static Node wrap(Zone element) {
      return new Node(element);
    }

    final Zone element;

    Node(Zone element) {
      this.element = element;
    }

    @Override
    public void dispose() {
      element.dispose();
    }

    void drawDebug(DebugMode mode, Pixmap pixmap, int x, int y) {
      element.drawDebug(mode, pixmap, x, y);
    }
  }

  public Zone claim(String name, int x, int y, int width, int height, int chunkWidth, int chunkHeight) {
    Zone element = Zone.obtain(name, x, y, width, height, chunkWidth, chunkHeight);
    Node n = Node.wrap(element);
    nodes.add(n);
    mode = UNSET;
    return element;
  }

  public Zone get(int x, int y) {
    for (Node n : nodes) {
      if (n.element.contains(x, y)) {
        return n.element;
      }
    }

    throw new NoSuchElementException("no zone found containing (" + x + "," + y + ")");
  }

  @Override
  public void dispose() {
    if (texture != null) texture.dispose();
    for (Node n : nodes) n.dispose();
  }

  public void drawDebug(Batch batch, DebugMode mode) {
    if (nodes.isEmpty()) return;
    Texture texture = prepareTexture(mode);
    if (texture == null) return;
    batch.draw(texture, box.xMin, -box.yMax);
  }

  Texture prepareTexture(DebugMode mode) {
    if (this.mode == mode) return texture;
    this.mode = mode;

    box.prepare();
    for (Node n : nodes) box.max(n.element);
    box.update();

    if (mode == TILE) {
      box.xMin /= SUBTILE_SIZE;
      box.yMin /= SUBTILE_SIZE;
      box.xMax /= SUBTILE_SIZE;
      box.yMax /= SUBTILE_SIZE;
      box.width /= SUBTILE_SIZE;
      box.height /= SUBTILE_SIZE;
    }

    Pixmap pixmap = new Pixmap(box.width, box.height, Pixmap.Format.RGB888);
    for (Node n : nodes) n.drawDebug(mode, pixmap, -box.xMin, -box.yMin);
    texture = new Texture(pixmap);
    pixmap.dispose();
    texture.setFilter(Nearest, Nearest);
    texture.setWrap(ClampToEdge, ClampToEdge);
    return texture;
  }
}
