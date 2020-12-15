package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class SchemaElement {
  static SchemaElement get(Context context, Element element) {
    TypeElement typeElement = (TypeElement) element;
    List<VariableElement> columns = collectColumns(context, typeElement);
    PrimaryKeyElement primaryKeyElement = PrimaryKeyElement.find(context, typeElement, columns);

    // for (VariableElement e : columns) {
    //   System.out.println(e);
    // }

    TableElement tableElement = TableElement.get(context, typeElement);
    SerializerElement serializerElement = SerializerElement.get(context, typeElement);

    return new SchemaElement(typeElement, tableElement, serializerElement);
  }

  static List<VariableElement> collectColumns(Context context, TypeElement typeElement) {
    List<VariableElement> columns = new ArrayList<>();
    TypeElement superclassElement = typeElement;
    for (;;) {
      for (Element e : superclassElement.getEnclosedElements()) {
        switch (e.getKind()) {
          case FIELD:
            columns.add((VariableElement) e);
            break;
        }
      }

      TypeMirror superclassMirror = superclassElement.getSuperclass();
      superclassElement = (TypeElement) context.typeUtils.asElement(superclassMirror);
      if (ClassName.OBJECT.equals(ClassName.get(superclassMirror))) {
        break;
      }
    }

    return columns;
  }

  final TypeElement element;
  final TableElement tableElement;
  final SerializerElement serializerElement;

  SchemaElement(
      TypeElement element,
      TableElement tableElement,
      SerializerElement serializerElement) {
    this.element = element;
    this.tableElement = tableElement;
    this.serializerElement = serializerElement;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("element", element)
        .append("tableElement", tableElement)
        .append("serializerElement", serializerElement)
        .toString();
  }
}
