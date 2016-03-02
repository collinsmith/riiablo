package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Button extends Widget {

public Button() {
    setFocusable(true);
}

@Override
public void onDrawBackground(@NonNull Batch batch) {
    final ShapeRenderer shapeRenderer = new ShapeRenderer();
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); {
        Color color = Color.RED;
        if (!isEnabled()) {
            color = Color.DARK_GRAY;
        } else if (!isFocusable()) {
            color = Color.LIGHT_GRAY;
        } else if (isDown() && isOver()) {
            color = Color.GREEN;
        } else if (isOver()) {
            color = Color.BLUE;
        }

        shapeRenderer.setColor(color);
        shapeRenderer.rect(getX()+1, getY()+1, getWidth(), getHeight());
    } shapeRenderer.end();
    shapeRenderer.dispose();
    super.onDrawBackground(batch);
}

}
