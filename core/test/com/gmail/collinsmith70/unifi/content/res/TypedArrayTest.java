package com.gmail.collinsmith70.unifi.content.res;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.gmail.collinsmith70.diablo.Client;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Collin Smith <i>collinsmith70@gmail.com</i>
 */
public class TypedArrayTest {

    private static HeadlessApplication APP;
    private static Resources RES;

    @BeforeClass
    public static void init() {
        final HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        final ApplicationListener client = new Client(1280, 720);
        APP = new HeadlessApplication(client, config);

        AssetManager ASSETS = new AssetManager();
        RES = new Resources(ASSETS);
    }

    @AfterClass
    public static void release() {
        RES = null;
        APP.exit();
        APP = null;
    }

    @Test
    public void testObtain() {
        TypedArray array = TypedArray.obtain(RES, 5);
        assertTrue(array.data != null);
        assertTrue(array.data.length == 5);
        assertTrue(!array.isRecycled());
        array.recycle();
    }

    @Test
    public void testRecycle() {
        TypedArray array = TypedArray.obtain(RES, 5);
        array.recycle();
        assertTrue(array.isRecycled());
    }

    @Test
    public void testReset() {
        final TypedArray array = TypedArray.obtain(RES, 5);
        array.recycle();
        assertTrue(array.isRecycled());
        assertThrows(IllegalStateException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Resources res = array.getResources();
            }
        });
        assertThrows(IllegalStateException.class, new Assert.ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Resources.Theme theme = array.getTheme();
            }
        });
        assertTrue(array.data == null);
    }
    
}
