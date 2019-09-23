package com.riiablo.codec.excel;

import java.lang.reflect.Field;

public class ExcelGenerationTool {

  public static void main(String[] args) throws Exception {
    for (String arg : args) {
      generate(arg + "$Entry");
    }
  }

  public static void generate(String _package) throws Exception {
    Class clazz = Class.forName(_package);
    Field[] fields = clazz.getFields();
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      Class type = field.getType();
      if (type.isArray()) {
        type = type.getComponentType();
        System.out.printf("for (%s x : %s) out.write%s(x);%n", type.getSimpleName(), field.getName(), getMethod(field));
      } else {
        System.out.printf("out.write%s(%s);%n", getMethod(field), field.getName());
      }
    }

    System.out.println("-----------------------------------");

    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      Class type = field.getType();
      if (type.isArray()) {
        type = type.getComponentType();
        System.out.printf("%s = new %s[%d];%n", field.getName(), type.getSimpleName(), column.endIndex() - column.startIndex());
        System.out.printf("for (int x = %d; x < %d; x++) %s[x] = in.read%s();%n", column.startIndex(), column.endIndex(), field.getName(), getMethod(field));
      } else {
        System.out.printf("%s = in.read%s();%n", field.getName(), getMethod(field));
      }
    }
  }

  private static String getMethod(Field field) {
    Class type = field.getType();
    if (type.isArray()) {
      type = type.getComponentType();
    }

    if (type == String.class) {
      return "UTF";
    } else if (type == byte.class) {
      return "Byte";
    } else if (type == short.class) {
      return "Short";
    } else if (type == int.class) {
      return "Int";
    } else if (type == long.class) {
      return "Long";
    } else if (type == boolean.class) {
      return "Boolean";
    } else {
      throw new UnsupportedOperationException("No support for " + type + " fields");
    }
  }
}
