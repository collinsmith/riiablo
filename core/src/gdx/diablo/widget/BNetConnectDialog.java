package gdx.diablo.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import gdx.diablo.Diablo;
import gdx.diablo.codec.DC6;
import gdx.diablo.loader.DC6Loader;

public class BNetConnectDialog extends Dialog {
  final AssetDescriptor<DC6> PopUp_340x224Descriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\PopUp_340x224.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  final AssetDescriptor<DC6> MediumButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\MediumButtonBlank.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);

  Label status;

  public BNetConnectDialog() {
    Diablo.assets.load(PopUp_340x224Descriptor);
    Diablo.assets.load(MediumButtonBlankDescriptor);

    setDebug(true, true);
  }

  public void create() {
    Diablo.assets.finishLoadingAsset(PopUp_340x224Descriptor);
    TextureRegion PopUp_340x224 = Diablo.assets.get(PopUp_340x224Descriptor).getTexture();
    setBackground(new TextureRegionDrawable(PopUp_340x224));

    getContentTable().add(new Table() {{
      add(new Label(5171, Diablo.fonts.font30) {{
        setWrap(true);
        setAlignment(Align.center);
      }}).width(220).row();
      add(status = new Label("", Diablo.fonts.font24)).growY().row();
      add(new Label("*", Diablo.fonts.font24)).row();
      setWidth(220);
      pad(10);
    }}).growY();

    Table buttonTable = getButtonTable();
    Button btnCancel = new TextButton(5134, new TextButton.TextButtonStyle() {{
      Diablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 MediumButtonBlank = Diablo.assets.get(MediumButtonBlankDescriptor);
      up = new TextureRegionDrawable(MediumButtonBlank.getTexture(0));
      down = new TextureRegionDrawable(MediumButtonBlank.getTexture(1));
      font = Diablo.fonts.fontexocet10;
    }});
    btnCancel.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        hide(null);
      }
    });
    buttonTable.add(btnCancel);
    buttonTable.padBottom(10);
  }

  @Override
  public com.badlogic.gdx.scenes.scene2d.ui.Dialog show(Stage stage, Action action) {
    setPosition(
        Diablo.VIRTUAL_WIDTH_CENTER  - 340 / 2,
        Diablo.VIRTUAL_HEIGHT_CENTER - 224 / 2);
    return super.show(stage, action);
  }

  @Override
  public void hide(Action action) {
    super.hide(action);
  }

  @Override
  public void dispose() {
    Diablo.assets.unload(PopUp_340x224Descriptor.fileName);
    Diablo.assets.unload(MediumButtonBlankDescriptor.fileName);
  }

  public void connect(Stage stage) {
    show(stage, null);
    queryGateway();
    findServer();
    accessAccount();
    checkVersion();
  }

  private void queryGateway() {
    status.setText(5172);
    findServer();
  }

  private void findServer() {
    status.setText(5173);
    Net.HttpRequest request = new HttpRequestBuilder()
        .newRequest()
        .method(Net.HttpMethods.GET)
        .url("http://hydra:6112/find-server")
        .build();
    Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
      @Override
      public void handleHttpResponse(Net.HttpResponse httpResponse) {
        accessAccount();
      }

      @Override
      public void failed(Throwable t) {
        BNetConnectDialog.this.failed(t.getMessage());
      }

      @Override
      public void cancelled() {
        BNetConnectDialog.this.failed("cancelled");
      }
    });
  }

  private void accessAccount() {
    status.setText(5174);
    checkVersion();
  }

  private void checkVersion() {
    status.setText(5175);
    connected();
  }

  protected void connected() {
  }

  protected void failed(String message) {
  }
}
