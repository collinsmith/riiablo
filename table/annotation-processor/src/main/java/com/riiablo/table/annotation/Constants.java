package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.ArrayUtils;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.BYTE;
import static com.squareup.javapoet.TypeName.INT;
import static com.squareup.javapoet.TypeName.LONG;
import static com.squareup.javapoet.TypeName.SHORT;

final class Constants {
  private Constants() {}

  static final String RESERVED_NAME = "x";

  static boolean isReserved(Name name) {
    return name.contentEquals(RESERVED_NAME);
  }

  static final ClassName STRING = ClassName.get(String.class);
  static final ClassName MANIFEST = ClassName.get("com.riiablo.table", "TableManifest");
  static final ClassName PRIMARY_KEY = ClassName.get(PrimaryKey.class);
  static final ClassName FOREIGN_KEY = ClassName.get(ForeignKey.class);
  static final ClassName FORMAT = ClassName.get(Format.class);

  static final TypeName[] PRIMARY_KEY_TYPES = { INT, STRING };

  static boolean isPrimaryKeyType(Element element) {
    return ArrayUtils.contains(PRIMARY_KEY_TYPES, TypeName.get(element.asType()));
  }

  static final TypeName[] RECORD_FIELD_TYPES = {
      BYTE, SHORT, INT, LONG, BOOLEAN, STRING
  };

  static boolean isRecordFieldType(Element element) {
    TypeMirror mirror = element.asType();
    return ArrayUtils.contains(RECORD_FIELD_TYPES,
        TypeName.get(mirror.getKind() == TypeKind.ARRAY
            ? ((ArrayType) mirror).getComponentType()
            : mirror));
  }
}
