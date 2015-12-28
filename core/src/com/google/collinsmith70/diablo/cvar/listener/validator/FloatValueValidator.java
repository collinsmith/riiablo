package com.google.collinsmith70.diablo.cvar.listener.validator;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarValueValidator;

public class FloatValueValidator implements CvarValueValidator<Float> {

private static final String TAG = FloatValueValidator.class.getSimpleName();

private final float min;
private final float max;

public FloatValueValidator(float min, float max) {
    this.min = min;
    this.max = max;
}

public FloatValueValidator(float min) {
    this.min = min;
    this.max = Float.MAX_VALUE;
}

@Override
public Float onValidateValue(Cvar<Float> cvar, Float fromValue, Float toValue) {
    if (toValue < min) {
        Gdx.app.log(TAG, String.format(
                "%s casted from %f to %f",
                cvar.getKey(),
                toValue,
                min));
        return min;
    } else if (toValue > max) {
        Gdx.app.log(TAG, String.format(
                "%s casted from %f to %f",
                cvar.getKey(),
                toValue,
                max));
        return max;
    }

    return toValue;
}

}
