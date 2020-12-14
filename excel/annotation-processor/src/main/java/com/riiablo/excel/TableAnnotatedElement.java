package com.riiablo.excel;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import com.riiablo.excel.annotation.Table;

public class TableAnnotatedElement {
  static TableAnnotatedElement get(TypeElement element) {
    Table annotation = element.getAnnotation(Table.class);
    if (annotation == null) return null;
    return new TableAnnotatedElement(element, annotation);
  }

  Table annotation;
  TypeElement element;
  String canonicalName;
  String simpleName;

  TableAnnotatedElement(TypeElement element, Table annotation) {
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
