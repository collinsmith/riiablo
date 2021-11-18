package com.riiablo.file;

import org.junit.jupiter.api.*;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.io.InputStream;

import com.riiablo.RiiabloTest;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.param.DcParams;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;

public class DccTest extends RiiabloTest {
  @Test
  @DisplayName("dcc_buffers w/ data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc")
  void dcc_buffers() throws Exception {
    DccDecoder decoder = new DccDecoder();
    EventExecutor executor = ImmediateEventExecutor.INSTANCE;
    MpqFileResolver resolver = new MpqFileResolver();
    try {
      final String dccName = "data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc";
      AssetDesc<Dcc> parent = AssetDesc.of(dccName, Dcc.class, DcParams.of(-1));
      MpqFileHandle dccHandle = resolver.resolve(parent);
      try {
        InputStream stream = dccHandle.bufferStream(executor, dccHandle.sectorSize()).get();
        Dcc dcc = Dcc.read(dccHandle, stream);
        int offset = dcc.dirOffset(0);
        int nextOffset = dcc.dirOffset(1);
        ByteBuf buffer = dccHandle.bufferAsync(executor, offset, nextOffset - offset).get();
        dcc.read(buffer, 0);
        decoder.decode(dcc, 0);
      } finally {
        ReferenceCountUtil.release(dccHandle);
      }
    } finally {
      resolver.dispose();
    }
  }
}
