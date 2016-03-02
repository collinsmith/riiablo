package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class EditableTextWidget extends TextWidget {

public EditableTextWidget(@NonNull BitmapFont font) {
    super(font);
}

public EditableTextWidget(@NonNull BitmapFont font, @Nullable String text) {
    super(font, text);
}

}
