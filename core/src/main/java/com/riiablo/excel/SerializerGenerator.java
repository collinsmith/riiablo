package com.riiablo.excel;

import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.util.Arrays;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.ClassUtils;

public class SerializerGenerator {
  private static final Logger log = LogManager.getLogger(SerializerGenerator.class);

  final String sourcePackage;
  final String serializerPackage;

  final FileHandle sourceDir;
  final FileHandle serializerDir;

  final SerializerSourceGenerator sourceGenerator;

  public SerializerGenerator(
      FileHandle sourceDir,
      FileHandle serializerDir) {
    this(
        "com.riiablo.excel.txt",
        "com.riiablo.excel.serializer",
        sourceDir,
        serializerDir
    );
  }

  public SerializerGenerator(
      String sourcePackage,
      String serializerPackage,
      FileHandle sourceDir,
      FileHandle serializerDir
  ) {
    this.sourcePackage = sourcePackage;
    this.serializerPackage = serializerPackage;
    this.sourceDir = sourceDir;
    this.serializerDir = serializerDir;
    sourceGenerator = new SerializerSourceGenerator(sourcePackage, serializerPackage);
  }

  public void generateSerializers() {
    log.info("Generating serializers for {}...", sourceDir);
    FileHandle[] sourceFiles = sourceDir.list("java");
    for (FileHandle sourceFile : sourceFiles) {
      try {
        log.info("Processing: '{}'", sourceFile);
        configureSourceGenerator(sourceFile);
        JavaFile serializerFile = sourceGenerator.generateFile(SerializerGenerator.class);
        File file = serializerFile.writeToFile(serializerDir.file());
        log.debug("Generated: '{}'", file);
      } catch (Throwable t) {
        log.error("Failed to generate serializer for {}", sourceFile, t);
      }
    }
  }

  public void configureSourceGenerator(FileHandle sourceFile) throws ClassNotFoundException {
    String sourceName = sourceFile.nameWithoutExtension();
    log.trace("sourceName: {}", sourceName);
    Class sourceClass = Class.forName(sourcePackage + "." + sourceName);

    // Prevent serializing literal Excel.class and non-subclasses of Excel.class
    if (sourceClass == Excel.class || !Excel.class.isAssignableFrom(sourceClass)) {
      return;
    }

    final Class entryClass;
    Entry excelDef = (Entry) sourceClass.getAnnotation(Entry.class);
    if (excelDef != null) {
      entryClass = excelDef.value();
    } else {
      log.warn("excel class {} not annotated with {}",
          sourceClass.getCanonicalName(), Entry.class.getCanonicalName());
      // Find impls of Entry.class within sourceClass
      Class[] entryClasses = ClassUtils.findDeclaredClasses(sourceClass, Excel.Entry.class);
      switch (entryClasses.length) {
        case 0:
          log.error("{} does not contain an implementation of {}", sourceClass, Excel.Entry.class);
          return;
        case 1:
          entryClass = entryClasses[0];
          log.trace("entryClass: {}", entryClass.getCanonicalName());
          break;
        default:
          log.error("{} contains ambiguous implementations of {}: {}",
              sourceClass, Excel.Entry.class, Arrays.toString(entryClasses));
          return;
      }
    }

    if (!entryClass.getSimpleName().equals(Excel.Entry.class.getSimpleName())) {
      log.warn("entry class {} not named {}",
          entryClass.getCanonicalName(),
          sourceClass.getCanonicalName() + "$" + Excel.Entry.class.getSimpleName());
      // return; // Allow it for now
    }

    sourceGenerator.configure(sourceClass, entryClass);
  }
}
