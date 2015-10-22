package com.google.collinsmith70.old2;

public class Cvar {
    private Cvar() {
        //...
    }

    public static class Client {
        private Client() {
            //...
        }
        public static final String Language = "Client.Language";

        public static class Vis {
            private Vis() {
                //...
            }
            public static final String ConsoleFont  = "Client.Vis.ConsoleFont";
            public static final String VSyncEnabled = "Client.Vis.VSyncEnabled";

            public static class ConsoleFontColor {
                private ConsoleFontColor() {
                    //...
                }
                public static final String r = "Client.Vis.ConsoleFontColor.r";
                public static final String g = "Client.Vis.ConsoleFontColor.g";
                public static final String b = "Client.Vis.ConsoleFontColor.b";
                public static final String a = "Client.Vis.ConsoleFontColor.a";
            }
        }
    }
}
