package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import org.apache.commons.lang3.Validate;

import static com.riiablo.table.annotation.Constants.STRING;

class SerializerCodeGenerator extends CodeGenerator {
  SerializerCodeGenerator(Context context, String serializerPackage) {
    super(context, serializerPackage);
  }

  @Override
  ClassName formatName(String packageName, SchemaElement schemaElement) {
    Validate.validState(schemaElement.serializerClassName == null,
        "schemaElement.serializerClassName already set to " + schemaElement.serializerClassName);
    return schemaElement.serializerClassName
        = ClassName.get(
            packageName,
            schemaElement.element.getSimpleName() + Serializer.class.getSimpleName());
  }

  @Override
  TypeSpec.Builder newTypeSpec(SchemaElement schemaElement) {
    return super.newTypeSpec(schemaElement)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(schemaElement.serializerElement.declaredType)
        .addMethod(readRecord(schemaElement))
        .addMethod(writeRecord(schemaElement))
        .addMethod(equals(schemaElement))
        .addMethod(compare(schemaElement))
        ;
  }

  MethodSpec readRecord(SchemaElement schemaElement) {
    SerializerElement serializerElement = schemaElement.serializerElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            serializerElement.getMethod("readRecord"),
            serializerElement.declaredType,
            context.typeUtils)
        ;

    final ParameterSpec record = method.parameters.get(0);
    final ParameterSpec in = method.parameters.get(1);
    for (FieldElement field : schemaElement.fields) {
      if (field.isTransient()) continue;
      final TypeName fieldTypeName = TypeName.get(field.element());
      final CodeBlock fqFieldName = qualify(record, field.name());
      if (field.isArray()) {
        final String var = Constants.RESERVED_NAME;
        final TypeName componentTypeName = TypeName.get(field.componentType());
        final Format format = field.formatElement.annotation;
        final int arraySize = format.endIndex() - format.startIndex();
        method.addCode(CodeBlock.builder()
            .addStatement(
                "$L = new $T[$L]", fqFieldName, componentTypeName, arraySize)
            .beginControlFlow(
                "for (int $1N = $2L; $1N < $3L; $1N++)", var, 0, arraySize)
            .addStatement(
                readX(in, componentTypeName, CodeBlock.of("$L[$N]", fqFieldName, var)))
            .endControlFlow()
            .build());
      } else {
        method.addCode(CodeBlock.builder()
            .addStatement(readX(in, fieldTypeName, fqFieldName))
            .build());
      }
    }

