package com.riiablo.table.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class TableElement {
  static TableElement get(Context context, Element element) {
    Table annotation = element.getAnnotation(Table.class);
    final TypeElement tableElement, tableImplElement;
    if (annotation == null) {
      // Only need tableElement if generating Table impl
      tableElement = context.elementUtils.getTypeElement(com.riiablo.table.Table.class.getCanonicalName());
      tableImplElement = null;
    } else {
      // Only need tableImplElement if @Table present
      tableImplElement = getTableImpl(context, annotation);
      tableElement = null;
    }

    return new TableElement(annotation, tableElement, tableImplElement);
  }

  static TypeElement getTableImpl(Context context, Table annotation) {
    if (annotation == null) return null;
    try {
      Class<?> tableImpl = annotation.value();
      return context.elementUtils.getTypeElement(tableImpl.getCanonicalName());
    } catch (MirroredTypeException t) {
      DeclaredType tableImplMirror = (DeclaredType) t.getTypeMirror();
      return (TypeElement) tableImplMirror.asElement();
    }
  }

  final Table annotation;
  final TypeElement tableElement; // Class<Table>
  final TypeElement tableImplElement; // Class<? extends Table<?>>

  TableElement(
      Table annotation,
      TypeElement tableElement,
      TypeElement tableImplElement) {
    this.annotation = annotation;
    this.tableElement = tableElement;
    this.tableImplElement = tableImplElement;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("annotation", annotation)
        .append("tableElement", tableElement)
        .append("tableImplElement", tableImplElement)
        .toString();
  }
}
