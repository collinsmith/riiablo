package gdx.diablo.widget;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import gdx.diablo.Audio;
import gdx.diablo.Diablo;
import gdx.diablo.codec.FontTBL;
import gdx.diablo.graphics.BorderedPaletteIndexedDrawable;

public class NpcDialogBox extends Table implements Disposable {

  DialogCompletionListener listener;
  TextArea textArea;
  ScrollPane scrollPane;
  float scrollSpeed;
  Audio.Instance audio;

  public NpcDialogBox(String sound, DialogCompletionListener listener) {
    this.listener = listener;
    setBackground(new BorderedPaletteIndexedDrawable());
    setTouchable(Touchable.disabled);
    //setDebug(true, true);

    // FIXME: scrollSpeed should be in pixels/sec, but timing is off by about 10-15%
    //        problem seems to be with fontformat11 metrics, applying scalar to line height
    final float lineScalar = 0.85f;
    final FontTBL.BitmapFont FONT = Diablo.fonts.fontformal11;
    String key = Diablo.files.speech.get(sound).soundstr;
    String text = Diablo.string.lookup(key);
    String[] parts = text.split("\n", 2);
    scrollSpeed = NumberUtils.toFloat(parts[0]) / 60 * FONT.getLineHeight() * lineScalar;
    final int count = StringUtils.countMatches(parts[1], '\n');
    textArea = new TextArea(parts[1], new TextArea.TextFieldStyle() {{
      font = FONT;
      fontColor = Diablo.colors.white;
    }}) {
      final float prefHeight = count * getStyle().font.getLineHeight();

      @Override
      public float getPrefHeight() {
        return prefHeight;
      }
    };

    scrollPane = new ScrollPane(textArea);
    scrollPane.setTouchable(Touchable.disabled);
    scrollPane.setSmoothScrolling(false);
    scrollPane.setFlickScroll(false);
    scrollPane.setFlingTime(0);
    scrollPane.setOverscroll(false, false);
    scrollPane.setClamp(false);
    scrollPane.setScrollX(-15); // FIXME: actual preferred width of text isn't calculated anywhere, this is best guess
    add(scrollPane).size(330, 128);
    pack();

    scrollPane.setScrollY(-scrollPane.getScrollHeight() + textArea.getStyle().font.getLineHeight() / 2);
    audio = Diablo.audio.play(sound, false);
  }

  @Override
  public void act(float delta) {
    scrollPane.setScrollY(scrollPane.getScrollY() + (scrollSpeed * delta));
    scrollPane.act(delta);
    if (scrollPane.getScrollY() > textArea.getPrefHeight()) {
      listener.onCompleted(this);
    }
  }

  @Override
  public void dispose() {
    audio.stop();
  }

  public interface DialogCompletionListener {
    void onCompleted(NpcDialogBox d);
  }
}
