package com.riiablo.table.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;

final class PrimaryKeyElement extends AnnotationElement<PrimaryKey> {
  static PrimaryKeyElement get(Context context, VariableElement element) {
    PrimaryKey annotation = element.getAnnotation(PrimaryKey.class);
    if (annotation == null) return null;
    AnnotationMirror mirror = context.getAnnotationMirror(element, Constants.PRIMARY_KEY);
    return new PrimaryKeyElement(context, annotation, mirror);
  }

  PrimaryKeyElement(Context context, PrimaryKey annotation, AnnotationMirror mirror) {
    super(context, annotation, mirror);
  }
}
