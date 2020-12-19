package com.riiablo.table.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class InjectorElement {
  static InjectorElement get(Context context, Element element) {
    Injector annotation = element.getAnnotation(Injector.class);
    final TypeElement injectorElement, injectorImplElement;
    if (annotation == null) {
      // Only need injectorElement if generating Injector impl
      injectorElement = context.elementUtils.getTypeElement(com.riiablo.table.Injector.class.getCanonicalName());
      injectorImplElement = null;
    } else {
      // Only need injectorImplElement if @Injector present
      injectorImplElement = getInjectorImpl(context, annotation);
      injectorElement = null;
    }
    return new InjectorElement(annotation, injectorElement, injectorImplElement);
  }

  static TypeElement getInjectorImpl(Context context, Injector annotation) {
    if (annotation == null) return null;
    try {
      Class<?> injectorImpl = annotation.value();
      return context.elementUtils.getTypeElement(injectorImpl.getCanonicalName());
    } catch (MirroredTypeException t) {
      DeclaredType injectorImplMirror = (DeclaredType) t.getTypeMirror();
      return (TypeElement) injectorImplMirror.asElement();
    }
  }

  final Injector annotation;
  final TypeElement injectorElement;
  final TypeElement injectorImplElement;

  InjectorElement(
      Injector annotation,
      TypeElement injectorElement,
      TypeElement injectorImplElement) {
    this.annotation = annotation;
    this.injectorElement = injectorElement;
    this.injectorImplElement = injectorImplElement;
  }

  ExecutableElement getMethod(CharSequence methodName) {
    for (Element e : injectorElement.getEnclosedElements()) {
      if (e.getKind() == ElementKind.METHOD) {
        ExecutableElement methodElement = (ExecutableElement) e;
        if (methodElement.getSimpleName().contentEquals(methodName)) {
          return methodElement;
        }
      }
    }

    throw new AssertionError(injectorElement + " does not contain " + methodName);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("annotation", annotation)
        .append("injectorElement", injectorElement)
        .append("injectorImplElement", injectorImplElement)
        .toString();
  }
}
