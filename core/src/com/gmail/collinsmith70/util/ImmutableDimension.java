package com.gmail.collinsmith70.util;

import java.awt.Dimension;

public class ImmutableDimension extends Dimension {

public ImmutableDimension(Dimension d) {
    super(d);
}

public ImmutableDimension(int width, int height) {
    super(width, height);
}

@Override
public void setSize(int width, int height) {
    throw new UnsupportedOperationException("This Dimension instance is immutable");
}

@Override
public void setSize(double width, double height) {
    throw new UnsupportedOperationException("This Dimension instance is immutable");
}

}
