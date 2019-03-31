package com.riiablo.widget;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.audio.Audio;
import com.riiablo.codec.FontTBL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class DialogScroller extends Table implements Disposable {

  private static final FontTBL.BitmapFont FONT = Riiablo.fonts.fontformal11;
  public static final float PADDING = 8;

  DialogCompletionListener listener;
  TextArea textArea;
  ScrollPane scrollPane;
  float scrollSpeed;
  Audio.Instance audio;

  public DialogScroller(DialogCompletionListener listener) {
    this.listener = listener;
    setTouchable(Touchable.disabled);
    //setDebug(true, true);

    textArea = new TextArea("", new TextArea.TextFieldStyle() {{
      font = FONT;
      fontColor = Riiablo.colors.white;
    }}) {
      int count;
      float prefHeight;

      @Override
      public float getPrefHeight() {
        return prefHeight;
      }

      @Override
      public void setText(String str) {
        super.setText(str);
        count = StringUtils.countMatches(str, '\n');
        prefHeight = count * getStyle().font.getLineHeight();
        setHeight(getPrefHeight());
      }
    };

    scrollPane = new ScrollPane(textArea);
    scrollPane.setTouchable(Touchable.disabled);
    scrollPane.setSmoothScrolling(false);
    scrollPane.setFlickScroll(false);
    scrollPane.setFlingTime(0);
    scrollPane.setOverscroll(false, false);
    scrollPane.setClamp(false);
    scrollPane.setScrollX(-5); // FIXME: actual preferred width of text isn't calculated anywhere, this is best guess
    add(scrollPane).pad(PADDING).grow();
    pack();
  }

  public void setTextId(String str) {
    setText(Riiablo.string.lookup(str));
  }

  public void setText(String str) {
    textArea.setText(str);
    scrollPane.layout();
    scrollSpeed = 0;
    scrollPane.setScrollY(0);
  }

  public void play(String dialog) {
    // FIXME: scrollSpeed should be in pixels/sec, but timing is off by about 10-15%
    //        problem seems to be with fontformat11 metrics, applying scalar to line height
    final float lineScalar = 0.85f;
    String key = Riiablo.files.speech.get(dialog).soundstr;
    String text = Riiablo.string.lookup(key);
    String[] parts = text.split("\n", 2);
    scrollSpeed = NumberUtils.toFloat(parts[0]) / 60 * FONT.getLineHeight() * lineScalar;
    textArea.setText(parts[1]);
    scrollPane.layout();

    scrollPane.setScrollY(-scrollPane.getScrollHeight() + textArea.getStyle().font.getLineHeight() / 2);
    audio = Riiablo.audio.play(dialog, false);
  }

  @Override
  public void act(float delta) {
    if (scrollPane == null) return;
    scrollPane.setScrollY(scrollPane.getScrollY() + (scrollSpeed * delta));
    scrollPane.act(delta);
    if (scrollPane.getScrollY() > textArea.getPrefHeight()) {
      listener.onCompleted(this);
    }
  }

  @Override
  public void dispose() {
    if (audio != null) audio.stop();
  }

  public interface DialogCompletionListener {
    void onCompleted(DialogScroller d);
  }
}
