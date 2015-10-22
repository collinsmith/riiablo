package com.google.collinsmith70.diablo.cvar;

import com.google.collinsmith70.diablo.Cvar;
import com.google.collinsmith70.diablo.CvarGroup;

public class Client implements CvarGroup {
private Client() {
    //...
}

public static final Cvar Language = new Cvar("Client.Language", String.class);

public static class Overlay implements CvarGroup {
    private Overlay() {
        //...
    }

    public static final Cvar ConsoleFont  = new Cvar("Client.Overlay.ConsoleFont", String.class);
    public static final Cvar VSyncEnabled = new Cvar("Client.Overlay.VSyncEnabled", Boolean.class);

    public static class ConsoleFontColor implements CvarGroup {
        private ConsoleFontColor() {
            //...
        }

        public static final Cvar r = new Cvar("Client.Overlay.ConsoleFontColor.r", Integer.class);
        public static final Cvar g = new Cvar("Client.Overlay.ConsoleFontColor.g", Integer.class);
        public static final Cvar b = new Cvar("Client.Overlay.ConsoleFontColor.b", Integer.class);
        public static final Cvar a = new Cvar("Client.Overlay.ConsoleFontColor.a", Integer.class);
    }
}
}
