package com.riiablo.table.annotation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.riiablo.table.Manifest;
import com.riiablo.table.Table;

import static com.riiablo.table.annotation.Constants.FOREIGN_KEY;
import static com.riiablo.table.annotation.Constants.MANIFEST;
import static com.riiablo.table.annotation.Constants.PRIMARY_KEY;
import static com.riiablo.table.annotation.Constants.PRIMARY_KEY_TYPES;

@AutoService(Processor.class)
public class SchemaProcessor extends AbstractProcessor {
  private final List<SchemaElement> schemas = new ArrayList<>();
  private final Map<ClassName, FieldSpec> tables = new HashMap<>();
  private final Set<TypeMirror> tableTypes = new HashSet<>();
  private Context context;

  @Override
  public synchronized void init(ProcessingEnvironment p) {
    super.init(p);
    Validate.validState(context == null, "context already configured");
    context = new Context(p);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment r) {
    if (r.processingOver()) {
      generateManifest();
    } else {
      processPrimaryKeyAnnotations(r);
      processSchemaAnnotations(r);
      processForeignKeyAnnotations(r);
    }

    return true;
  }

  private void processPrimaryKeyAnnotations(RoundEnvironment r) {
    for (Element element : r.getElementsAnnotatedWith(PrimaryKey.class)) {
      AnnotationMirror annotationMirror = context.getAnnotationMirror(element, PRIMARY_KEY);
      if (element.getKind() != ElementKind.FIELD) {
        context.error(element, annotationMirror,
            "{} can only be applied to fields",
            PrimaryKey.class);
      }

      if (!Constants.isPrimaryKeyType(element)) {
        context.error(element, annotationMirror,
            "{} must be one of {}",
            PrimaryKey.class, PRIMARY_KEY_TYPES);
      }
    }
  }

  private void processForeignKeyAnnotations(RoundEnvironment r) {
    for (Element element : r.getElementsAnnotatedWith(ForeignKey.class)) {
      AnnotationMirror annotationMirror = context.getAnnotationMirror(element, FOREIGN_KEY);
      if (element.getKind() != ElementKind.FIELD) {
        context.error(element, annotationMirror,
            "{} can only be applied to fields",
            ForeignKey.class);
      }

      // validates that foreign key field type matches an existing table type
      TypeMirror mirror = element.asType();
      if (!tableTypes.contains(mirror)) {
        context.error(element, annotationMirror,
            "cannot locate table of type {} for {element}",
            mirror);
      }

      // finds schema element of this foreign key element
      SchemaElement schemaElement = null;
      ForeignKeyElement foreignKey = null;
finder:
      for (SchemaElement e : schemas) {
        for (FieldElement f : e.fields) {
          if (f.element == element) {
            schemaElement = e;
            foreignKey = f.foreignKeyElement;
            break finder;
          }
        }
      }

      // validates that foreign key column name matches an existing column name
      if (schemaElement != null) {
        boolean found = false;
        for (FieldElement f : schemaElement.fields) {
          if (f.name().contentEquals(foreignKey.annotation.value())) {
            found = true;
            break;
          }
        }

        if (!found) {
          context.error(element, foreignKey.mirror, foreignKey.value("value"),
              "{} does not contain any field named '{}'",
              schemaElement.element.getQualifiedName(), foreignKey.annotation.value());
        }
      }
    }
  }

