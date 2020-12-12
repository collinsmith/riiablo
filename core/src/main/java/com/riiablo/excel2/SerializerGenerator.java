package com.riiablo.excel2;

import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.util.Arrays;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.excel2.Excel.Entry;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.ClassUtils;

public class SerializerGenerator {
  private static final Logger log = LogManager.getLogger(SerializerGenerator.class);

  String sourcePackage = "com.riiablo.excel2.txt";
  String serializerPackage = "com.riiablo.excel2.serializer";

  FileHandle sourceDir;
  FileHandle serializerDir;

  SerializerSourceGenerator sourceGenerator;

  void init() {
    sourceGenerator = new SerializerSourceGenerator(sourcePackage, serializerPackage);
  }

  public void generateSerializers() {
    log.info("Generating serializers for {}...", sourceDir);
    FileHandle[] sourceFiles = sourceDir.list("java");
    for (FileHandle sourceFile : sourceFiles) {
      try {
        log.info("Generating: '{}'", sourceFile);
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

    // Find impls of Entry.class within sourceClass
    Class entryClass;
    Class[] entryClasses = ClassUtils.findDeclaredClasses(sourceClass, Entry.class);
    switch (entryClasses.length) {
      case 0:
        log.error("{} does not contain an implementation of {}", sourceClass, Entry.class);
        return;
      case 1:
        entryClass = entryClasses[0];
        log.trace("entryClass: {}", entryClass.getCanonicalName());
        break;
      default:
        log.error("{} contains ambiguous implementations of {}: {}",
            sourceClass, Entry.class, Arrays.toString(entryClasses));
        return;
    }

    if (!entryClass.getSimpleName().equals(Entry.class.getSimpleName())) {
      log.warn("entry class {} not named {}",
          entryClass.getCanonicalName(),
          sourceClass.getCanonicalName() + "$" + Entry.class.getSimpleName());
      // return; // Allow it for now
    }

    sourceGenerator.configure(sourceClass, entryClass);
  }
}
