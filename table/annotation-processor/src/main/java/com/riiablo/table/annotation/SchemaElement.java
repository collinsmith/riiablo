package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class SchemaElement {
  static SchemaElement get(final Context context, Element element) {
    TypeElement typeElement = (TypeElement) element;
    Set<Modifier> modifiers = typeElement.getModifiers();
    if (!modifiers.contains(Modifier.PUBLIC)) {
      context.error(typeElement, "{element} must be declared {}", Modifier.PUBLIC);
      return null;
    } else if (modifiers.contains(Modifier.ABSTRACT)) {
      context.error(typeElement, "{element} must be a concrete type");
      return null;
    }

      ExecutableElement defaultConstructor = defaultConstructor(context, typeElement);
    if (defaultConstructor == null) {
      context.error(typeElement, "{element} must contain a default constructor");
      return null;
    } else if (!defaultConstructor.getModifiers().contains(Modifier.PUBLIC)) {
      context.error(defaultConstructor, "{element} must be declared {}", Modifier.PUBLIC);
      return null;
    }

    Collection<FieldElement> fields = collectFieldElements(context, typeElement);

    final FieldElement primaryKeyFieldElement;
    Collection<FieldElement> primaryKeys = CollectionUtils.select(fields, new Predicate<FieldElement>() {
      @Override
      public boolean evaluate(FieldElement e) {
        return e.primaryKeyElement != null;
      }
    });
    if (primaryKeys.size() >= 1) {
      Iterator<FieldElement> it = primaryKeys.iterator();
      primaryKeyFieldElement = it.next();
      for (FieldElement e : IteratorUtils.asIterable(it)) {
        context.warn(e.element, e.primaryKeyElement.mirror,
            "{} already declared as {}",
            primaryKeyFieldElement, PrimaryKey.class);
      }
    } else {
      primaryKeyFieldElement = FieldElement.firstPrimaryKey(fields);
      if (primaryKeyFieldElement == null) {
        context.error(element, "{element} did not declare any valid {}", PrimaryKey.class);
        return null;
      }

      context.warn(element,
          "{element} did not declare any valid {}, using {}",
          PrimaryKey.class, primaryKeyFieldElement);
    }

    TableElement tableElement = TableElement.get(context, typeElement);
    SerializerElement serializerElement = SerializerElement.get(context, typeElement);

    return new SchemaElement(
        typeElement,
        tableElement,
        serializerElement,
        primaryKeyFieldElement,
        fields);
  }

  static ExecutableElement defaultConstructor(
      Context context,
      TypeElement typeElement
  ) {
    for (Element e : typeElement.getEnclosedElements()) {
      switch (e.getKind()) {
        case CONSTRUCTOR:
          ExecutableElement constructor = (ExecutableElement) e;
          if (constructor.getParameters().isEmpty()) {
            return constructor;
          }
          break;
      }
    }

    return null;
  }

  static Collection<FieldElement> collectFieldElements(Context context, TypeElement typeElement) {
    Collection<FieldElement> fields = new ArrayList<>();
    TypeElement superclassElement = typeElement;
    for (;;) {
      for (Element e : superclassElement.getEnclosedElements()) {
        switch (e.getKind()) {
          case FIELD:
            FieldElement field = FieldElement.get(context, (VariableElement) e);
            if (field != null) fields.add(field);
            break;
        }
      }

      TypeMirror superclassMirror = superclassElement.getSuperclass();
      superclassElement = (TypeElement) context.typeUtils.asElement(superclassMirror);
      if (ClassName.OBJECT.equals(ClassName.get(superclassMirror))) {
        break;
      }
    }

    return fields;
  }

  final TypeElement element;
  final TableElement tableElement;
  final SerializerElement serializerElement;
  final FieldElement primaryKeyFieldElement;
  final Collection<FieldElement> fields;

  SchemaElement(
      TypeElement element,
      TableElement tableElement,
      SerializerElement serializerElement,
      FieldElement primaryKeyFieldElement,
      Collection<FieldElement> fields) {
    this.element = element;
    this.tableElement = tableElement;
    this.serializerElement = serializerElement;
    this.primaryKeyFieldElement = primaryKeyFieldElement;
    this.fields = fields;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("element", element)
        .append("tableElement", tableElement)
        .append("serializerElement", serializerElement)
        .append("primaryKeyFieldElement", primaryKeyFieldElement)
        .toString();
  }
}
