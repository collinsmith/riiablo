package com.gmail.collinsmith70.unifi.content.res;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.gmail.collinsmith70.unifi.graphics.ColorUtils;
import com.gmail.collinsmith70.unifi.util.XmlUtils;

import org.apache.commons.lang3.Validate;

public class TypedArray implements Pool.Poolable {

    private boolean recycled;

    @Nullable
    private Resources res;

    @Nullable
    Resources.Theme theme;

    @Nullable
    @Size(min = 0)
    Object[] data;

    static TypedArray obtain(@NonNull Resources res,
                             @IntRange(from = 0, to = Integer.MAX_VALUE) int len) {
        Validate.isTrue(res != null, "res cannot be null");
        final TypedArray attrs = Pools.obtain(TypedArray.class);
        if (attrs == null) {
            return new TypedArray(res, len);
        } else {
            attrs.recycled = false;
            attrs.data = new Object[len];
            attrs.res = res;
        }

        return attrs;
    }

    public void recycle() {
        checkRecycled();
        Pools.free(this);
    }

    TypedArray(@NonNull Resources res,
               @IntRange(from = 0, to = Integer.MAX_VALUE) int len) {
        _setResources(res);
        this.data = new Object[len];
    }

    @Override
    public void reset() {
        recycled = true;
        res = null;
        theme = null;
        data = null;
    }

    public boolean isRecycled() {
        return recycled;
    }

    private void checkRecycled() {
        if (recycled) {
            throw new IllegalStateException(toString() + " has been recycled!");
        }
    }

    @NonNull
    public Resources getResources() {
        checkRecycled();
        return res;
    }

    private void _setResources(@NonNull Resources res) {
        Validate.isTrue(res != null, "res cannot be null");
        this.res = res;
    }

    @Nullable
    public Resources.Theme getTheme() {
        checkRecycled();
        return theme;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int length() {
        if (data == null) {
            return 0;
        }

        return data.length;
    }

    public int getInt(int index, int defValue) {
        checkRecycled();
        final Object value = data[index];
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return XmlUtils.convertValueToInt(value.toString(), defValue);
        }

        return defValue;
    }

    public float getFloat(int index, float defValue) {
        checkRecycled();
        final Object value = data[index];
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof String) {
            return Float.parseFloat(value.toString());
        }

        return defValue;
    }

    public int getColor(int index, int defValue) {
        checkRecycled();
        final Object value = data[index];
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return ColorUtils.parseColor(value.toString());
        }

        return defValue;
    }

}
