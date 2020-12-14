package com.riiablo.excel;

import java.util.Objects;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class GenerationException extends RuntimeException {
  final CharSequence msg;
  final Element e;
  final AnnotationMirror a;
  final AnnotationValue v;

  Diagnostic.Kind kind = Diagnostic.Kind.ERROR;

  GenerationException(CharSequence msg) {
    this(msg, null, null, null);
  }

  GenerationException(CharSequence msg, Element e) {
    this(msg, e, null, null);
  }

  GenerationException(CharSequence msg, Element e, AnnotationMirror a) {
    this(msg, e, a, null);
  }

  GenerationException(CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
    super(Objects.toString(msg));
    this.msg = msg;
    this.e = e;
    this.a = a;
    this.v = v;
  }

  GenerationException kind(Diagnostic.Kind kind) {
    this.kind = kind;
    return this;
  }

  void printMessage(Messager messager) {
    messager.printMessage(kind, msg, e, a, v);
  }
}
