package com.riiablo.excel;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import com.riiablo.excel.annotation.Table;

public class TableAnnotatedClass {
  TypeElement element;
  String canonicalName;
  String simpleName;

  TableAnnotatedClass(TypeElement element) {
    this.element = element;
    Table annotation = element.getAnnotation(Table.class);
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
