package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Bounded {

    @NonNull
    Bounds getBounds();

    @NonNull
    Bounds getBounds(@Nullable Bounds dst);

    void setBounds(@NonNull Bounds src);

    boolean hasBounds();

}
