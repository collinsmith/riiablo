package com.riiablo.excel;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import com.riiablo.excel.annotation.SerializedWith;

public class SerializedWithAnnotatedElement {
  static SerializedWithAnnotatedElement get(TypeElement element) {
    SerializedWith annotation = element.getAnnotation(SerializedWith.class);
    if (annotation == null) return null;
    return new SerializedWithAnnotatedElement(element, annotation);
  }

  SerializedWith annotation;
  TypeElement element;
  String canonicalName;
  String simpleName;

  SerializedWithAnnotatedElement(TypeElement element, SerializedWith annotation) {
    this.element = element;
    this.annotation = annotation;
    try {
      Class<?> clazz = annotation.value();
      canonicalName = clazz.getCanonicalName();
      simpleName = clazz.getSimpleName();
    } catch (MirroredTypeException t) {
      DeclaredType mirroredType = (DeclaredType) t.getTypeMirror();
      TypeElement mirroredElement = (TypeElement) mirroredType.asElement();
      canonicalName = mirroredElement.getQualifiedName().toString();
      simpleName = mirroredElement.getSimpleName().toString();
    }
  }
}
