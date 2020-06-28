package com.riiablo.server.d2gs.tcp;

import io.netty.channel.Channel;

import com.riiablo.nnet.Endpoint;

public class ChannelIdResolver implements Endpoint.IdResolver<Channel> {
  private final Channel[] channels;

  public ChannelIdResolver(int maxClients) {
    channels = new Channel[maxClients];
  }

  @Override
  public Channel get(int id) {
    return channels[id];
  }

  @Override
  public Channel put(int id, Channel ch) {
    Channel oldValue = channels[id];
    channels[id] = ch;
    return oldValue;
  }

  @Override
  public Channel remove(int id) {
    return put(id, null);
  }
}
