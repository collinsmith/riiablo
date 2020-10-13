package com.riiablo.asset.path;

import io.netty.util.AsciiString;

public interface PathTransformer {
  AsciiString transform(AsciiString path);
}
