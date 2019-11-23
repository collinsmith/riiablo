package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Transformer;

import java.util.Iterator;

public class GraphPath extends DefaultGraphPath<Point2> implements SmoothableGraphPath<Point2, Vector2>, Pool.Poolable {
  private Vector2 tmp = new Vector2();

  @Override
  public void reset() {
    clear();
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  @Override
  public Vector2 getNodePosition(int index) {
    Point2 src = nodes.get(index);
    return tmp.set(src.x, src.y);
  }

  @Override
  public void swapNodes(int index1, int index2) {
    nodes.set(index1, nodes.get(index2));
  }

  @Override
  public void truncatePath(int newLength) {
    nodes.truncate(newLength);
  }

  @Override
  public String toString() {
    return nodes.toString();
  }

  public Iterator<Vector2> vectorIterator() {
    return IteratorUtils.transformedIterator(new Array.ArrayIterator<>(nodes), new Transformer<Point2, Vector2>() {
      final Vector2 tmp = new Vector2();

      @Override
      public Vector2 transform(Point2 input) {
        return tmp.set(input.x, input.y);
      }
    });
  }
}
