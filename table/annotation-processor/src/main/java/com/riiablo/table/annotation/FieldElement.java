package com.riiablo.table.annotation;

import java.util.Collection;
import javax.lang.model.element.VariableElement;

final class FieldElement {
  static FieldElement get(Context context, VariableElement element) {
    FormatElement formatElement = FormatElement.get(context, element);
    PrimaryKeyElement primaryKeyElement = PrimaryKeyElement.get(context, element);

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
  final FormatElement formatElement;
  final PrimaryKeyElement primaryKeyElement;

  FieldElement(VariableElement element, FormatElement formatElement, PrimaryKeyElement primaryKeyElement) {
    this.element = element;
    this.formatElement = formatElement;
    this.primaryKeyElement = primaryKeyElement;
  }

  @Override
  public String toString() {
    return element.toString();
  }
}
