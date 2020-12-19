package com.riiablo.table.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Map;
import javax.lang.model.element.Modifier;

final class InjectorCodeGenerator extends CodeGenerator {
  final ClassName tableManifest;
  final Map<ClassName, FieldSpec> tables;

  InjectorCodeGenerator(
      Context context,
      String injectorPackage,
      ClassName tableManifest,
      Map<ClassName, FieldSpec> tables) {
    super(context, injectorPackage);
    this.tableManifest = tableManifest;
    this.tables = tables;
  }

  @Override
  ClassName formatName(String packageName, SchemaElement schemaElement) {
    return schemaElement.parserClassName = ClassName.get(
        packageName,
        schemaElement.element.getSimpleName() + Injector.class.getSimpleName());
  }

  @Override
  TypeSpec.Builder newTypeSpec(SchemaElement schemaElement) {
    return super.newTypeSpec(schemaElement)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(ParameterizedTypeName.get(
            ClassName.get(com.riiablo.table.Injector.class),
            ClassName.get(schemaElement.element),
            tableManifest))
        .addMethod(inject(schemaElement))
        ;
  }

  // R inject(Object manifest, R record);
  MethodSpec inject(SchemaElement schemaElement) {
    ClassName schemaName = ClassName.get(schemaElement.element);
    final ParameterSpec manifest = ParameterSpec.builder(tableManifest, "arg0").build();
    final ParameterSpec record = ParameterSpec.builder(schemaName, "arg1").build();
    MethodSpec.Builder method = MethodSpec
        .methodBuilder("inject")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(schemaName)
        .addParameter(manifest)
        .addParameter(record)
        ;

    for (FieldElement field : schemaElement.foreignKeys) {
      FieldSpec fieldSpec = tables.get(ClassName.get(field.element()));
      if (fieldSpec == null) continue;
      method.addStatement("$N.$N = $N.$N.get($N.$N)",
          record,
          field.name(),
          manifest,
          fieldSpec,
          record,
          field.foreignKeyElement.annotation.value());
    }

    method.addStatement("return $N", record);
    return method.build();
  }
}
