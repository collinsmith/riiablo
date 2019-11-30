package com.riiablo.server.bnls;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.BufferUtils;
import com.riiablo.net.packet.bnls.BNLSData;
import com.riiablo.net.packet.bnls.QueryRealms;
import com.riiablo.net.packet.bnls.Realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class BNLSTest {
  private static final String TAG = "BNLSTest";

  @Before
  public void setUp() throws Exception {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new BNLS(), config);
    Thread.sleep(100);
  }

  @After
  public void tearDown() throws Exception {
    Gdx.app.exit();
  }

  @Test
  public void testQueryRealmsReq() throws IOException {
    FlatBufferBuilder builder = new FlatBufferBuilder();

    QueryRealms.startQueryRealms(builder);
    int realmId = QueryRealms.endQueryRealms(builder);

    int id = com.riiablo.net.packet.bnls.BNLS.createBNLS(builder, BNLSData.QueryRealms, realmId);

    builder.finish(id);

    Socket socket = null;
    try {
      socket = Gdx.net.newClientSocket(Net.Protocol.TCP, "localhost", 6110, null);
      WritableByteChannel out = Channels.newChannel(socket.getOutputStream());
      out.write(builder.dataBuffer());

      ByteBuffer buffer = BufferUtils.newByteBuffer(4096);
      ReadableByteChannel in = Channels.newChannel(socket.getInputStream());
      in.read(buffer);
      buffer.rewind();

      com.riiablo.net.packet.bnls.BNLS packet = com.riiablo.net.packet.bnls.BNLS.getRootAsBNLS(buffer);
      Gdx.app.log(TAG, "packet " + BNLSData.name(packet.dataType()));

      if (packet.dataType() == BNLSData.ConnectionClosed) {
        Gdx.app.log(TAG, "connection closed.");
        return;
      }

      QueryRealms qr = (QueryRealms) packet.data(new QueryRealms());
      Gdx.app.log(TAG, "realms:");
      for (int i = 0; i < qr.realmsLength(); i++) {
        Realm r = qr.realms(i);
        Gdx.app.log(TAG, "realm[" + i + "]=" + r.name() + "," + r.desc());
      }
    } finally {
      Gdx.app.log(TAG, "closing socket...");
      if (socket != null) socket.dispose();
    }
  }

}
