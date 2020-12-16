package com.riiablo.table.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

final class PrimaryKeyElement extends AnnotationElement<PrimaryKey> {
  static PrimaryKeyElement get(Context context, VariableElement element) {
    PrimaryKey annotation = element.getAnnotation(PrimaryKey.class);
    if (annotation == null) return null;
    if (!element.getModifiers().contains(Modifier.PUBLIC)) {
      context.warn(element, "{} fields must be declared {}", PrimaryKey.class, Modifier.PUBLIC);
      return null;
    }

    AnnotationMirror mirror = context.getAnnotationMirror(element, Constants.PRIMARY_KEY);
    return new PrimaryKeyElement(context, annotation, mirror);
  }

  PrimaryKeyElement(Context context, PrimaryKey annotation, AnnotationMirror mirror) {
    super(context, annotation, mirror);
  }
}
