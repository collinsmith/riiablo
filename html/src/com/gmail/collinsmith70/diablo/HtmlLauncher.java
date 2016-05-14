package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class HtmlLauncher extends GwtApplication {

    private ApplicationListener applicationListener;

    @Override
    public ApplicationListener createApplicationListener() {
        return new Client();
    }

    @Override
    public GwtApplicationConfiguration getConfig() {
        return new GwtApplicationConfiguration(480, 320);
    }

    @Override
    public ApplicationListener getApplicationListener() {
        if (applicationListener == null) {
            applicationListener = createApplicationListener();
        }

        return applicationListener;
    }

}