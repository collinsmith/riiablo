package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Padded {

    @NonNull
    Padding getPadding();

    @NonNull
    Padding getPadding(@Nullable Padding dst);

    void setPadding(@NonNull Padding src);

    boolean hasPadding();

}
