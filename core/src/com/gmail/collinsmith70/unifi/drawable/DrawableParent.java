package com.gmail.collinsmith70.unifi.drawable;

import android.support.annotation.IntRange;

public interface DrawableParent {

@IntRange(from = 0, to = Integer.MAX_VALUE) int getX();
@IntRange(from = 0, to = Integer.MAX_VALUE) int getY();

}
