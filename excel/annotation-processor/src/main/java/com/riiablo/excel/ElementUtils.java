package com.riiablo.excel;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;

public final class ElementUtils {
  private ElementUtils() {}

  public static AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
    List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
    for (AnnotationMirror annotationMirror : annotationMirrors) {
      TypeName declaredType = ClassName.get(annotationMirror.getAnnotationType());
      TypeName annotationType = TypeName.get(annotation);
      if (declaredType.equals(annotationType)) {
        return annotationMirror;
      }
    }

    return null;
  }

  public static AnnotationEntry getAnnotationEntry(
      Elements elementUtils,
      AnnotationMirror annotationMirror,
      String property
  ) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationEntry
        : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
      ExecutableElement key = annotationEntry.getKey();
      if (key.getSimpleName().contentEquals(property)) {
        return new AnnotationEntry(annotationEntry.getKey(), annotationEntry.getValue());
      }
    }

    return null;
  }
}
