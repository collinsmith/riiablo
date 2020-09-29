package com.riiablo.assets;

import io.netty.util.AsciiString;

public interface PathTransformer {
  AsciiString transform(AsciiString path);
}
