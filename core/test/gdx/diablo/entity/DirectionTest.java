package gdx.diablo.entity;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DirectionTest {
  @Test
  public void testRadians4() {
    System.out.println(Arrays.toString(Direction.RADIANS_4));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_4) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians4m() {
    System.out.println(Arrays.toString(Direction.RADIANS_4));
    System.out.println(Arrays.toString(Direction.RADIANS_4M));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_4M) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians8() {
    System.out.println(Arrays.toString(Direction.RADIANS_8));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_8) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians8m() {
    System.out.println(Arrays.toString(Direction.RADIANS_8));
    System.out.println(Arrays.toString(Direction.RADIANS_8M));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_8M) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians16() {
    System.out.println(Arrays.toString(Direction.RADIANS_16));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_16) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians16m() {
    System.out.println(Arrays.toString(Direction.RADIANS_16));
    System.out.println(Arrays.toString(Direction.RADIANS_16M));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_16M) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians32() {
    System.out.println(Arrays.toString(Direction.RADIANS_32));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_32) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void testRadians32m() {
    System.out.println(Arrays.toString(Direction.RADIANS_32));
    System.out.println(Arrays.toString(Direction.RADIANS_32M));
    double t = Integer.MIN_VALUE;
    for (float f : Direction.RADIANS_32M) {
      Assert.assertTrue(f > t);
      t = f;
    }
  }

  @Test
  public void test_radiansToDirection4() {
    for (float f : Direction.RADIANS_8M) {
      System.out.println(f + " : " + Direction.radiansToDirection(f, 4));
    }
  }

  @Test
  public void test_radiansToDirection8() {
    for (float f : Direction.RADIANS_16M) {
      System.out.println(f + " : " + Direction.radiansToDirection(f, 8));
    }
  }

  @Test
  public void test_radiansToDirection16() {
    for (float f : Direction.RADIANS_32M) {
      System.out.println(f + " : " + Direction.radiansToDirection(f, 16));
    }
  }

  @Test
  public void test_radiansToDirection32() {
    for (float f : Direction.RADIANS_32M) {
      System.out.println(f + " : " + Direction.radiansToDirection(f, 32));
    }
  }
}