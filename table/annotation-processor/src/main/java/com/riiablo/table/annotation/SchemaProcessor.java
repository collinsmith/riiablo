package com.riiablo.table.annotation;

import com.google.auto.service.AutoService;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.apache.commons.lang3.exception.ExceptionUtils;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SchemaProcessor extends AbstractProcessor {
  static final Set<String> SUPPORTED_ANNOTATIONS;
  static {
    Set<String> set = new LinkedHashSet<>();
    set.add(Schema.class.getCanonicalName());
    set.add(PrimaryKey.class.getCanonicalName());
    SUPPORTED_ANNOTATIONS = set;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv
  ) {
    final Context context = new Context(processingEnv);
    for (Element element : roundEnv.getElementsAnnotatedWith(PrimaryKey.class)) {
      VariableElement variableElement = (VariableElement) element;
      if (!Constants.isPrimaryKey(variableElement)) {
        context.error(variableElement, "{} must be one of {}",
            PrimaryKey.class, Constants.PRIMARY_KEY_TYPES);
      }
    }

    TableCodeGenerator tableCodeGenerator = new TableCodeGenerator(
        context, "com.riiablo.excel.table");
    SerializerCodeGenerator serializerCodeGenerator = new SerializerCodeGenerator(
        context, "com.riiablo.excel.serializer");
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

      // Depends on serializerElement to generate Serializer impl
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
    }

    return true;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return SUPPORTED_ANNOTATIONS;
  }
}
