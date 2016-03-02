package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.google.common.base.Strings;

public class TextWidget extends Widget {

public TextWidget(@NonNull BitmapFont font) {
    this(font, null);
}
public TextWidget(@NonNull BitmapFont font, @Nullable String text) {
    setFont(font);
    setText(text);
}

@NonNull private String text;
@NonNull public String getText() {
    return text;
}
public void setText(@Nullable String text) {
    this.text = Strings.nullToEmpty(text);
}

@NonNull private BitmapFont font;
@NonNull public BitmapFont getFont() {
    return font;
}
public void setFont(@NonNull BitmapFont font) {
    if (font == null) {
        throw new IllegalArgumentException("font cannot be null");
    }

    this.font = font;
}

@Override public String toString() {
    return text;
}

@Override
public void onDraw(@NonNull Batch batch) {
    getFont().draw(batch, getText(), getX(), getY());
}

}
