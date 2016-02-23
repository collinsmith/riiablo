package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.Nullable;

public interface Parentable {

boolean hasParent();
@Nullable  WidgetParent getParent();

}
