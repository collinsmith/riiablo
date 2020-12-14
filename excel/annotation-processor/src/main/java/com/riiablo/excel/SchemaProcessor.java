package com.riiablo.excel;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.riiablo.excel.annotation.Schema;

@AutoService(Processor.class)
public class SchemaProcessor extends AbstractProcessor {
  static final Set<String> SUPPORTED_ANNOTATIONS;
  static {
    Set<String> set = new LinkedHashSet<>();
    set.add(Schema.class.getCanonicalName());
    SUPPORTED_ANNOTATIONS = set;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Messager messager = processingEnv.getMessager();
    SerializerGenerator serializerGenerator = new SerializerGenerator(
        processingEnv, SchemaProcessor.class, "com.riiablo.excel.serializer");
    TableGenerator tableGenerator = new TableGenerator(
        processingEnv, SchemaProcessor.class, "com.riiablo.excel.table");

    for (Element element : roundEnv.getElementsAnnotatedWith(Schema.class)) {
      if (element.getKind() != ElementKind.CLASS) {
        messager.printMessage(Diagnostic.Kind.ERROR,
            "Only classes can be annotated with " + Schema.class,
            element);
        continue;
      }

      SchemaAnnotatedElement schema = SchemaAnnotatedElement.get(element);
      TypeElement schemaElement = schema.element;

      TableAnnotatedElement table = TableAnnotatedElement.get(schemaElement);
      if (table == null) {
        try {
          JavaFile file = tableGenerator.generateFile(schema);
          // file.writeTo(System.out);
          file.writeTo(processingEnv.getFiler());
        } catch (GenerationException t) {
          t.printMessage(messager);
          continue;
        } catch (Throwable t) {
          messager.printMessage(Diagnostic.Kind.ERROR, t.getMessage(), element);
        }
      }

      SerializedWithAnnotatedElement serializedWith = SerializedWithAnnotatedElement.get(schemaElement);
      if (serializedWith == null) {
        try {
          JavaFile file = serializerGenerator.generateFile(schema);
          // file.writeTo(System.out);
          file.writeTo(processingEnv.getFiler());
        } catch (GenerationException t) {
          t.printMessage(messager);
          continue;
        } catch (Throwable t) {
          messager.printMessage(Diagnostic.Kind.ERROR, t.getMessage(), element);
        }
      }
    }

    return true;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return SUPPORTED_ANNOTATIONS;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
