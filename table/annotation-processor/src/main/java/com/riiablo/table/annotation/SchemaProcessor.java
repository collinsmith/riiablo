package com.riiablo.table.annotation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

@AutoService(Processor.class)
public class SchemaProcessor extends AbstractProcessor {
  private final Set<String> schemas = new HashSet<>();
  private Context context;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    context = new Context(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      generateManifest();
    } else {
      processAnnotations(annotations, roundEnv);
    }

    return true;
  }

  private void processAnnotations(
      Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv) {

    for (Element element : roundEnv.getElementsAnnotatedWith(PrimaryKey.class)) {
      VariableElement variableElement = (VariableElement) element;
      if (!Constants.isPrimaryKey(variableElement)) {
        context.error(variableElement, "{} must be one of {}",
            PrimaryKey.class, Constants.PRIMARY_KEY_TYPES);
      }
    }

    TableCodeGenerator tableCodeGenerator = new TableCodeGenerator(
        context, "com.riiablo.table.table");
    SerializerCodeGenerator serializerCodeGenerator = new SerializerCodeGenerator(
        context, "com.riiablo.table.serializer");
    ParserCodeGenerator parserCodeGenerator = new ParserCodeGenerator(
        context, "com.riiablo.table.parser");
    for (Element element : roundEnv.getElementsAnnotatedWith(Schema.class)) {
      if (element.getKind() != ElementKind.CLASS) {
        context.error(element, "{} can only be applied to classes", Schema.class);
        continue;
      }

      SchemaElement schemaElement = SchemaElement.get(context, element);
      if (schemaElement == null) continue;
      if (schemaElement.serializerElement.declaredType != null) {
        try {
          serializerCodeGenerator.generate(schemaElement)
              .writeTo(processingEnv.getFiler());
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }

      if (schemaElement.parserElement.declaredType != null) {
        try {
          parserCodeGenerator.generate(schemaElement)
              .writeTo(processingEnv.getFiler());
              // .writeTo(System.out);
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }

      // Depends on serializerElement to generate Serializer impl
      // Depends on parserElement to generate Parser impl
      if (schemaElement.tableElement.declaredType != null) {
        try {
          tableCodeGenerator.generate(schemaElement)
              .writeTo(processingEnv.getFiler());
              // .writeTo(System.out);
        } catch (Throwable t) {
          context.error(ExceptionUtils.getRootCauseMessage(t));
          t.printStackTrace(System.err);
        }
      }

      schemas.add(CodeBlock.of("$S", schemaElement.element).toString());
    }
  }

  private void generateManifest() {
    try {
      JavaFile.builder("com.riiablo.table",
          TypeSpec
              .classBuilder("TableManifest")
              .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
              .addMethod(MethodSpec
                  .constructorBuilder()
                  .addModifiers(Modifier.PRIVATE)
                  .build())
              .addMethod(MethodSpec
                  .methodBuilder("tables")
                  .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                  .returns(ArrayTypeName.of(String.class))
                  .addStatement("return new String[] {\n$L\n}", StringUtils
                      .join(schemas, ",\n"))
                  .build())
              .build())
          .build()
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
    return SetUtils.unmodifiableSet(set);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
