package com.gmail.collinsmith70.diablo.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.gmail.collinsmith70.diablo.ClientConsole;
import com.gmail.collinsmith70.util.BufferListener;
import com.gmail.collinsmith70.util.PrintStreamListener;

public class ClientConsoleWidget extends WidgetGroup implements BufferListener, PrintStreamListener {

  private final ClientConsole CONSOLE;

  private final TextArea OUTPUT_TEXTAREA;
  private final TextField INPUT_TEXTFIELD;

  public ClientConsoleWidget(ClientConsole console) {
    this.CONSOLE = console;
    CONSOLE.addStreamListener(this);
    CONSOLE.addBufferListener(this);

    TextField.TextFieldStyle style = new TextField.TextFieldStyle();
    style.font = CONSOLE.getClient().getDefaultFont();
    style.fontColor = Color.WHITE;

    this.OUTPUT_TEXTAREA = new TextArea("", style);
    OUTPUT_TEXTAREA.setPrefRows(24);
    ScrollPane sp = new ScrollPane(OUTPUT_TEXTAREA);

    this.INPUT_TEXTFIELD = new TextField("", style);
    INPUT_TEXTFIELD.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.ENTER) {
          CONSOLE.keyTyped('\n');
        }

        return super.keyDown(event, keycode);
      }

      @Override
      public boolean keyTyped(InputEvent event, char ch) {
        return CONSOLE.keyTyped(ch);
      }
    });

    VerticalGroup vg = new VerticalGroup();
    vg.align(Align.topLeft);
    vg.addActor(sp);
    vg.addActor(INPUT_TEXTFIELD);

    addActor(vg);
    setVisible(false);

    setFillParent(true);
    setLayoutEnabled(true);
    vg.setLayoutEnabled(true);
    vg.setFillParent(true);
    OUTPUT_TEXTAREA.setBounds(0, 0, 800, 600);
    invalidateHierarchy();
    validate();
  }

  public ClientConsole getConsole() {
    return CONSOLE;
  }

  @Override
  public void onPrintln(String s) {
    OUTPUT_TEXTAREA.setText(OUTPUT_TEXTAREA.getText() + s + '\n');
  }

  @Override
  public void modified(String buffer) {

  }

  @Override
  public void commit(String buffer) {
    INPUT_TEXTFIELD.setText("");
  }

  @Override
  public boolean flush() {
    return false;
  }

}
