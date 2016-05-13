package com.gmail.collinsmith70.unifi.math;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RectTest {

    private static final int[] data = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
            43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
    };

    @Test
    public void testSetLeft() {
        Rect rect = new Rect();
        for (int testCase : data) {
            rect.setLeft(testCase);
            assertTrue(rect.getLeft() == testCase);
        }
    }

    @Test
    public void testSetTop() {
        Rect rect = new Rect();
        for (int testCase : data) {
            rect.setTop(testCase);
            assertTrue(rect.getTop() == testCase);
        }
    }

    @Test
    public void testSetRight() {
        Rect rect = new Rect();
        for (int testCase : data) {
            rect.setRight(testCase);
            assertTrue(rect.getRight() == testCase);
        }
    }

    @Test
    public void testSetBottom() {
        Rect rect = new Rect();
        for (int testCase : data) {
            rect.setBottom(testCase);
            assertTrue(rect.getBottom() == testCase);
        }
    }

    @Test
    public void testSet_IntIntIntInt() {
        Rect rect = new Rect();
        int left, top, right, bottom;
        for (int i = 0; i < data.length - 3; i++) {
            left = data[i];
            top = data[i + 1];
            right = data[i + 1];
            bottom = data[i + 1];
            rect.set(left, top, right, bottom);
            assertTrue(rect.getLeft() == left);
            assertTrue(rect.getTop() == top);
            assertTrue(rect.getRight() == right);
            assertTrue(rect.getBottom() == bottom);
        }
    }

    @Test
    public void testSet_Rect() {
        Rect rect = new Rect();
        Rect rect2;
        int left, top, right, bottom;
        for (int i = 0; i < data.length - 3; i++) {
            left = data[i];
            top = data[i + 1];
            right = data[i + 2];
            bottom = data[i + 3];
            rect2 = new Rect(left, top, right, bottom);
            rect.set(rect2);
            assertTrue(rect.equals(rect2));
        }
    }

    @Test
    public void testOnChange() {
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Rect() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Rect#onChange() called");
                    }
                }.setLeft(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Rect() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Rect#onChange() called");
                    }
                }.setTop(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Rect() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Rect#onChange() called");
                    }
                }.setRight(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Rect() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Rect#onChange() called");
                    }
                }.setBottom(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Rect() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Rect#onChange() called");
                    }
                }.set(1, 1, 1, 1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Rect() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Rect#onChange() called");
                    }
                }.set(new Rect(1, 1, 1, 1));
            }
        });
    }

    @Test
    public void testEquals_IntIntIntInt() {
        int left, top, right, bottom;
        for (int i = 0; i < data.length - 3; i++) {
            left = data[i];
            top = data[i + 1];
            right = data[i + 2];
            bottom = data[i + 3];
            assertTrue(new Rect(left, top, right, bottom).equals(left, top, right, bottom));
        }
    }

    @Test
    public void testEquals_Object() {
        Rect rect1 = new Rect(0, 0, 0, 0);
        Rect rect2 = new Rect(1, 1, 1, 1);

        assertTrue(rect1.equals(rect1));
        assertTrue(rect2.equals(rect2));
        assertFalse(rect1.equals(rect2));
        assertFalse(rect2.equals(rect1));
        assertFalse(rect1.equals(null));
        assertFalse(rect2.equals(null));
    }

    @Test
    public void testHashCode() {
        Rect rect1 = new Rect(0, 0, 0, 0);
        Rect rect2 = new Rect(1, 1, 1, 1);
        Rect rect3 = new Rect(0, 0, 0, 0);

        assertTrue(rect1.hashCode() == rect1.hashCode());
        assertTrue(rect1.hashCode() == rect3.hashCode());
        assertFalse(rect1.hashCode() == rect2.hashCode());
        assertFalse(rect3.hashCode() == rect2.hashCode());
    }

}
