package com.google.collinsmith70.old.util;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class EventHandler {
	private static final Vector2 tmpCoords = new Vector2();

	private EventHandler() {
		//...
	}

	public static boolean handle(Event e, InputProcessor processor) {
		if (!(e instanceof InputEvent)) {
			return false;
		}
		
		if (processor == null) {
			return false;
		}

		InputEvent event = (InputEvent)e;

		switch (event.getType()) {
			case keyDown:
				return processor.keyDown(event.getKeyCode());
			case keyUp:
				return processor.keyUp(event.getKeyCode());
			case keyTyped:
				return processor.keyTyped(event.getCharacter());
		}

		event.toCoordinates(event.getListenerActor(), tmpCoords);

		switch (event.getType()) {
			case touchDown:
				return processor.touchDown((int)tmpCoords.x, (int)tmpCoords.y, event.getPointer(), event.getButton());
			case touchUp:
				processor.touchUp((int)tmpCoords.x, (int)tmpCoords.y, event.getPointer(), event.getButton());
				return true;
			case touchDragged:
				processor.touchDragged((int)tmpCoords.x, (int)tmpCoords.y, event.getPointer());
				return true;
			case mouseMoved:
				return processor.mouseMoved((int)tmpCoords.x, (int)tmpCoords.y);
			case scrolled:
				return processor.scrolled(event.getScrollAmount());
		}

		return false;
	}
}
