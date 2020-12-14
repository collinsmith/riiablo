package com.riiablo.excel;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

public class AnnotationEntry {
  final ExecutableElement element;
  final AnnotationValue value;

  AnnotationEntry(ExecutableElement element, AnnotationValue value) {
    this.element = element;
    this.value = value;
  }

  ExecutableElement element() {
    return element;
  }

  AnnotationValue value() {
    return value;
  }

  @Override
  public String toString() {
    return element.toString();
  }
}
