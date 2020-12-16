package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import java.lang.annotation.Annotation;
import java.util.Objects;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.TextStringBuilder;

class Context {
  final ProcessingEnvironment processingEnvironment;
  final Messager messager;
  final Types typeUtils;
  final Elements elementUtils;

  Context(ProcessingEnvironment processingEnvironment) {
    this.processingEnvironment = processingEnvironment;
    messager = processingEnvironment.getMessager();
    typeUtils = processingEnvironment.getTypeUtils();
    elementUtils = processingEnvironment.getElementUtils();
  }

  /**
   * @deprecated use {@link #getAnnotationMirror(Element, ClassName)} with cached value
   */
  @Deprecated
  AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
    return getAnnotationMirror(element, ClassName.get(annotationClass));
  }

  AnnotationMirror getAnnotationMirror(Element element, ClassName annotationClass) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationClass.equals(ClassName.get(annotationMirror.getAnnotationType()))) {
        return annotationMirror;
      }
    }

    return null;
  }

  void log(
      Diagnostic.Kind kind,
      Element element,
      AnnotationMirror annotationMirror,
      AnnotationValue annotationValue,
      String message, Object... args) {
    TextStringBuilder builder = new TextStringBuilder(message);
    builder.replaceAll("{element}", Objects.toString(element));
    builder.replaceAll("{annotationMirror}", Objects.toString(annotationMirror));
    builder.replaceAll("{annotationValue}", Objects.toString(annotationValue));
    for (Object arg : args) {
      if (arg instanceof Class && Annotation.class.isAssignableFrom((Class<?>) arg)) {
        builder.replaceFirst("{}", "@" + ((Class<?>) arg).getSimpleName());
      } else if (arg instanceof Element) {
        builder.replaceFirst("{}", ((Element) arg).getSimpleName().toString());
      } else if (arg != null && arg.getClass().isArray()) {
        builder.replaceFirst("{}", ArrayUtils.toString(arg));
      } else {
        builder.replaceFirst("{}", Objects.toString(arg));
      }
    }

    messager.printMessage(kind, builder, element, annotationMirror, annotationValue);
  }

  void error(String message, Object... args) {
    log(Diagnostic.Kind.ERROR, null, null, null, message, args);
  }

  void error(Element element, String message, Object... args) {
    log(Diagnostic.Kind.ERROR, element, null, null, message, args);
  }

  void error(Element element, AnnotationMirror annotationMirror, String message, Object... args) {
    log(Diagnostic.Kind.ERROR, element, annotationMirror, null, message, args);
  }

  void error(Element element, AnnotationMirror annotationMirror, AnnotationValue annotationValue, String message, Object... args) {
    log(Diagnostic.Kind.ERROR, element, annotationMirror, annotationValue, message, args);
  }

  void warn(String message, Object... args) {
    log(Diagnostic.Kind.WARNING, null, null, null, message, args);
  }

  void warn(Element element, String message, Object... args) {
    log(Diagnostic.Kind.WARNING, element, null, null, message, args);
  }

  void warn(Element element, AnnotationMirror annotationMirror, String message, Object... args) {
    log(Diagnostic.Kind.WARNING, element, annotationMirror, null, message, args);
  }

  void warn(Element element, AnnotationMirror annotationMirror, AnnotationValue annotationValue, String message, Object... args) {
    log(Diagnostic.Kind.WARNING, element, annotationMirror, annotationValue, message, args);
  }
}
