package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;

class TableCodeGenerator extends CodeGenerator {
  TableCodeGenerator(Context context, String tablePackage) {
    super(context, tablePackage);
  }

  @Override
  ClassName formatName(String packageName, SchemaElement schemaElement) {
    return ClassName.get(
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
        .addMethod(newSerializer(schemaElement))
        .addMethod(offset(schemaElement))
        .addMethod(indexed(schemaElement))
        .addMethod(primaryKey(schemaElement))
        ;
  }

  MethodSpec constructor(SchemaElement schemaElement) {
    Schema config = schemaElement.annotation;
    return MethodSpec
        .constructorBuilder()
        .addStatement("super($T.class, $L, $Lf)", // does not append "f" automatically for float literals
            schemaElement.element, config.initialCapacity(), config.loadFactor())
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

  MethodSpec newSerializer(SchemaElement schemaElement) {
    TableElement tableElement = schemaElement.tableElement;
    SerializerElement serializerElement = schemaElement.serializerElement;
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
