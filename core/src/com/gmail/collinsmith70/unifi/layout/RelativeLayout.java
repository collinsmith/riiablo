package com.gmail.collinsmith70.unifi.layout;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetManager;

public class RelativeLayout extends AnchoredLayout {

@NonNull
@Override public WidgetManager addWidget(@NonNull Widget child) {
    if (child == null) {
        throw new IllegalArgumentException("child cannot be null");
    }

    Anchor boxedValue = child.get(AnchoredLayout.anchorDst);
    return addWidget(child, boxedValue == null ? Anchor.CENTER : boxedValue);
}
@NonNull public WidgetManager addWidget(@NonNull Widget child, Anchor anchorDst) {
    child.put(AnchoredLayout.anchor, this);
    child.put(AnchoredLayout.anchorDst, anchorDst);
    child.put(AnchoredLayout.anchorSrc, anchorDst);
    return super.addWidget(child);
}

}