    return method.build();
  }

  MethodSpec writeRecord(SchemaElement schemaElement) {
    SerializerElement serializerElement = schemaElement.serializerElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            serializerElement.getMethod("writeRecord"),
            serializerElement.declaredType,
            context.typeUtils)
        ;

    final ParameterSpec record = method.parameters.get(0);
    final ParameterSpec out = method.parameters.get(1);
    for (FieldElement field : schemaElement.fields) {
      if (field.isTransient()) continue;
      final TypeName fieldTypeName = TypeName.get(field.element());
      final CodeBlock fqFieldName = qualify(record, field.name());
      if (field.isArray()) {
        final String var = Constants.RESERVED_NAME;
        final TypeName componentTypeName = TypeName.get(field.componentType());
        method.addCode(CodeBlock.builder()
            .beginControlFlow(
                "for ($T $N : $L)", componentTypeName, var, fqFieldName)
            .addStatement(STRING.equals(componentTypeName)
                ? writeX(out, componentTypeName, defaultString(var))
                : writeX(out, componentTypeName, var))
            .endControlFlow()
            .build());
      } else {
        method.addCode(CodeBlock.builder()
            .addStatement(STRING.equals(fieldTypeName)
                ? writeX(out, fieldTypeName, defaultString(fqFieldName))
                : writeX(out, fieldTypeName, fqFieldName))
            .build());
      }
    }

    return method.build();
  }

  MethodSpec equals(SchemaElement schemaElement) {
    SerializerElement serializerElement = schemaElement.serializerElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            serializerElement.getMethod("equals"),
            serializerElement.declaredType,
            context.typeUtils)
        ;

    final ParameterSpec e1 = method.parameters.get(0);
    final ParameterSpec e2 = method.parameters.get(1);
    for (FieldElement field : schemaElement.fields) {
      if (field.isTransient()) continue;
      final Name fieldName = field.name();
      final CodeBlock e1FqFieldName = qualify(e1, fieldName);
      final CodeBlock e2FqFieldName = qualify(e2, fieldName);
      final CodeBlock.Builder block = CodeBlock.builder();
      if (field.isPrimitive()) {
        block.beginControlFlow("if ($L != $L)", e1FqFieldName, e2FqFieldName);
      } else {
        block.beginControlFlow("if (!$L)",
            equalsX(
                field.isArray() ? Arrays.class : Objects.class,
                e1FqFieldName,
                e2FqFieldName));
      }

      method.addCode(block
          .addStatement("return false")
          .endControlFlow()
          .build());
    }

    method.addStatement("return true");
    return method.build();
  }

  MethodSpec compare(SchemaElement schemaElement) {
    SerializerElement serializerElement = schemaElement.serializerElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            serializerElement.getMethod("compare"),
            serializerElement.declaredType,
            context.typeUtils)
        ;

    ParameterSpec e1 = method.parameters.get(0);
    ParameterSpec e2 = method.parameters.get(1);

    TypeName mismatchesTypeName = ParameterizedTypeName.get(ArrayList.class, Throwable.class);
    FieldSpec mismatches = FieldSpec
        .builder(mismatchesTypeName, "mismatches")
        .initializer("new $T()", mismatchesTypeName)
        .build();
    method.addStatement("$T $N = $L", mismatches.type, mismatches, mismatches.initializer);

    for (FieldElement field : schemaElement.fields) {
      if (field.isTransient()) continue;
      final Name fieldName = field.name();
      final CodeBlock e1FqFieldName = qualify(e1, fieldName);
      final CodeBlock e2FqFieldName = qualify(e2, fieldName);
      final CodeBlock.Builder block = CodeBlock.builder();
      if (field.isPrimitive()) {
        block.beginControlFlow("if ($L != $L)", e1FqFieldName, e2FqFieldName);
      } else {
        block.beginControlFlow("if (!$L)",
            equalsX(
                field.isArray() ? Arrays.class : Objects.class,
                e1FqFieldName,
                e2FqFieldName));
      }

      method.addCode(block
          .addStatement(logX(mismatches, format(
              CodeBlock.of("$L does not match: $N=%s, $N=%s", fieldName, e1, e2),
              CodeBlock.of("$L, $L", e1FqFieldName, e2FqFieldName))))
          .endControlFlow()
          .build());
    }

    method.addStatement("return $N", mismatches);
    return method.build();
  }

  static CodeBlock qualify(Object object, Name field) {
    return CodeBlock.of("$N.$N", object, field);
  }

  static CodeBlock readX(Object in, TypeName type, Object var) {
    return CodeBlock.of("$L = $N.$N$L()", var, in, "read", getIoMethod(type));
  }

  static CodeBlock writeX(Object out, TypeName type, Object var) {
    return CodeBlock.of("$N.$N$L($L)", out, "write", getIoMethod(type), var);
  }

  static CodeBlock equalsX(Type type, Object obj1, Object obj2) {
    return CodeBlock.of("$T.equals($L, $L)", type, obj1, obj2);
  }

  static CodeBlock logX(Object collection, Object message) {
    return CodeBlock.of("$N.$N(new $T($L))", collection, "add", RuntimeException.class, message);
  }

  static CodeBlock format(Object format, Object args) {
    return CodeBlock.of("$T.$N($S, $L)", String.class, "format", format, args);
  }

  static CodeBlock defaultString(Object var) {
    // return CodeBlock.of("$T.$N($L)", StringUtils.class, "defaultString", var);
    return CodeBlock.of("$1L == null ? $2S : $1L", var, "");
  }

  static String getIoMethod(TypeName type) {
    if (type == TypeName.BYTE) {
      return "8";
    } else if (type == TypeName.SHORT) {
      return "16";
    } else if (type == TypeName.INT) {
      return "32";
    } else if (type == TypeName.LONG) {
      return "64";
    } else if (type == TypeName.BOOLEAN) {
      return "Boolean";
    } else if (STRING.equals(type)) {
      return "String";
    } else {
      throw new UnsupportedOperationException(type + " is not supported!");
    }
  }
}
