package com.riiablo.table.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;

final class FormatElement extends AnnotationElement<Format> {
  static FormatElement get(Context context, VariableElement element) {
    Format annotation = element.getAnnotation(Format.class);
    if (annotation == null) return null;
    AnnotationMirror mirror = context.getAnnotationMirror(element, Constants.FORMAT);
    return new FormatElement(context, annotation, mirror);
  }

  FormatElement(Context context, Format annotation, AnnotationMirror mirror) {
    super(context, annotation, mirror);
  }
}
