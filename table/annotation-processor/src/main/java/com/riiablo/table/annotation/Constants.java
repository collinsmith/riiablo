package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import org.apache.commons.lang3.ArrayUtils;

final class Constants {
  private Constants() {}

  static final String RESERVED_NAME = "x";

  static boolean isReserved(Name name) {
    return name.contentEquals(RESERVED_NAME);
  }

  static final ClassName STRING = ClassName.get(String.class);
  static final ClassName PRIMARY_KEY = ClassName.get(PrimaryKey.class);
  static final ClassName FORMAT = ClassName.get(Format.class);

  static final TypeName[] PRIMARY_KEY_TYPES = { TypeName.INT, STRING };

  static boolean isPrimaryKey(Element element) {
    return ArrayUtils.contains(PRIMARY_KEY_TYPES, TypeName.get(element.asType()));
  }
}