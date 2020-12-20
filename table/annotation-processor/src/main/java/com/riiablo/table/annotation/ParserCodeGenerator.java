package com.riiablo.table.annotation;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

import com.riiablo.table.ParserInput;

import static com.riiablo.table.annotation.Constants.STRING;

final class ParserCodeGenerator extends CodeGenerator {
  ParserCodeGenerator(Context context, String parserPackage) {
    super(context, parserPackage);
  }

  @Override
  ClassName formatName(String packageName, SchemaElement schemaElement) {
    return schemaElement.parserClassName = ClassName.get(
        packageName,
        schemaElement.element.getSimpleName() + Parser.class.getSimpleName());
  }

  @Override
  TypeSpec.Builder newTypeSpec(SchemaElement schemaElement) {
    ArrayTypeName fieldIdsTypeName = ArrayTypeName.of(int.class);
    FieldSpec fieldIds = FieldSpec
        .builder(fieldIdsTypeName, "fieldIds", Modifier.FINAL)
        .initializer("new $T[$L]", fieldIdsTypeName.componentType, schemaElement.numFields)
        .build();
    return super.newTypeSpec(schemaElement)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .superclass(schemaElement.parserElement.declaredType)
        .addField(fieldIds)
        .addMethod(constructor(schemaElement))
        .addMethod(parseFields(schemaElement, fieldIds))
        .addMethod(parseRecord(schemaElement, fieldIds))
        ;
  }

  MethodSpec constructor(SchemaElement schemaElement) {
    ParameterSpec parser = ParameterSpec
        .builder(ParserInput.class, "parser")
        .build();
    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(parser)
        .addStatement("super($N)", parser)
        .build();
  }

  MethodSpec parseFields(SchemaElement schemaElement, FieldSpec fieldIds) {
    ParserElement parserElement = schemaElement.parserElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            parserElement.getMethod("_parseFields"),
            parserElement.declaredType,
            context.typeUtils);

    int i = 0;
    final ParameterSpec parser = method.parameters.get(0);
    for (FieldElement field : schemaElement.fields) {
      if (field.isForeignKey()) continue;
      for (String fieldName : field.fieldNames) {
        method.addStatement("$N[$L] = $N.$N($S)", fieldIds, i++, parser, "fieldId", fieldName);
      }
    }

    method.addStatement("return $L", "this");
    return method.build();
  }

  MethodSpec parseRecord(SchemaElement schemaElement, FieldSpec fieldIds) {
    ParserElement parserElement = schemaElement.parserElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            parserElement.getMethod("_parseRecord"),
            parserElement.declaredType,
            context.typeUtils);

    int i = 0;
    final ParameterSpec parser = method.parameters.get(0);
    final ParameterSpec recordId = method.parameters.get(1);
    final ParameterSpec record = method.parameters.get(2);
    for (FieldElement field : schemaElement.fields) {
      if (field.isForeignKey()) continue;
      final TypeName fieldTypeName = TypeName.get(field.element());
      final CodeBlock fqFieldName = qualify(record, field.name());
      if (field.isArray()) {
        final TypeName componentTypeName = TypeName.get(field.componentType());
        final int arraySize = field.fieldNames.length;
        method.addStatement(
            "$L = new $T[$L]", fqFieldName, componentTypeName, arraySize);
        for (int j = 0; j < arraySize; j++, i++) {
          method.addStatement(parseX(parser, componentTypeName,
              CodeBlock.of("$L[$L]", fqFieldName, j), // variable
              recordId, // recordId
              CodeBlock.of("$N[$L]", fieldIds, i))); // fieldId
        }
      } else {
        method.addStatement(parseX(parser, fieldTypeName, fqFieldName, recordId,
            CodeBlock.of("$N[$L]", fieldIds, i++)));
      }
    }

    method.addStatement("return $N", record);
    return method.build();
  }

  static CodeBlock qualify(Object object, Name field) {
    return CodeBlock.of("$N.$N", object, field);
  }

  static CodeBlock parseX(Object parser, TypeName type, Object var, Object recordId, Object fieldId) {
    return CodeBlock.of("$L = $N.$N$L($N, $L)", var, parser, "parse", getIoMethod(type), recordId, fieldId);
  }

  static String getIoMethod(TypeName type) {
    if (type == TypeName.BYTE) {
      return "Byte";
    } else if (type == TypeName.SHORT) {
      return "Short";
    } else if (type == TypeName.INT) {
      return "Int";
    } else if (type == TypeName.LONG) {
      return "Long";
    } else if (type == TypeName.BOOLEAN) {
      return "Boolean";
    } else if (STRING.equals(type)) {
      return "String";
    } else {
      throw new UnsupportedOperationException(type + " is not supported!");
    }
  }
}
