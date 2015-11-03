package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class PropagatingEventHandler implements EventListener {

private static final Vector2 COORDS = new Vector2();

private InputProcessor inputProcessor;

public PropagatingEventHandler(InputProcessor inputProcessor) {
    if (inputProcessor == null) {
        throw new IllegalArgumentException("Input Processor cannot be null");
    }

    this.inputProcessor = inputProcessor;
}

@Override
public boolean handle(Event e) {
    if (!(e instanceof InputEvent)) {
        return false;
    }

    InputEvent event = (InputEvent)e;
    switch (event.getType()) {
        case keyDown:
            return inputProcessor.keyDown(event.getKeyCode());
        case keyUp:
            return inputProcessor.keyUp(event.getKeyCode());
        case keyTyped:
            return inputProcessor.keyTyped(event.getCharacter());
    }

    event.toCoordinates(event.getListenerActor(), COORDS);
    switch (event.getType()) {
        case touchDown:
            return inputProcessor.touchDown((int)COORDS.x, (int)COORDS.y, event.getPointer(), event.getButton());
        case touchUp:
            inputProcessor.touchUp((int)COORDS.x, (int)COORDS.y, event.getPointer(), event.getButton());
            return true;
        case touchDragged:
            inputProcessor.touchDragged((int)COORDS.x, (int)COORDS.y, event.getPointer());
            return true;
        case mouseMoved:
            return inputProcessor.mouseMoved((int)COORDS.x, (int)COORDS.y);
        case scrolled:
            return inputProcessor.scrolled(event.getScrollAmount());
    }

    return false;
}

}
