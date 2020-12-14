package com.riiablo.excel;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import com.riiablo.excel.annotation.Format;

public class FieldElement {
  final VariableElement element;
  final TypeMirror mirror;
  final TypeName typeName;
  final Name variableName;
  final TypeName componentType;
  final Format format;

  FieldElement(Elements elementUtils, Element element) {
    this(elementUtils, (VariableElement) element);
  }

  FieldElement(Elements elementUtils, VariableElement element) {
    this.element = element;
    this.mirror = element.asType();
    this.typeName = ClassName.get(mirror);
    this.variableName = element.getSimpleName();
    this.format = element.getAnnotation(Format.class);
    if (isArray()) {
      componentType = ((ArrayTypeName) typeName).componentType;
      if (format == null) {
        throw new GenerationException(
            String.format("array type must be annotated with @%s", Format.class.getCanonicalName()),
            element);
      } else {
        AnnotationMirror a = ElementUtils.getAnnotationMirror(element, Format.class);
        AnnotationEntry endIndex = ElementUtils.getAnnotationEntry(elementUtils, a, "endIndex");
        AnnotationEntry startIndex = ElementUtils.getAnnotationEntry(elementUtils, a, "startIndex");
        if (format.endIndex() <= format.startIndex()) {
          throw new GenerationException(
              String.format("%s must be greater than %s", endIndex, startIndex),
              element, a, endIndex.value());
        }
      }
    } else {
      componentType = null;
    }
  }

  boolean isArray() {
    return mirror.getKind() == TypeKind.ARRAY;
  }
}
