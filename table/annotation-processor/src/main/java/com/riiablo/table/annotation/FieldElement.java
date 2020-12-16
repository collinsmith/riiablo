package com.riiablo.table.annotation;

import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

final class FieldElement {
  static FieldElement get(Context context, VariableElement element) {
    FormatElement formatElement = FormatElement.get(context, element);
    PrimaryKeyElement primaryKeyElement = PrimaryKeyElement.get(context, element);
    Set<Modifier> modifiers = element.getModifiers();
    if (!modifiers.contains(Modifier.PUBLIC)) {
      context.warn(element, "record fields should be declared {}", Modifier.PUBLIC);
      return null;
    }
    if (modifiers.contains(Modifier.FINAL)) {
      context.error(element, "record fields cannot be {}", Modifier.FINAL);
      return null;
    }
    if (Constants.isReserved(element.getSimpleName())) {
      context.error(element, "'{}' is an illegal record field name", Constants.RESERVED_NAME);
      return null;
    }
    return new FieldElement(element, formatElement, primaryKeyElement);
  }

  static FieldElement firstPrimaryKey(Collection<FieldElement> fields) {
    for (FieldElement field : fields) {
      if (field.primaryKeyElement != null || Constants.isPrimaryKey(field.element)) {
        return field;
      }
    }

    return null;
  }

  final VariableElement element;
  final TypeMirror mirror;
  final FormatElement formatElement;
  final PrimaryKeyElement primaryKeyElement;

  FieldElement(VariableElement element, FormatElement formatElement, PrimaryKeyElement primaryKeyElement) {
    this.element = element;
    this.mirror = element.asType();
    this.formatElement = formatElement;
    this.primaryKeyElement = primaryKeyElement;
  }

  Name name() {
    return element.getSimpleName();
  }

  boolean isArray() {
    return mirror.getKind() == TypeKind.ARRAY;
  }

  boolean isPrimitive() {
    return mirror.getKind().isPrimitive();
  }

  TypeMirror element() {
    return mirror;
  }

  TypeMirror componentType() {
    return ((ArrayType) mirror).getComponentType();
  }

  @Override
  public String toString() {
    return element.toString();
  }
}
