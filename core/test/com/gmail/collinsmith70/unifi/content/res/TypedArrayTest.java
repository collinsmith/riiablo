package com.gmail.collinsmith70.unifi.content.res;

import com.badlogic.gdx.assets.AssetManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Collin Smith <i>collinsmith70@gmail.com</i>
 */
public class TypedArrayTest {
    
    private final AssetManager ASSETS = new AssetManager();
    
    public TypedArrayTest() {
    }

    @Test
    public void testObtain() {
        final Resources RES = new Resources(ASSETS);
        TypedArray array = TypedArray.obtain(RES, 5);
        assertTrue(array.data != null);
        assertTrue(array.data.length == 5);
        assertTrue(!array.isRecycled());
        array.recycle();
    }

    @Test
    public void testRecycle() {
        final Resources RES = new Resources(ASSETS);
        TypedArray array = TypedArray.obtain(RES, 5);
        array.recycle();
        assertTrue(array.isRecycled());
    }

    @Test
    public void testReset() {
        final Resources RES = new Resources(ASSETS);
        TypedArray array = TypedArray.obtain(RES, 5);
        array.recycle();
        assertTrue(array.isRecycled());
        assertTrue(array.getResources() == null);
        assertTrue(array.getTheme() == null);
        assertTrue(array.data == null);
    }
    
}
