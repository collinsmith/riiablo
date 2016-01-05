package com.gmail.collinsmith70.diablo;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.validator.NumberRangeValueValidator;

public class Cvars {

private Cvars() {
    //...
}

public static class Client {

    private Client() {
        //...
    }

    public static final Cvar<Double> Scale = new Cvar<Double>(
            "Client.Scale",
            Double.class, 1.0,
            new NumberRangeValueValidator<Double>(1.0));

}

}
