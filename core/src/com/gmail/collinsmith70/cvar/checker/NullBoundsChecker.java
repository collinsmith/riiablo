package com.gmail.collinsmith70.cvar.checker;

import com.gmail.collinsmith70.cvar.BoundsChecker;

public enum NullBoundsChecker implements BoundsChecker {
INSTANCE;

@Override
public boolean isWithinBounds(Object obj) {
    return true;
}

}
