package com.riiablo.file;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.io.InputStream;

import com.riiablo.RiiabloTest;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.param.DcParams;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;

public class Dc6Test extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.file.Dc6", Level.TRACE);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "data\\global\\monsters\\ty\\ra\\tyralitnuhth.dc6",
      "data\\global\\ui\\panel\\invchar6.dc6",
      "data\\global\\ui\\Loading\\loadingscreen.dc6",
  })
  void test(String dc6Name) throws Exception {
    EventExecutor executor = ImmediateEventExecutor.INSTANCE;
    MpqFileResolver resolver = new MpqFileResolver();
    try {
      AssetDesc<Dc6> parent = AssetDesc.of(dc6Name, Dc6.class, DcParams.of(-1));
      MpqFileHandle dc6Handle = resolver.resolve(parent);
      try {
        InputStream stream = dc6Handle.bufferStream(executor, dc6Handle.sectorSize()).get();
        Dc6 dc6 = Dc6.read(dc6Handle, stream);
        int offset = dc6.dirOffset(0);
        int nextOffset = dc6.dirOffset(1);
        ByteBuf buffer = dc6Handle.bufferAsync(executor, offset, nextOffset - offset).get();
        dc6.read(buffer, 0);
      } finally {
        ReferenceCountUtil.release(dc6Handle);
      }
    } finally {
      resolver.dispose();
    }
  }
}