  private void processSchemaAnnotations(RoundEnvironment r) {
    TableCodeGenerator tableCodeGenerator = new TableCodeGenerator(
        context, "com.riiablo.table.table");
    SerializerCodeGenerator serializerCodeGenerator = new SerializerCodeGenerator(
        context, "com.riiablo.table.serializer");
    ParserCodeGenerator parserCodeGenerator = new ParserCodeGenerator(
        context, "com.riiablo.table.parser");
    for (Element element : r.getElementsAnnotatedWith(Schema.class)) {
      if (element.getKind() != ElementKind.CLASS) {
        context.error(element, "{} can only be applied to classes", Schema.class);
        continue;
      }

      SchemaElement schemaElement = SchemaElement.get(context, element);
      if (schemaElement == null) continue;
      if (schemaElement.serializerElement.declaredType != null) {
        try {
          serializerCodeGenerator
              .generate(schemaElement)
              .writeTo(processingEnv.getFiler());
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }

      if (schemaElement.parserElement.declaredType != null) {
        try {
          parserCodeGenerator
              .generate(schemaElement)
              .writeTo(processingEnv.getFiler());
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }

      // Depends on serializerElement to generate Serializer impl
      // Depends on parserElement to generate Parser impl
      // Depends on injectorElement to generate Injector impl
      if (schemaElement.tableElement.declaredType != null) {
        try {
          tableCodeGenerator
              .generate(schemaElement)
              .writeTo(processingEnv.getFiler());
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }

      schemas.add(schemaElement);
      tableTypes.add(schemaElement.element.asType());
    }
  }

  private void generateManifest() {
    try {
      TypeSpec.Builder tableManifest = TypeSpec
          .classBuilder(MANIFEST)
          .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
          .addSuperinterface(Manifest.class)
          .addMethod(MethodSpec
              .constructorBuilder()
              .addModifiers(Modifier.PRIVATE)
              .build())
          ;

      FieldSpec manifest = FieldSpec
          .builder(
              MANIFEST,
              "INSTANCE",
              Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("new $T()", MANIFEST)
          .build();
      tableManifest.addField(manifest);

      for (SchemaElement schema : schemas) {
        ClassName schemaName = ClassName.get(schema.element);
        FieldSpec tableFieldSpec = FieldSpec
            .builder(
                schema.tableClassName,
                schemaName.simpleName().toLowerCase(),
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T($N)", schema.tableClassName, manifest)
            .build();
        tableManifest.addField(tableFieldSpec);
        tables.put(schemaName, tableFieldSpec);
      }

      TypeVariableName R = TypeVariableName.get("R");
      ParameterSpec table = ParameterSpec
          .builder(ParameterizedTypeName.get(ClassName.get(Table.class), R), "table")
          .build();
      ParameterSpec record = ParameterSpec
          .builder(R, "record")
          .build();
      MethodSpec.Builder inject = MethodSpec
          .methodBuilder("inject")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .addTypeVariable(R)
          .addParameter(table)
          .addParameter(record)
          .returns(table.type)
          ;
      inject.beginControlFlow("if ($N == null)", table);
      for (SchemaElement schema : schemas) {
        if  (!schema.requiresInjection()) continue;
        ClassName schemaName = ClassName.get(schema.element);
        inject.nextControlFlow("else if ($N == $N)", table, tables.get(schemaName));
        FieldSpec castedRecord = FieldSpec
            .builder(schemaName, "r", Modifier.FINAL)
            .initializer("($T) $N", schemaName, record)
            .build();
        inject.addCode("$L", castedRecord);
        for (FieldElement field : schema.foreignKeys) {
          FieldSpec fieldSpec = tables.get(ClassName.get(field.element()));
          if (fieldSpec == null) continue;
          inject.addStatement("$N.$N = $T.$N.get($N.$N)",
              castedRecord,
              field.name(),
              MANIFEST,
              fieldSpec,
              castedRecord,
              field.foreignKeyElement.annotation.value());
        }
      }
      // Generate a default clause to throw error if table wasn't found
      // Will require cases for all tables in the manifest to be generated
      // inject.nextControlFlow("else");
      // inject.addStatement("throw new $T($S)", AssertionError.class, "table is not managed by this manifest");
      inject.endControlFlow();
      inject.addStatement("return $N", table);
      tableManifest.addMethod(inject.build());

      JavaFile
          .builder(MANIFEST.packageName(), tableManifest.build()).build()
          .writeTo(processingEnv.getFiler());
    } catch (Throwable t) {
      context.error(ExceptionUtils.getRootCauseMessage(t));
      t.printStackTrace(System.err);
    }
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new LinkedHashSet<>();
    set.add(Schema.class.getCanonicalName());
    set.add(PrimaryKey.class.getCanonicalName());
    set.add(ForeignKey.class.getCanonicalName());
    return SetUtils.unmodifiableSet(set);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
