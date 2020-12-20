package com.riiablo.table.annotation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
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

import static com.riiablo.table.annotation.Constants.FOREIGN_KEY;
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
      ClassName tableManifest = generateManifest();
      if (tableManifest != null) {
        generateInjectors(tableManifest);
      }
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

  private ClassName generateManifest() {
    try {
      ClassName manifestName = ClassName.get("com.riiablo.table", "TableManifest");
      TypeSpec.Builder tableManifest = TypeSpec
          .classBuilder(manifestName)
          .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
          .addMethod(MethodSpec
              .constructorBuilder()
              .addModifiers(Modifier.PRIVATE)
              .build())
          ;

      for (SchemaElement schema : schemas) {
        ClassName schemaName = ClassName.get(schema.element);
        FieldSpec tableFieldSpec = FieldSpec
            .builder(
                schema.tableClassName,
                schemaName.simpleName().toLowerCase(),
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T()", schema.tableClassName)
            .build();
        tableManifest.addField(tableFieldSpec);
        tables.put(schemaName, tableFieldSpec);
      }

      JavaFile
          .builder(manifestName.packageName(), tableManifest.build()).build()
          .writeTo(processingEnv.getFiler());
      return manifestName;
    } catch (Throwable t) {
      context.error(ExceptionUtils.getRootCauseMessage(t));
      t.printStackTrace(System.err);
      return null;
    }
  }

  private void generateInjectors(ClassName tableManifest) {
    InjectorCodeGenerator injectorCodeGenerator = new InjectorCodeGenerator(
        context, "com.riiablo.table.injector", tableManifest, tables);
    for (SchemaElement schemaElement : schemas) {
      if (schemaElement.foreignKeys.isEmpty()) continue;
      if (schemaElement.parserElement.declaredType != null) {
        try {
          injectorCodeGenerator
              .generate(schemaElement)
              .writeTo(processingEnv.getFiler());
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }
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
