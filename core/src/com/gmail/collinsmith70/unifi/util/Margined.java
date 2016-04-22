package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Margined {

    @NonNull
    Margins getMargins();

    @NonNull
    Margins getMargins(@Nullable Margins dst);

    void setMargins(@NonNull Margins src);

    boolean hasMargins();

}
