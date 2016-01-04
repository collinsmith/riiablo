package com.gmail.collinsmith70.diablo;

import com.gmail.collinsmith70.cvar.Cvar;

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
            Double.class, 1.0);

}

}
