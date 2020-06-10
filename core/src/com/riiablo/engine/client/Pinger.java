package com.riiablo.engine.client;

import com.google.flatbuffers.FlatBufferBuilder;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.Riiablo;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Ping;

@All
public class Pinger extends IntervalSystem {
  private static final String TAG = "Pinger";
  private static final boolean DEBUG = !true;

  @Wire(name = "client.socket")
  protected Socket socket;

  // TODO: this system depends on a 32-bit int -- at some point will need to figure out if this will become an issue
  //       it may be possible to use Gdx.graphics.getFrameId() -- but that isn't related to engine tick
  private int tick;

// TODO: provide a running average of past N RTTs
//  private final double deltas[] = new double[5];
//  private int deltaCount;

  public Pinger() {
    super(null, 1.0f);
  }

  @Override
  protected void processSystem() {
    FlatBufferBuilder builder = new FlatBufferBuilder(0);
    int dataOffset = Ping.createPing(builder, tick++, TimeUtils.millis(), 0);
    int root = D2GS.createD2GS(builder, D2GSData.Ping, dataOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);

    try {
      OutputStream out = socket.getOutputStream();
      WritableByteChannel channelOut = Channels.newChannel(out);
      channelOut.write(builder.dataBuffer());
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }

  public void Ping(Ping packet) {
    Riiablo.ping = TimeUtils.millis() - packet.sendTime() - packet.processTime();
    Riiablo.rtt = TimeUtils.millis() - packet.sendTime();
  }
}
