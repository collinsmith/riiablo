package com.riiablo.engine.client;

import com.google.flatbuffers.FlatBufferBuilder;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import com.artemis.annotations.Wire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.engine.IntervalBaseSystem;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Ping;

public class Pinger extends IntervalBaseSystem {
  private static final String TAG = "Pinger";
  private static final boolean DEBUG = !true;

  final Array<PacketListener> packetListeners = new Array<>(false, 16);

  @Wire(name = "client.socket")
  protected Socket socket;

  // TODO: this system depends on a 32-bit int -- at some point will need to figure out if this will become an issue
  //       it may be possible to use Gdx.graphics.getFrameId() -- but that isn't related to engine tick
  private int tick;

  public long ping;
  public long rtt;

  public Pinger() {
    super(1.0f);
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
    ping = TimeUtils.millis() - packet.sendTime() - packet.processTime();
    rtt  = TimeUtils.millis() - packet.sendTime();
    notifyPing(packet, ping, rtt);
  }

  public boolean addPacketListener(PacketListener l) {
    packetListeners.add(l);
    return true;
  }

  private void notifyPing(Ping packet, long ping, long rtt) {
    for (PacketListener l : packetListeners) l.onPingResponse(this, packet, ping, rtt);
  }

  public interface PacketListener {
    void onPingResponse(Pinger pinger, Ping packet, long ping, long rtt);
  }
}
