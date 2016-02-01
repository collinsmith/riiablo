package com.gmail.collinsmith70.unifi.widget;

import com.google.common.base.Strings;

public class TextWidget extends Widget {

private String text;

public TextWidget() {
    this(null);
}

public TextWidget(String text) {
    setText(text);
}

public String getText() {
    return text;
}

public void setText(String text) {
    this.text = Strings.nullToEmpty(text);
}

@Override
public String toString() {
    return text;
}

}
