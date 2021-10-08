package com.riiablo.excel;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import org.apache.commons.lang3.ArrayUtils;

import com.riiablo.excel.annotation.PrimaryKey;

public class PrimaryKeyAnnotatedElement {
  static final TypeName STRING = ClassName.get("java.lang", "String");
  static final TypeName[] VALID_TYPES = new TypeName[] { TypeName.INT, STRING };

  static PrimaryKeyAnnotatedElement get(Element element) {
    AnnotationMirror a = ElementUtils.getAnnotationMirror(element, PrimaryKey.class);
    if (a == null) return null;

    if (element.getKind() != ElementKind.FIELD) {
      throw new GenerationException(
          String.format("only fields can be @%s", PrimaryKey.class.getCanonicalName()),
          element, a);
    }

    TypeName elementType = ClassName.get(element.asType());
    if (!ArrayUtils.contains(VALID_TYPES, elementType)) {
      throw new GenerationException(
          String.format("@%s must be one of %s",
              PrimaryKey.class.getCanonicalName(),
              Arrays.toString(VALID_TYPES)),
          element, a);
    }

    PrimaryKey annotation = element.getAnnotation(PrimaryKey.class);
    return new PrimaryKeyAnnotatedElement((VariableElement) element, a, annotation);
  }

  PrimaryKey annotation;
  AnnotationMirror mirror;
  VariableElement element;

  PrimaryKeyAnnotatedElement(VariableElement element, AnnotationMirror mirror, PrimaryKey annotation) {
    this.element = element;
    this.mirror = mirror;
    this.annotation = annotation;
  }
}
