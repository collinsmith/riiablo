package com.gmail.collinsmith70.unifi.graphics.drawables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.gmail.collinsmith70.unifi.content.res.Resources;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.graphics.Paint;
import com.gmail.collinsmith70.unifi.util.Bounds;

import org.apache.commons.lang3.Validate;

public class TextureDrawable extends Drawable {

    @Nullable
    private Resources resources;

    @Nullable
    private String fileName;

    @Nullable
    private Texture texture;

    @NonNull
    private Paint paint;

    private int textureWidth = INVALID_SIZE;
    private int textureHeight = INVALID_SIZE;

    public TextureDrawable() {
        _setPaint(Paint.DEFAULT);
    }

    public TextureDrawable(@NonNull AssetManager assetManager,
                           @NonNull String fileName) {
        this(assetManager, fileName, null);
    }

    public TextureDrawable(@NonNull AssetManager assetManager,
                           @NonNull String fileName,
                           @NonNull TextureLoader.TextureParameter params) {
        this();
        Validate.isTrue(assetManager != null, "assetManager cannot be null");
        Validate.isTrue(fileName != null, "fileName cannot be null");
        this.fileName = fileName;

        assetManager.load(fileName, Texture.class, params);
        assetManager.finishLoadingAsset(fileName);
        _setTexture(assetManager.get(fileName, Texture.class));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Bounds bounds = getBounds();
        final int saveCount = canvas.save();
        canvas.translate(bounds.getX(), bounds.getY());
        canvas.clipRect(getBounds());
        canvas.drawTexture(texture, 0, 0, getPaint());
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void dispose() {
        if (texture != null) {
            resources.getAssets().unload(fileName);
            //Unifi.getAssetManager().unload(fileName);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return textureWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return textureHeight;
    }

    public Texture getTexture() {
        return texture;
    }

    private void _setTexture(@NonNull Texture texture) {
        Validate.isTrue(texture != null, "texture cannot be null");
        if (getTexture() != texture) {
            this.texture = texture;
            invalidate();
        }
    }

    public void setTexture(@NonNull Texture texture) {
        _setTexture(texture);
    }

    @NonNull
    public Paint getPaint() {
        return paint;
    }

    private void _setPaint(@NonNull Paint paint) {
        Validate.isTrue(paint != null, "paint cannot be null");
        if (getPaint() != paint) {
            this.paint = paint;
            invalidate();
        }
    }

    public void setPaint(@NonNull Paint paint) {
        _setPaint(paint);
    }

}
