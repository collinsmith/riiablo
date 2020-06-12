package com.riiablo.engine.client;

import com.riiablo.Riiablo;
import com.riiablo.engine.IntervalBaseSystem;
import com.riiablo.net.packet.d2gs.Ping;
import com.riiablo.profiler.SystemProfiler;

public class NetworkProfiler extends IntervalBaseSystem implements Pinger.PacketListener {
  private static final String TAG = "NetworkProfiler";
  private static final boolean DEBUG = !true;

  /**
   * Seconds to store samples -- averages will be taken of this time frame.
   */
  private static final float SAMPLES_DURATION = 30;

  protected Pinger pinger;

  protected SystemProfiler pingProfiler;
  protected SystemProfiler rttProfiler;

  private long ping;
  private long rtt;

  public NetworkProfiler() {
    super(SAMPLES_DURATION / SystemProfiler.SAMPLES);
  }

  @Override
  protected void initialize() {
    pinger.addPacketListener(this);

    pingProfiler = SystemProfiler.create("Ping");
    pingProfiler.setColor(0, 1, 1, 1);

    rttProfiler = SystemProfiler.create("RTT");
    rttProfiler.setColor(1, 0, 1, 1);
  }

  @Override
  protected void processSystem() {
    pingProfiler.sample(ping * 1000000);
    Riiablo.metrics.ping = pingProfiler.getAverage() / 1000000;

    rttProfiler.sample(rtt * 1000000);
    Riiablo.metrics.rtt = rttProfiler.getAverage() / 1000000;
  }

  @Override
  public void onPingResponse(Pinger pinger, Ping packet, long ping, long rtt) {
    this.ping = ping;
    this.rtt = rtt;
  }
}
