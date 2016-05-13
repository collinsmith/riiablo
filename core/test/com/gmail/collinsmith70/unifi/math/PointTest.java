package com.gmail.collinsmith70.unifi.math;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PointTest {

    private static final int[] data = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
            43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
    };

    @Test
    public void testSetX() {
        Point pt = new Point();
        for (int testCase : data) {
            pt.setX(testCase);
            assertTrue(pt.getX() == testCase);
        }
    }

    @Test
    public void testSetY() {
        Point pt = new Point();
        for (int testCase : data) {
            pt.setY(testCase);
            assertTrue(pt.getY() == testCase);
        }
    }

    @Test
    public void testSet() {
        Point pt = new Point();
        int width, height;
        for (int i = 0; i < data.length - 1; i++) {
            width = data[i];
            height = data[i + 1];
            pt.set(width, height);
            assertTrue(pt.getX() == width);
            assertTrue(pt.getY() == height);
        }
    }

    @Test
    public void testOnChange() {
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Point() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Point#onChange() called");
                    }
                }.setX(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Point() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Point#onChange() called");
                    }
                }.setY(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Point() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Point#onChange() called");
                    }
                }.set(1, 1);
            }
        });
    }

    @Test
    public void testEquals_IntInt() {
        int width, height;
        for (int i = 0; i < data.length - 1; i++) {
            width = data[i];
            height = data[i + 1];
            assertTrue(new Point(width, height).equals(width, height));
        }
    }

    @Test
    public void testEquals_Object() {
        Point pt1 = new Point(0, 0);
        Point pt2 = new Point(1, 1);

        assertTrue(pt1.equals(pt1));
        assertTrue(pt2.equals(pt2));
        assertFalse(pt1.equals(pt2));
        assertFalse(pt2.equals(pt1));
        assertFalse(pt1.equals(null));
        assertFalse(pt2.equals(null));
    }

    @Test
    public void testHashCode() {
        Point pt1 = new Point(0, 0);
        Point pt2 = new Point(1, 1);
        Point pt3 = new Point(0, 0);

        assertTrue(pt1.hashCode() == pt1.hashCode());
        assertTrue(pt1.hashCode() == pt3.hashCode());
        assertFalse(pt1.hashCode() == pt2.hashCode());
        assertFalse(pt3.hashCode() == pt2.hashCode());
    }

}
