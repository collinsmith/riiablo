package com.riiablo.table.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import org.apache.commons.lang3.builder.ToStringBuilder;

abstract class AnnotationElement<A extends Annotation> {
  protected final Context context;
  protected final A annotation;
  protected final AnnotationMirror mirror;

  protected AnnotationElement(Context context, A annotation, AnnotationMirror mirror) {
    this.context = context;
    this.annotation = annotation;
    this.mirror = mirror;
  }

  Map<? extends ExecutableElement, ? extends AnnotationValue>
  defaults() {
    return context.elementUtils.getElementValuesWithDefaults(mirror);
  }

  AnnotationValue value(String key) {
    for (
        Map.Entry<
            ? extends ExecutableElement,
            ? extends AnnotationValue
        > entry : defaults().entrySet()) {
      if (entry.getKey().getSimpleName().contentEquals(key)) {
        return entry.getValue();
      }
    }

    return null;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("context", context)
        .append("annotation", annotation)
        .append("mirror", mirror)
        .toString();
  }
}
