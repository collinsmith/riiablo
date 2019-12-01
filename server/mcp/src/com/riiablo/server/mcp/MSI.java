package com.riiablo.server.mcp;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.riiablo.net.packet.msi.MSIData;
import com.riiablo.net.packet.msi.Result;
import com.riiablo.net.packet.msi.StartInstance;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DateFormat;
import java.util.Calendar;

public class MSI extends ApplicationAdapter {
  private static final String TAG = "MSI";

  static final int PORT = 6112;

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new MSI(), config);
  }

  ServerSocket server;
  ByteBuffer buffer;
  final Array<Process> instances = new Array<>();

  MSI() {}

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    final Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    Gdx.app.log(TAG, format.format(calendar.getTime()));

    try {
      InetAddress address = InetAddress.getLocalHost();
      Gdx.app.log(TAG, "IP Address: " + address.getHostAddress() + ":" + PORT);
      Gdx.app.log(TAG, "Host Name: " + address.getHostName());
    } catch (UnknownHostException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    }

    Gdx.app.log(TAG, "Starting server...");
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, PORT, null);
    buffer = BufferUtils.newByteBuffer(4096);
  }

  @Override
  public void render() {
    Socket socket = null;
    try {
      Gdx.app.log(TAG, "waiting...");
      socket = server.accept(null);
      Gdx.app.log(TAG, "connection from " + socket.getRemoteAddress());

      buffer.clear();
      buffer.mark();
      ReadableByteChannel in = Channels.newChannel(socket.getInputStream());
      in.read(buffer);
      buffer.limit(buffer.position());
      buffer.reset();

      com.riiablo.net.packet.msi.MSI packet = com.riiablo.net.packet.msi.MSI.getRootAsMSI(buffer);
      Gdx.app.log(TAG, "packet type " + MSIData.name(packet.dataType()));
      process(socket, packet);
    } catch (Throwable t) {
      if (socket != null) socket.dispose();
    }
  }

  private void process(Socket socket, com.riiablo.net.packet.msi.MSI packet) throws IOException {
    switch (packet.dataType()) {
      case MSIData.StartInstance:
        StartInstance(socket, packet);
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
    }
  }

  private boolean StartInstance(Socket socket, com.riiablo.net.packet.msi.MSI packet) throws IOException {
    Gdx.app.debug(TAG, "Starting instance...");

    try {
      File outFile = new File("D2GS.tmp");
      ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "server/d2gs/build/libs/d2gs-1.0.jar");
      processBuilder.redirectOutput(ProcessBuilder.Redirect.to(outFile));
      processBuilder.redirectError(ProcessBuilder.Redirect.to(outFile));
      Process process = processBuilder.start();
      instances.add(process);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    int ip     = 2130706433; // 127.0.0.1
    short port = 6114;

    FlatBufferBuilder builder = new FlatBufferBuilder();
    StartInstance.startStartInstance(builder);
    StartInstance.addResult(builder, Result.SUCCESS);
    StartInstance.addIp(builder, ip);
    StartInstance.addPort(builder, port);
    int startInstanceOffset = StartInstance.endStartInstance(builder);
    int id = com.riiablo.net.packet.msi.MSI.createMSI(builder, MSIData.StartInstance, startInstanceOffset);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    Gdx.app.debug(TAG, "Returning instance at " + InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(ip).array()));
    return true;
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "Shutting down...");
    server.dispose();
    for (Process process : instances) {
      process.destroy();
    }
  }
}
