package com.google.collinsmith70.diablo.cvar.listener.validator;

import com.badlogic.gdx.Gdx;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarValueValidator;

public class IntegerValueValidator implements CvarValueValidator<Integer> {

private static final String TAG = FloatValueValidator.class.getSimpleName();

private final int min;
private final int max;

public IntegerValueValidator(int min, int max) {
    this.min = min;
    this.max = max;
}

public IntegerValueValidator(int max) {
    this(0, max);
}

@Override
public Integer onValidateValue(Cvar<Integer> cvar, Integer fromValue, Integer toValue) {
    if (toValue < min) {
        Gdx.app.log(TAG, String.format(
                "%s casted from %d to %d",
                cvar.getKey(),
                toValue,
                min));
        return min;
    } else if (toValue > max) {
        Gdx.app.log(TAG, String.format(
                "%s casted from %d to %d",
                cvar.getKey(),
                toValue,
                max));
        return max;
    }

    return toValue;
}

}