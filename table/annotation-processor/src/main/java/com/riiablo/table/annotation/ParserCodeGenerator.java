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

import static com.riiablo.table.annotation.Constants.STRING;

class ParserCodeGenerator extends CodeGenerator {
  ParserCodeGenerator(Context context, String parserPackage) {
    super(context, parserPackage);
  }

  @Override
  ClassName formatName(String packageName, SchemaElement schemaElement) {
    return schemaElement.parserClassName
        = ClassName.get(
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
        .addSuperinterface(schemaElement.parserElement.declaredType)
        .addField(fieldIds)
        .addMethod(hasNext(schemaElement))
        .addMethod(parseFields(schemaElement, fieldIds))
        .addMethod(parseRecord(schemaElement, fieldIds))
        ;
  }

  MethodSpec hasNext(SchemaElement schemaElement) {
    ParserElement parserElement = schemaElement.parserElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            parserElement.getMethod("hasNext"),
            parserElement.declaredType,
            context.typeUtils);
    final ParameterSpec parser = method.parameters.get(0);
    method.addStatement("return $N.$N() != $L", parser, "cacheLine", -1);
    return method.build();
  }

  MethodSpec parseFields(SchemaElement schemaElement, FieldSpec fieldIds) {
    ParserElement parserElement = schemaElement.parserElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            parserElement.getMethod("parseFields"),
            parserElement.declaredType,
            context.typeUtils);

    int i = 0;
    final ParameterSpec parser = method.parameters.get(0);
    for (FieldElement field : schemaElement.fields) {
      for (String fieldName : field.fieldNames) {
        method.addStatement("$N[$L] = $N.$N($S)", fieldIds, i++, parser, "fieldId", fieldName);
      }
    }

    return method.build();
  }

  MethodSpec parseRecord(SchemaElement schemaElement, FieldSpec fieldIds) {
    ParserElement parserElement = schemaElement.parserElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            parserElement.getMethod("parseRecord"),
            parserElement.declaredType,
            context.typeUtils);

    int i = 0;
    final ParameterSpec record = method.parameters.get(0);
    final ParameterSpec parser = method.parameters.get(1);
    for (FieldElement field : schemaElement.fields) {
      final TypeName fieldTypeName = TypeName.get(field.element());
      final CodeBlock fqFieldName = qualify(record, field.name());
      if (field.isArray()) {
        final TypeName componentTypeName = TypeName.get(field.componentType());
        for (int j = 0, s = field.fieldNames.length; j < s; j++, i++) {
          method.addStatement(parseX(parser, componentTypeName,
              CodeBlock.of("$L[$L]", fqFieldName, j),
              CodeBlock.of("$N[$L]", fieldIds, i)));
        }
      } else {
        method.addStatement(parseX(parser, fieldTypeName, fqFieldName,
            CodeBlock.of("$N[$L]", fieldIds, i++)));
      }
    }

    return method.build();
  }

  static CodeBlock qualify(Object object, Name field) {
    return CodeBlock.of("$N.$N", object, field);
  }

  static CodeBlock parseX(Object parser, TypeName type, Object var, Object fieldId) {
    return CodeBlock.of("$L = $N.$N$L($L)", var, parser, "parse", getIoMethod(type), fieldId);
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
