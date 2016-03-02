package com.gmail.collinsmith70.unifi.layout;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;
import com.gmail.collinsmith70.unifi.widget.WidgetManager;
import com.gmail.collinsmith70.unifi.widget.WidgetParent;

public class AnchoredLayout extends WidgetGroup {

/**
 * Widget Params:
 *
 * anchor    = Target Widget
 * anchorSrc = Anchor point on this Widget
 * anchorDst = Anchor point on target Widget
 */

public static final String anchor = "anchor";
public static final String anchorSrc = "anchorSrc";
public static final String anchorDst = "anchorDst";

public enum Anchor {
    TOP_LEFT {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveTop(anchor.getTop());
                    child.moveLeft(anchor.getLeft());
                    break;
                case TOP_CENTER:
                    child.moveTop(anchor.getTop());
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveTop(anchor.getTop());
                    child.moveLeft(anchor.getRight());
                    break;
                case CENTER_LEFT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveLeft(anchor.getLeft());
                    break;
                case CENTER:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveLeft(anchor.getRight());
                    break;
                case BOTTOM_LEFT:
                    child.moveTop(anchor.getBottom());
                    child.moveLeft(anchor.getLeft());
                    break;
                case BOTTOM_CENTER:
                    child.moveTop(anchor.getBottom());
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveTop(anchor.getBottom());
                    child.moveLeft(anchor.getRight());
                    break;
                default:
            }
        }
    },
    TOP_CENTER {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveTop(anchor.getTop());
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case TOP_CENTER:
                    child.moveTop(anchor.getTop());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveTop(anchor.getTop());
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                case CENTER_LEFT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case CENTER:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                case BOTTOM_LEFT:
                    child.moveTop(anchor.getBottom());
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case BOTTOM_CENTER:
                    child.moveTop(anchor.getBottom());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveTop(anchor.getBottom());
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                default:
            }
        }
    },
    TOP_RIGHT {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveTop(anchor.getTop());
                    child.moveRight(anchor.getLeft());
                    break;
                case TOP_CENTER:
                    child.moveTop(anchor.getTop());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveTop(anchor.getTop());
                    child.moveRight(anchor.getRight());
                    break;
                case CENTER_LEFT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft());
                    break;
                case CENTER:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getRight());
                    break;
                case BOTTOM_LEFT:
                    child.moveTop(anchor.getBottom());
                    child.moveRight(anchor.getLeft());
                    break;
                case BOTTOM_CENTER:
                    child.moveTop(anchor.getBottom());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveTop(anchor.getBottom());
                    child.moveRight(anchor.getRight());
                    break;
                default:
            }
        }
    },

    CENTER_LEFT {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveLeft(anchor.getLeft());
                    break;
                case TOP_CENTER:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveLeft(anchor.getRight());
                    break;
                case CENTER_LEFT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveLeft(anchor.getLeft());
                    break;
                case CENTER:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveLeft(anchor.getRight());
                    break;
                case BOTTOM_LEFT:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveLeft(anchor.getLeft());
                    break;
                case BOTTOM_CENTER:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveLeft(anchor.getRight());
                    break;
                default:
            }
        }
    },
    CENTER {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case TOP_CENTER:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                case CENTER_LEFT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case CENTER:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                case BOTTOM_LEFT:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case BOTTOM_CENTER:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                default:
            }
        }
    },
    CENTER_RIGHT {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft());
                    break;
                case TOP_CENTER:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveTop(anchor.getTop() + (child.getHeight() / 2));
                    child.moveRight(anchor.getRight());
                    break;
                case CENTER_LEFT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft());
                    break;
                case CENTER:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveTop(anchor.getBottom() + (anchor.getHeight() / 2) + (child.getHeight() / 2));
                    child.moveRight(anchor.getRight());
                    break;
                case BOTTOM_LEFT:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft());
                    break;
                case BOTTOM_CENTER:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveTop(anchor.getBottom() + (child.getHeight() / 2));
                    child.moveRight(anchor.getRight());
                    break;
                default:
            }
        }
    },

    BOTTOM_LEFT {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveBottom(anchor.getTop());
                    child.moveLeft(anchor.getLeft());
                    break;
                case TOP_CENTER:
                    child.moveBottom(anchor.getTop());
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveBottom(anchor.getTop());
                    child.moveLeft(anchor.getRight());
                    break;
                case CENTER_LEFT:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveLeft(anchor.getLeft());
                    break;
                case CENTER:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveLeft(anchor.getRight());
                    break;
                case BOTTOM_LEFT:
                    child.moveBottom(anchor.getBottom());
                    child.moveLeft(anchor.getLeft());
                    break;
                case BOTTOM_CENTER:
                    child.moveBottom(anchor.getBottom());
                    child.moveLeft(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveBottom(anchor.getBottom());
                    child.moveLeft(anchor.getRight());
                    break;
                default:
            }
        }
    },
    BOTTOM_CENTER {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveBottom(anchor.getTop());
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case TOP_CENTER:
                    child.moveBottom(anchor.getTop());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveBottom(anchor.getTop());
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                case CENTER_LEFT:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case CENTER:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                case BOTTOM_LEFT:
                    child.moveBottom(anchor.getBottom());
                    child.moveRight(anchor.getLeft() + (child.getWidth() / 2));
                    break;
                case BOTTOM_CENTER:
                    child.moveBottom(anchor.getBottom());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2) + (child.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveBottom(anchor.getBottom());
                    child.moveRight(anchor.getRight() + (child.getWidth() / 2));
                    break;
                default:
            }
        }
    },
    BOTTOM_RIGHT {
        @Override
        void layout(@NonNull Widget child, @NonNull Widget anchor, @NonNull Anchor anchorDst) {
            switch (anchorDst) {
                case TOP_LEFT:
                    child.moveBottom(anchor.getTop());
                    child.moveRight(anchor.getLeft());
                    break;
                case TOP_CENTER:
                    child.moveBottom(anchor.getTop());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case TOP_RIGHT:
                    child.moveBottom(anchor.getTop());
                    child.moveRight(anchor.getRight());
                    break;
                case CENTER_LEFT:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft());
                    break;
                case CENTER:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case CENTER_RIGHT:
                    child.moveBottom(anchor.getBottom() + (anchor.getHeight() / 2));
                    child.moveRight(anchor.getRight());
                    break;
                case BOTTOM_LEFT:
                    child.moveBottom(anchor.getBottom());
                    child.moveRight(anchor.getLeft());
                    break;
                case BOTTOM_CENTER:
                    child.moveBottom(anchor.getBottom());
                    child.moveRight(anchor.getLeft() + (anchor.getWidth() / 2));
                    break;
                case BOTTOM_RIGHT:
                    child.moveBottom(anchor.getBottom());
                    child.moveRight(anchor.getRight());
                    break;
                default:
            }
        }
    };

    abstract void layout(@NonNull Widget child,
                         @NonNull Widget anchor,
                         @NonNull Anchor anchorDst);
}

@NonNull public WidgetManager addWidget(@NonNull Widget child) {
    if (!child.containsKey(AnchoredLayout.anchor)) {
        child.put(AnchoredLayout.anchor, this);
    }

    if (!child.containsKey(AnchoredLayout.anchorSrc)) {
        child.put(AnchoredLayout.anchorSrc, Anchor.CENTER);
    }

    if (!child.containsKey(AnchoredLayout.anchorDst)) {
        child.put(AnchoredLayout.anchorDst, Anchor.CENTER);
    }

    return super.addWidget(child);
}

@Override protected void layoutChildren() {
    for (Widget child : this) {
        if (child.getVisibility().equals(Visibility.GONE)) {
            continue;
        } else if (child instanceof WidgetParent) {
            ((WidgetParent)child).requestLayout();
        }

        Widget anchor = child.get(AnchoredLayout.anchor);
        Anchor anchorSrc = child.get(AnchoredLayout.anchorSrc);
        Anchor anchorDst = child.get(AnchoredLayout.anchorDst);
        anchorSrc.layout(child, anchor, anchorDst);
    }
}

}
