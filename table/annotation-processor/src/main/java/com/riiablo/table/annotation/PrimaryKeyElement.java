package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

class PrimaryKeyElement {
  static PrimaryKeyElement find(
      Context context,
      TypeElement element,
      Collection<VariableElement> elements
  ) {
    VariableElement firstAcceptableElement = null, primaryKeyElement = null;
    for (VariableElement e : elements) {
      if (firstAcceptableElement == null
          && ArrayUtils.contains(Constants.PRIMARY_KEY_TYPES, ClassName.get(e.asType()))) {
        firstAcceptableElement = e;
      }

      PrimaryKey annotation = e.getAnnotation(PrimaryKey.class);
      if (annotation != null) {
        if (primaryKeyElement == null) {
          primaryKeyElement = e;
        } else {
          context.error(e, "{} already declared as {}", primaryKeyElement, PrimaryKey.class);
        }
      }
    }

    if (primaryKeyElement == null) {
      if (firstAcceptableElement == null) {
        context.error(element, "{element} did not declare any {}", PrimaryKey.class);
        return null;
      }

      context.warn(element, "{element} did not declare any {}, using {}",
          PrimaryKey.class, firstAcceptableElement);
      primaryKeyElement = firstAcceptableElement;
    }

    return new PrimaryKeyElement(primaryKeyElement);
  }

  final VariableElement element;

  PrimaryKeyElement(VariableElement element) {
    this.element = element;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("element", element)
        .toString();
  }
}
