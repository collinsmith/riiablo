package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

final class Constants {
  private Constants() {}

  static final ClassName STRING = ClassName.get(String.class);

  static final TypeName[] PRIMARY_KEY_TYPES = { TypeName.INT, STRING };
}
