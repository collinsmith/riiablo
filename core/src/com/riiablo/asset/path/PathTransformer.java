package com.riiablo.asset.path;

import com.riiablo.asset.MutableString;

public interface PathTransformer {
  MutableString transform(MutableString path);
}
