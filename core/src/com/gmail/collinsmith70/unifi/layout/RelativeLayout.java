package com.gmail.collinsmith70.unifi.layout;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;
import com.gmail.collinsmith70.unifi.widget.WidgetManager;
import com.gmail.collinsmith70.unifi.widget.WidgetParent;

import java.util.EnumSet;

public class RelativeLayout extends WidgetGroup implements WidgetGroup.LayoutParamChangeListener {

@Override
public void onChange(String param, Object value) {
    if (param.equals("relativeTo")) {

    }
}

public enum RelativeTo { BOTTOM, LEFT, RIGHT, TOP; }
public static final EnumSet<RelativeTo> CENTERED
        = EnumSet.of(RelativeTo.BOTTOM, RelativeTo.LEFT, RelativeTo.RIGHT, RelativeTo.TOP);

@NonNull
@Override public WidgetManager addWidget(@NonNull Widget child) {
    if (child == null) {
        throw new IllegalArgumentException("child cannot be null");
    }

    if (!child.containsKey("relativeTo")) {
        return addWidget(child, CENTERED);
    }

    EnumSet<RelativeTo> boxedValue = child.get("relativeTo");
    return addWidget(child, boxedValue == null ? CENTERED : boxedValue);
}
@NonNull public WidgetManager addWidget(@NonNull Widget child, EnumSet<RelativeTo> relativeTo) {
    super.addWidget(child);
    child.put("relativeTo", relativeTo);
    if (hasParent()) {
        getParent().requestLayout();
    } else {
        requestLayout();
    }

    return this;
}

@Override protected void layoutChildren() {
    for (Widget child : this) {
        if (child.getVisibility().equals(Visibility.GONE)) {
            continue;
        } else if (child instanceof WidgetParent) {
            ((WidgetParent)child).requestLayout();
        }

        EnumSet<RelativeTo> relativeTo = child.get("relativeTo");
        if (relativeTo.contains(RelativeTo.LEFT) && relativeTo.contains(RelativeTo.RIGHT)) {
            final int right = getLeft() + (getWidth() / 2) + (child.getWidth() / 2);
            child.moveRelativeRight(right);
        } else if (relativeTo.contains(RelativeTo.LEFT)) {
            child.moveRelativeLeft(getLeft());
        } else if (relativeTo.contains(RelativeTo.RIGHT)) {
            child.moveRelativeRight(getRight());
        }

        if (relativeTo.contains(RelativeTo.BOTTOM) && relativeTo.contains(RelativeTo.TOP)) {
            final int top = getBottom() + (getHeight() / 2) + (child.getHeight() / 2);
            child.moveRelativeTop(top);
        } else if (relativeTo.contains(RelativeTo.BOTTOM)) {
            child.moveRelativeBottom(getBottom());
        } else if (relativeTo.contains(RelativeTo.TOP)) {
            child.moveRelativeTop(getTop());
        }
    }
}

}
