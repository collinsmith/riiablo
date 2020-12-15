package com.riiablo.table.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class SerializerElement {
  static SerializerElement get(Context context, Element element) {
    Serializer annotation = element.getAnnotation(Serializer.class);
    final TypeElement serializerElement, serializerImplElement;
    final DeclaredType declaredType;
    if (annotation == null) {
      // Only need serializerElement if generating Serializer impl
      serializerElement = context.elementUtils.getTypeElement(com.riiablo.table.Serializer.class.getCanonicalName());
      declaredType = context.typeUtils.getDeclaredType(serializerElement, element.asType());
      serializerImplElement = null;
    } else {
      // Only need serializerImplElement if @Serializer present
      serializerImplElement = getSerializerImpl(context, annotation);
      serializerElement = null;
      declaredType = null;
    }
    return new SerializerElement(annotation, declaredType, serializerElement, serializerImplElement);
  }

  static TypeElement getSerializerImpl(Context context, Serializer annotation) {
    if (annotation == null) return null;
    try {
      Class<?> serializerImpl = annotation.value();
      return context.elementUtils.getTypeElement(serializerImpl.getCanonicalName());
    } catch (MirroredTypeException t) {
      DeclaredType serializerImplMirror = (DeclaredType) t.getTypeMirror();
      return (TypeElement) serializerImplMirror.asElement();
    }
  }

  final Serializer annotation;
  final DeclaredType declaredType;
  final TypeElement serializerElement;
  final TypeElement serializerImplElement;

  SerializerElement(
      Serializer annotation,
      DeclaredType declaredType,
      TypeElement serializerElement,
      TypeElement serializerImplElement) {
    this.annotation = annotation;
    this.declaredType = declaredType;
    this.serializerElement = serializerElement;
    this.serializerImplElement = serializerImplElement;
  }

  ExecutableElement getMethod(CharSequence methodName) {
    for (Element e : serializerElement.getEnclosedElements()) {
      if (e.getKind() == ElementKind.METHOD) {
        ExecutableElement methodElement = (ExecutableElement) e;
        if (methodElement.getSimpleName().contentEquals(methodName)) {
          return methodElement;
        }
      }
    }

    throw new AssertionError(serializerElement + " does not contain " + methodName);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("annotation", annotation)
        .append("declaredType", declaredType)
        .append("serializerElement", serializerElement)
        .append("serializerImplElement", serializerImplElement)
        .toString();
  }
}
