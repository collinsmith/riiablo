package com.riiablo.table.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class ParserElement {
  static ParserElement get(Context context, Element element) {
    Parser annotation = element.getAnnotation(Parser.class);
    final TypeElement parserElement, parserImplElement;
    final DeclaredType declaredType;
    if (annotation == null) {
      // Only need parserElement if generating Parser impl
      parserElement = context.elementUtils.getTypeElement(com.riiablo.table.Parser.class.getCanonicalName());
      declaredType = context.typeUtils.getDeclaredType(parserElement, element.asType());
      parserImplElement = null;
    } else {
      // Only need parserImplElement if @Parser present
      parserImplElement = getParserImpl(context, annotation);
      parserElement = null;
      declaredType = null;
    }
    return new ParserElement(annotation, declaredType, parserElement, parserImplElement);
  }

  static TypeElement getParserImpl(Context context, Parser annotation) {
    if (annotation == null) return null;
    try {
      Class<?> parserImpl = annotation.value();
      return context.elementUtils.getTypeElement(parserImpl.getCanonicalName());
    } catch (MirroredTypeException t) {
      DeclaredType parserImplMirror = (DeclaredType) t.getTypeMirror();
      return (TypeElement) parserImplMirror.asElement();
    }
  }

  final Parser annotation;
  final DeclaredType declaredType;
  final TypeElement parserElement;
  final TypeElement parserImplElement;

  ParserElement(
      Parser annotation,
      DeclaredType declaredType,
      TypeElement parserElement,
      TypeElement parserImplElement) {
    this.annotation = annotation;
    this.declaredType = declaredType;
    this.parserElement = parserElement;
    this.parserImplElement = parserImplElement;
  }

  ExecutableElement getMethod(CharSequence methodName) {
    for (Element e : parserElement.getEnclosedElements()) {
      if (e.getKind() == ElementKind.METHOD) {
        ExecutableElement methodElement = (ExecutableElement) e;
        if (methodElement.getSimpleName().contentEquals(methodName)) {
          return methodElement;
        }
      }
    }

    throw new AssertionError(parserElement + " does not contain " + methodName);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("annotation", annotation)
        .append("declaredType", declaredType)
        .append("parserElement", parserElement)
        .append("parserImplElement", parserImplElement)
        .toString();
  }
}
