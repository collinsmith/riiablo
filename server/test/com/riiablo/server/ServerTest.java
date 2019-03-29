package com.riiablo.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Json;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerTest {
  private static final String TAG = "ServerTest";

  @Before
  public void setUp() throws Exception {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new ServerBrowser(), config);
  }

  @After
  public void tearDown() throws Exception {
    Gdx.app.exit();
  }

  @Test
  public void testGetGames() {
    final AtomicBoolean done = new AtomicBoolean();
    final Net.HttpRequest request = new HttpRequestBuilder()
        .newRequest()
        .method(Net.HttpMethods.GET)
        .url("http://hydra:6112/get-sessions")
        .build();
    Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
      @Override
      public void handleHttpResponse(Net.HttpResponse httpResponse) {
        Gdx.app.log(TAG, httpResponse.getResultAsString());
        done.set(true);
      }

      @Override
      public void failed(Throwable t) {
        done.set(true);
      }

      @Override
      public void cancelled() {
        done.set(true);
      }
    });
    while (!done.get());
  }

  @Test
  public void testCreateGame() {
    final AtomicBoolean done = new AtomicBoolean();
    Net.HttpRequest request = new HttpRequestBuilder()
        .newRequest()
        .method(Net.HttpMethods.POST)
        .url("http://hydra:6112/create-session")
        .jsonContent(new Session.Builder() {{
          name     = "test game";
          password = "1111";
          desc     = "test desc";
        }})
        .build();
    Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
      @Override
      public void handleHttpResponse(Net.HttpResponse httpResponse) {
        Gdx.app.log(TAG, ObjectUtils.toString(httpResponse));
        Gdx.app.log(TAG, httpResponse.getResultAsString());
        done.set(true);
      }

      @Override
      public void failed(Throwable t) {
        Gdx.app.log(TAG, ObjectUtils.toString(t));
        done.set(true);
      }

      @Override
      public void cancelled() {
        Gdx.app.log(TAG, "cancelled");
        done.set(true);
      }
    });
    while (!done.get()) ;
  }

  @Test
  public void testLogin() {
    final AtomicBoolean done = new AtomicBoolean();
    Net.HttpRequest request = new HttpRequestBuilder()
        .newRequest()
        .method(Net.HttpMethods.POST)
        .url("http://hydra:6112/login")
        .jsonContent(new Account.Builder() {{ account = "test"; }})
        .build();
    Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
      @Override
      public void handleHttpResponse(Net.HttpResponse httpResponse) {
        Gdx.app.log(TAG, ObjectUtils.toString(httpResponse));
        Gdx.app.log(TAG, httpResponse.getResultAsString());
        done.set(true);
      }

      @Override
      public void failed(Throwable t) {
        Gdx.app.log(TAG, ObjectUtils.toString(t));
        done.set(true);
      }

      @Override
      public void cancelled() {
        Gdx.app.log(TAG, "cancelled");
        done.set(true);
      }
    });
    while (!done.get()) ;
  }

  @Test
  public void testConnect() {
    final AtomicBoolean done = new AtomicBoolean();
    Net.HttpRequest request = new HttpRequestBuilder()
        .newRequest()
        .method(Net.HttpMethods.POST)
        .url("http://hydra:6112/create-session")
        .jsonContent(new Session.Builder() {{
          name = "test game";
          password = "1111";
          desc = "test desc";
        }})
        .build();
    Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
      @Override
      public void handleHttpResponse(Net.HttpResponse httpResponse) {
        Session session = new Json().fromJson(Session.class, httpResponse.getResultAsString());
        Gdx.app.log(TAG, httpResponse.getResultAsString());
        Socket socket = null;
        try {
          socket = Gdx.net.newClientSocket(Net.Protocol.TCP, session.host, session.port, null);
        } finally {
          if (socket != null) socket.dispose();
        }

        done.set(true);
      }

      @Override
      public void failed(Throwable t) {
        Gdx.app.log(TAG, ObjectUtils.toString(t));
        done.set(true);
      }

      @Override
      public void cancelled() {
        Gdx.app.log(TAG, "cancelled");
        done.set(true);
      }
    });
    while (!done.get());
  }

  @Test
  public void testDisconnect() {
    final AtomicBoolean done = new AtomicBoolean();
    Net.HttpRequest request = new HttpRequestBuilder()
        .newRequest()
        .method(Net.HttpMethods.POST)
        .url("http://hydra:6112/create-session")
        .jsonContent(new Session.Builder() {{
          name = "test game";
          password = "1111";
          desc = "test desc";
        }})
        .build();
    Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
      @Override
      public void handleHttpResponse(Net.HttpResponse httpResponse) {
        Session session = new Json().fromJson(Session.class, httpResponse.getResultAsString());
        Socket socket = null;
        try {
          socket = Gdx.net.newClientSocket(Net.Protocol.TCP, session.host, session.port, null);
          Gdx.app.log(TAG, "Connected: " + socket.isConnected());
          new PrintWriter(socket.getOutputStream(), true).println("test");
          try {
            Thread.sleep(500);
          } catch (InterruptedException ignored) {}
        } finally {
          Gdx.app.log(TAG, "disconnecting...");
          if (socket != null) {
            //IOUtils.closeQuietly(socket.getInputStream());
            IOUtils.closeQuietly(socket.getOutputStream());
            socket.dispose();
          }
        }

        try {
          Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        done.set(true);
      }

      @Override
      public void failed(Throwable t) {
        Gdx.app.log(TAG, ObjectUtils.toString(t));
        done.set(true);
      }

      @Override
      public void cancelled() {
        Gdx.app.log(TAG, "cancelled");
        done.set(true);
      }
    });
    while (!done.get()) ;
  }
}