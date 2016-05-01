package com.gmail.collinsmith70.unifi.content;

import android.support.annotation.NonNull;

import com.badlogic.gdx.assets.AssetManager;

import org.apache.commons.lang3.Validate;

public class Context {

    @NonNull
    private AssetManager assetManager;

    public Context() {
        this(new AssetManager());
    }

    public Context(@NonNull AssetManager assetManager) {
        _setAssetManager(assetManager);
    }

    @NonNull
    public AssetManager getAssets() {
        return assetManager;
    }

    private void _setAssetManager(@NonNull AssetManager assetManager) {
        Validate.isTrue(assetManager != null, "assetManager cannot be null");
        this.assetManager = assetManager;
    }

    public void setAssetManager(@NonNull AssetManager assetManager) {
        _setAssetManager(assetManager);
    }

}
