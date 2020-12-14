package com.riiablo.excel;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.riiablo.excel.annotation.Schema;

public class SchemaAnnotatedElement {
  static SchemaAnnotatedElement get(Element element) {
    Schema annotation = element.getAnnotation(Schema.class);
    return new SchemaAnnotatedElement(element, annotation);
  }

  Schema annotation;
  TypeElement element;
  ClassName name;

  SchemaAnnotatedElement(Element element, Schema annotation) {
    this((TypeElement) element, annotation);
  }

  SchemaAnnotatedElement(TypeElement element, Schema annotation) {
    this.element = element;
    this.annotation = annotation;
    this.name = ClassName.get(element);
  }
}
