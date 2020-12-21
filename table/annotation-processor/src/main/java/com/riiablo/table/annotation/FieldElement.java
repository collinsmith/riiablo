package com.riiablo.table.annotation;

import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.ArrayUtils;

final class FieldElement {
  static FieldElement get(Context context, VariableElement element) {
    FormatElement formatElement = FormatElement.get(context, element);
    PrimaryKeyElement primaryKeyElement = PrimaryKeyElement.get(context, element);
    ForeignKeyElement foreignKeyElement = ForeignKeyElement.get(context, element);
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL)) {
      return null;
    }
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
    if (foreignKeyElement == null && !Constants.isRecordFieldType(element)) {
      context.error(element, "{element} is not a supported record field type");
    }
    return new FieldElement(element, formatElement, primaryKeyElement, foreignKeyElement);
  }

  static FieldElement firstPrimaryKey(Collection<FieldElement> fields) {
    for (FieldElement field : fields) {
      if (field.primaryKeyElement != null || Constants.isPrimaryKeyType(field.element)) {
        return field;
      }
    }

    return null;
  }

  final VariableElement element;
  final TypeMirror mirror;
  final FormatElement formatElement;
  final PrimaryKeyElement primaryKeyElement;
  final ForeignKeyElement foreignKeyElement;
  final String[] fieldNames;

  FieldElement(
      VariableElement element,
      FormatElement formatElement,
      PrimaryKeyElement primaryKeyElement,
      ForeignKeyElement foreignKeyElement) {
    this.element = element;
    this.mirror = element.asType();
    this.formatElement = formatElement;
    this.primaryKeyElement = primaryKeyElement;
    this.foreignKeyElement = foreignKeyElement;
    fieldNames = formatElement != null
        ? formatElement.fieldNames
        : ArrayUtils.toArray(element.getSimpleName().toString());
  }

  Name name() {
    return element.getSimpleName();
  }

  boolean isPrimaryKey() {
    return primaryKeyElement != null;
  }

  boolean isForeignKey() {
    return foreignKeyElement != null;
  }

  boolean isTransient() {
    return element.getModifiers().contains(Modifier.TRANSIENT);
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
