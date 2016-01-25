package com.google.collinsmith70.diablo.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gmail.collinsmith70.diablo.Client;

public class DesktopLauncher {
	public static void main (String[] args) {
        boolean forceWindowed = false;
        for (String arg : args) {
            if ((arg.equalsIgnoreCase("-windowed")
              || arg.equalsIgnoreCase("-w"))) {
                forceWindowed = true;
            }
        }

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = false;
        config.width = 1280;
        config.height = 720;
		config.foregroundFPS = 0;
		config.backgroundFPS = 10;
		config.allowSoftwareMode = true;
		config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
		config.addIcon("ic_launcher_32.png", Files.FileType.Internal);
		config.addIcon("ic_launcher_16.png", Files.FileType.Internal);
		new LwjglApplication(new Client(1280, 720, forceWindowed), config);
	}
}
