package com.riiablo.table.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.ArrayUtils;

final class FormatElement extends AnnotationElement<Format> {
  static FormatElement get(Context context, VariableElement element) {
    Format annotation = element.getAnnotation(Format.class);
    if (annotation == null) return null;
    AnnotationMirror mirror = context.getAnnotationMirror(element, Constants.FORMAT);
    String[] fieldNames = formatFieldNames(context, element, annotation, mirror);
    return new FormatElement(context, annotation, mirror, fieldNames);
  }

  static void checkDeclaredArrayLength(
      Context context,
      VariableElement element,
      AnnotationMirror annotationMirror,
      int length) {
    TypeMirror mirror = element.asType();
    if (mirror.getKind() != TypeKind.ARRAY && length > 1) {
      context.error(element, annotationMirror,
          "{element} corresponds to multiple fields but is not an array {}",
          context.typeUtils.getArrayType(mirror));
    }
  }

  static String[] formatFieldNames(
      Context context,
      VariableElement element,
      Format annotation,
      AnnotationMirror mirror) {
    final String format = annotation.format();
    final String[] values = annotation.values();
    final int startIndex = annotation.startIndex();
    final int endIndex = annotation.endIndex();
    final int columnIndex = annotation.columnIndex();
    if (columnIndex >= 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    } else if (format.isEmpty()) {
      final String fieldName = element.getSimpleName().toString();
      if (values.length > 0) {
        // values[] used as literal column names
        checkDeclaredArrayLength(context, element, mirror, values.length);
        String[] fieldNames = new String[values.length];
        for (int i = 0; i < values.length; i++) {
          fieldNames[i] = values[i];
        }

        return fieldNames;
      } else if (startIndex == 0 && endIndex == 0) {
        // field name used as literal column name
        return ArrayUtils.toArray(fieldName);
      } else {
        // field name + indexes used as column names
        checkDeclaredArrayLength(context, element, mirror, endIndex - startIndex);
        String[] fieldNames = new String[endIndex - startIndex];
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
          fieldNames[j] = fieldName + i;
        }

        return fieldNames;
      }
    } else {
      if (startIndex == 0 && endIndex == 0) {
        // format used as literal column name
        return ArrayUtils.toArray(format);
      } else {
        checkDeclaredArrayLength(context, element, mirror, endIndex - startIndex);
        String[] fieldNames = new String[endIndex - startIndex];
        if (values.length == 0) {
          // format used in conjunction with indexes as column names
          // format must contain %d within it, replaced with indexes
          for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            fieldNames[j] = String.format(format, i);
          }
        } else {
          // format used in conjunction with values as column names
          // format must contain as many values as indexes
          for (int i = 0, s = values.length; i < s; i++) {
            fieldNames[i] = String.format(format, values[i]);
          }
        }

        return fieldNames;
      }
    }
  }

  final String[] fieldNames;

  FormatElement(Context context, Format annotation, AnnotationMirror mirror, String[] fieldNames) {
    super(context, annotation, mirror);
    this.fieldNames = fieldNames;
  }
}
