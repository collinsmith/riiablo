package com.gmail.collinsmith70.diablo.widget;

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.gmail.collinsmith70.diablo.ClientConsole;
import com.gmail.collinsmith70.util.BufferListener;

public class ClientConsoleWidget extends WidgetGroup implements BufferListener {

private ClientConsole CONSOLE;

public ClientConsoleWidget(ClientConsole console) {
    this.CONSOLE = console;
    CONSOLE.addBufferListener(this);

    VerticalGroup vg = new VerticalGroup();


}

public ClientConsole getConsole() { return CONSOLE; }


@Override
public void modified(String buffer) {

}

@Override
public void commit(String buffer) {

}

@Override
public boolean flush() {
    return false;
}

}
