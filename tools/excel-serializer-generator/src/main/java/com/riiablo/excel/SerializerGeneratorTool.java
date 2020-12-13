package com.riiablo.excel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.ClassUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.tool.HeadlessTool;
import com.riiablo.tool.Tool;

import static com.riiablo.util.ClassUtils.toPackagePath;

public class SerializerGeneratorTool extends Tool {
  private static final Logger log = LogManager.getLogger(SerializerGeneratorTool.class);
  private static final String PACKAGE_NAME = ClassUtils.getPackageName(SerializerGeneratorTool.class);

  public static void main(String[] args) {
    LogManager.setLevel(SerializerGeneratorTool.class.getCanonicalName(), Level.TRACE);
    LogManager.setLevel(SerializerGenerator.class.getCanonicalName(), Level.DEBUG);
    HeadlessTool.create(SerializerGeneratorTool.class, "excel-serializer-generator", args)
        .start();
  }

  String src;
  String srcPackage;
  FileHandle srcDir;

  String gen;
  String genPackage;
  FileHandle genDir;

  @Override
  protected void createCliOptions(Options options) {
    super.createCliOptions(options);

    options.addOption(Option
        .builder()
        .longOpt("schema-package")
        .desc("package containing the excel schemas (default: " + PACKAGE_NAME + ")")
        .hasArg()
        .argName("pkg")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("serializer-package")
        .desc("package containing the excel serializers (default: " + PACKAGE_NAME + ")")
        .hasArg()
        .argName("pkg")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("src")
        .desc("source root containing the excel schemas")
        .required()
        .hasArg()
        .argName("path")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("gen")
        .desc("source root to write the excel serializers")
        .required()
        .hasArg()
        .argName("path")
        .build());
  }

  @Override
  protected void handleCliOptions(String cmd, Options options, CommandLine cli) {
    super.handleCliOptions(cmd, options, cli);

    String srcOptionValue = cli.getOptionValue("src");
    log.debug("src: {}", srcOptionValue);
    src = srcOptionValue;

    String genOptionValue = cli.getOptionValue("gen");
    log.debug("gen: {}", genOptionValue);
    gen = genOptionValue;

    String srcPackageOptionValue = cli.getOptionValue("schema-package", PACKAGE_NAME);
    log.debug("schema-package: {}", srcPackageOptionValue);
    srcPackage = srcPackageOptionValue;

    String genPackageOptionValue = cli.getOptionValue("serializer-package", PACKAGE_NAME);
    log.debug("serializer-package: {}", genPackageOptionValue);
    genPackage = genPackageOptionValue;
  }

  @Override
  public void create() {
    srcDir = Gdx.files.absolute(src).child(toPackagePath(srcPackage));
    log.debug("srcDir: {}", srcDir);

    genDir = Gdx.files.absolute(gen);
    log.debug("genDir: {}", genDir);

    LogManager.setLevel(SerializerGenerator.class.getCanonicalName(), Level.INFO);
    SerializerGenerator generator = new SerializerGenerator(srcPackage, genPackage, srcDir, genDir);
    generator.generateSerializers();

    Gdx.app.exit();
  }
}
