package com.riiablo.server.d2gs.tcp;

import io.netty.channel.Channel;

import com.riiablo.nnet.Endpoint;

/**
 * Not used anymore -- prefer to use anonymous implementations of {@link Endpoint.IdResolver} which
 * wrap preexisting data structures.
 */
@Deprecated
public class ChannelIdResolver implements Endpoint.IdResolver<Channel> {
  private final Channel[] channels;

  public ChannelIdResolver(int maxClients) {
    channels = new Channel[maxClients];
  }

  @Override
  public Channel get(int id) {
    return channels[id];
  }
}
