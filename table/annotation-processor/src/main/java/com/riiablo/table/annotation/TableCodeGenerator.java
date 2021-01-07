package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;

import com.riiablo.table.Manifest;

class TableCodeGenerator extends CodeGenerator {
  TableCodeGenerator(Context context, String tablePackage) {
    super(context, tablePackage);
  }

  @Override
  ClassName formatName(String packageName, SchemaElement schemaElement) {
    return schemaElement.tableClassName = ClassName.get(
        packageName,
        schemaElement.element.getSimpleName() + Table.class.getSimpleName());
  }

  @Override
  TypeSpec.Builder newTypeSpec(SchemaElement schemaElement) {
    return super.newTypeSpec(schemaElement)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .superclass(schemaElement.tableElement.declaredType)
        .addMethod(constructor(schemaElement))
        .addMethod(newRecord(schemaElement))
        .addMethod(newParser(schemaElement))
        .addMethod(newSerializer(schemaElement))
        .addMethod(offset(schemaElement))
        .addMethod(indexed(schemaElement))
        .addMethod(preload(schemaElement))
        .addMethod(primaryKey(schemaElement))
        ;
  }

  MethodSpec constructor(SchemaElement schemaElement) {
    Schema config = schemaElement.annotation;
    final boolean stringLookup;
    if (config.indexed()) {
      stringLookup = false;
    } else {
      TypeName primaryKeyType = ClassName.get(schemaElement.primaryKeyFieldElement.element());
      stringLookup = Constants.STRING.equals(primaryKeyType);
    }

    ParameterSpec manifest = ParameterSpec
        .builder(Manifest.class, "manifest")
        .build();
    return MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(manifest)
        .addStatement("super($N, $T.class, $L, $Lf, $L)", // does not append "f" automatically for float literals
            manifest, schemaElement.element, config.initialCapacity(), config.loadFactor(), stringLookup)
        .build();
  }

  MethodSpec newRecord(SchemaElement schemaElement) {
    TableElement tableElement = schemaElement.tableElement;
    return MethodSpec
        .overriding(
            tableElement.getMethod("newRecord"),
            tableElement.declaredType,
            context.typeUtils)
        .addStatement("return new $T()", schemaElement.element)
        .build();
  }

  MethodSpec newParser(SchemaElement schemaElement) {
    TableElement tableElement = schemaElement.tableElement;
    MethodSpec.Builder method = MethodSpec
        .overriding(
            tableElement.getMethod("newParser"),
            tableElement.declaredType,
            context.typeUtils);
    final ParameterSpec parser = method.parameters.get(0);
    method.addStatement("return new $T($N)", schemaElement.parserClassName, parser);
    return method.build();
  }

  MethodSpec newSerializer(SchemaElement schemaElement) {
    TableElement tableElement = schemaElement.tableElement;
    return MethodSpec
        .overriding(
            tableElement.getMethod("newSerializer"),
            tableElement.declaredType,
            context.typeUtils)
        .addStatement("return new $T()", schemaElement.serializerClassName)
        .build();
  }

  MethodSpec offset(SchemaElement schemaElement) {
    Schema config = schemaElement.annotation;
    TableElement tableElement = schemaElement.tableElement;
    return MethodSpec
        .overriding(
            tableElement.getMethod("offset"),
            tableElement.declaredType,
            context.typeUtils)
        .addStatement("return $L", config.offset())
        .build();
  }

  MethodSpec indexed(SchemaElement schemaElement) {
    Schema config = schemaElement.annotation;
    TableElement tableElement = schemaElement.tableElement;
    return MethodSpec
        .overriding(
            tableElement.getMethod("indexed"),
            tableElement.declaredType,
            context.typeUtils)
        .addStatement("return $L", config.indexed())
        .build();
  }

  MethodSpec preload(SchemaElement schemaElement) {
    Schema config = schemaElement.annotation;
    TableElement tableElement = schemaElement.tableElement;
    return MethodSpec
        .overriding(
            tableElement.getMethod("preload"),
            tableElement.declaredType,
            context.typeUtils)
        .addStatement("return $L", config.preload())
        .build();
  }

  MethodSpec primaryKey(SchemaElement schemaElement) {
    Schema config = schemaElement.annotation;
    TableElement tableElement = schemaElement.tableElement;
    return MethodSpec
        .overriding(
            tableElement.getMethod("primaryKey"),
            tableElement.declaredType,
            context.typeUtils)
        .addStatement("return $L", config.indexed()
            ? null
            : CodeBlock.of("$S", schemaElement.primaryKeyFieldElement.element))
        .build();
  }
}
