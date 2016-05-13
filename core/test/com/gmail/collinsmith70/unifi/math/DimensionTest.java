package com.gmail.collinsmith70.unifi.math;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class DimensionTest {

    private static final int[] data = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
            43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
    };

    @Test
    public void testSetWidth() {
        Dimension dim = new Dimension();
        for (int testCase : data) {
            dim.setWidth(testCase);
            assertTrue(dim.getWidth() == testCase);
        }
    }

    @Test
    public void testSetHeight() {
        Dimension dim = new Dimension();
        for (int testCase : data) {
            dim.setHeight(testCase);
            assertTrue(dim.getHeight() == testCase);
        }
    }

    @Test
    public void testSet_IntInt() {
        Dimension dim = new Dimension();
        int width, height;
        for (int i = 0; i < data.length - 1; i++) {
            width = data[i];
            height = data[i + 1];
            dim.set(width, height);
            assertTrue(dim.getWidth() == width);
            assertTrue(dim.getHeight() == height);
        }
    }

    @Test
    public void testSet_Dimension() {
        Dimension dim = new Dimension();
        Dimension dim2;
        int width, height;
        for (int i = 0; i < data.length - 1; i++) {
            width = data[i];
            height = data[i+1];
            dim2 = new Dimension(width, height);
            dim.set(dim2);
            assertTrue(dim.equals(dim2));
        }
    }

    @Test
    public void testOnChange() {
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Dimension() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Dimension#onChange() called");
                    }
                }.setWidth(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Dimension() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Dimension#onChange() called");
                    }
                }.setHeight(1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Dimension() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Dimension#onChange() called");
                    }
                }.set(1, 1);
            }
        });
        assertThrows(RuntimeException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new Dimension() {
                    @Override
                    protected void onChange() {
                        throw new RuntimeException("Dimension#onChange() called");
                    }
                }.set(new Dimension(1, 1));
            }
        });
    }

    @Test
    public void testEquals_IntInt() {
        int width, height;
        for (int i = 0; i < data.length - 1; i++) {
            width = data[i];
            height = data[i + 1];
            assertTrue(new Dimension(width, height).equals(width, height));
        }
    }

    @Test
    public void testEquals_Object() {
        Dimension dim1 = new Dimension(0, 0);
        Dimension dim2 = new Dimension(1, 1);
        
        assertTrue(dim1.equals(dim1));
        assertTrue(dim2.equals(dim2));
        assertFalse(dim1.equals(dim2));
        assertFalse(dim2.equals(dim1));
        assertFalse(dim1.equals(null));
        assertFalse(dim2.equals(null));
    }

    @Test
    public void testHashCode() {
        Dimension dim1 = new Dimension(0, 0);
        Dimension dim2 = new Dimension(1, 1);
        Dimension dim3 = new Dimension(0, 0);

        assertTrue(dim1.hashCode() == dim1.hashCode());
        assertTrue(dim1.hashCode() == dim3.hashCode());
        assertFalse(dim1.hashCode() == dim2.hashCode());
        assertFalse(dim3.hashCode() == dim2.hashCode());
    }

}
