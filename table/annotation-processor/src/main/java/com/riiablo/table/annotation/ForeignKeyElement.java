package com.riiablo.table.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

final class ForeignKeyElement extends AnnotationElement<ForeignKey> {
  static ForeignKeyElement get(Context context, VariableElement element) {
    ForeignKey annotation = element.getAnnotation(ForeignKey.class);
    if (annotation == null) return null;
    if (!element.getModifiers().contains(Modifier.PUBLIC)) {
      context.warn(element, "{} fields must be declared {}", ForeignKey.class, Modifier.PUBLIC);
      return null;
    }

    AnnotationMirror mirror = context.getAnnotationMirror(element, Constants.FOREIGN_KEY);
    return new ForeignKeyElement(context, annotation, mirror);
  }

  ForeignKeyElement(Context context, ForeignKey annotation, AnnotationMirror mirror) {
    super(context, annotation, mirror);
  }
}
