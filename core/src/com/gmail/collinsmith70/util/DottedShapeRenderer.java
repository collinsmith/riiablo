package com.gmail.collinsmith70.util;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class DottedShapeRenderer extends ShapeRenderer {

public DottedShapeRenderer() {
    super();
}

public DottedShapeRenderer(int maxVertices) {
    super(maxVertices);
}

public DottedShapeRenderer(int maxVertices, @Nullable ShaderProgram defaultShader) {
    super(maxVertices, defaultShader);
}

@Override
public void rect(float x, float y, float width, float height) {
    if (!getCurrentType().equals(ShapeType.Point)) {
        super.rect(x, y, width, height);
        return;
    }

    rect(x, y, width, height, 8);
}

public void rect(float x, float y, float width, float height, int dotDist) {
    if (!getCurrentType().equals(ShapeType.Point)) {
        super.rect(x, y, width, height);
        return;
    }

    dottedLine(x, y, x, y+height, dotDist);
    dottedLine(x, y+height, x+width, y+height, dotDist);
    dottedLine(x+width, y+height, x+width, y, dotDist);
    dottedLine(x+width, y, x, y, dotDist);
}

public void dottedLine(float x1, float y1, float x2, float y2, int dotDist) {
    Vector2 vec2 = new Vector2(x2, y2).sub(new Vector2(x1, y1));
    float length = vec2.len();
    for (int i = 0; i < length; i += dotDist) {
        vec2.clamp(length - i, length - i);
        point(x1 + vec2.x, y1 + vec2.y, 0);
    }
}

}
