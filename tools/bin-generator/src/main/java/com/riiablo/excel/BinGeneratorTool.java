package com.riiablo.excel;

import java.io.PrintStream;
import java.lang.reflect.Field;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.excel.gen.SerializerGenerator;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.tool.HeadlessTool;
import com.riiablo.tool.Tool;

import static com.riiablo.util.ClassUtils.toPackagePath;

public class BinGeneratorTool extends Tool {
  private static final Logger log = LogManager.getLogger(BinGeneratorTool.class);
  private static final String PACKAGE_NAME = ClassUtils.getPackageName(BinGeneratorTool.class);

  public static void main(String[] args) {
    LogManager.setLevel(BinGeneratorTool.class.getCanonicalName(), Level.TRACE);
    LogManager.setLevel(SerializerGenerator.class.getCanonicalName(), Level.DEBUG);
    HeadlessTool.create(BinGeneratorTool.class, "bin-generator", args)
        .start();
  }

  int maxStringLen = 64;

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
        .longOpt("txt-package")
        .desc("package containing the excel schemas (default: " + PACKAGE_NAME + ")")
        .hasArg()
        .argName("pkg")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("bin-package")
        .desc("package containing the excel bins (default: " + PACKAGE_NAME + ")")
        .hasArg()
        .argName("pkg")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("src")
        .desc("directory containing the excel schemas")
        .required()
        .hasArg()
        .argName("path")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("gen")
        .desc("directory to write the excel bins")
        .required()
        .hasArg()
        .argName("path")
        .build());

    options.addOption(Option
        .builder()
        .longOpt("string-len")
        .desc("maximum number of characters in a null-terminated string (default: " + maxStringLen + " characters)")
        .hasArg()
        .argName(int.class.getName())
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

    String txtPackageOptionValue = cli.getOptionValue("txt-package", PACKAGE_NAME);
    log.debug("txt-package: {}", txtPackageOptionValue);
    srcPackage = txtPackageOptionValue;

    String binPackageOptionValue = cli.getOptionValue("bin-package", PACKAGE_NAME);
    log.debug("bin-package: {}", binPackageOptionValue);
    genPackage = binPackageOptionValue;

    String stringLenOptionValue = cli.getOptionValue("string-len");
    log.debug("string-len: {}", StringUtils.defaultString(stringLenOptionValue));
    maxStringLen = NumberUtils.toInt(stringLenOptionValue, maxStringLen);
  }

  @Override
  public void create() {
    srcDir = Gdx.files.absolute(src).child(toPackagePath(srcPackage));
    log.debug("srcDir: {}", srcDir);

    genDir = Gdx.files.absolute(gen).child(toPackagePath(genPackage));
    log.debug("genDir: {}", genDir);

    log.debug("maxStringLen: {} characters", maxStringLen);

    SerializerGenerator generator = new SerializerGenerator();
    generator.txtPackage = srcPackage;
    generator.binPackage = genPackage;
    generator.txtDir = srcDir;
    generator.binDir = genDir;
    generator.maxStringLen = maxStringLen;
    generator.generateSerializers(srcDir);

    // log.info("Generating serializers...");
    // FileHandle[] srcFiles = srcDir.list("java"); // assert all sources like *.java
    // if (srcFiles.length == 0) {
    //   log.warn("{} did not contain any java source files!", srcDir);
    // }
    //
    // for (FileHandle src : srcFiles) {
    //   try {
    //     generateSerializer(src);
    //   } catch (Throwable t) {
    //     log.error("Error generating serializer for {}", src, t);
    //   }
    // }

    Gdx.app.exit();
  }

  private static String formatBinFilename(String className) {
    return className + "Bin";
  }

  private void generateSerializer(FileHandle src) throws Exception {
    String baseName = src.nameWithoutExtension();
    Class srcClass = Class.forName(srcPackage + "." + baseName);
    if (srcClass == Excel.class || !Excel.class.isAssignableFrom(srcClass)) {
      return;
    }

    String binClassName = formatBinFilename(baseName);
    FileHandle gen = genDir.child(binClassName + "." + "java");
    log.info("{}->{}", src, gen);

    log.trace("srcClass: {}", srcClass.getCanonicalName());

    // find Excel.Entry impl
    Class entryClass;
    try {
      entryClass = findDeclaredClass(srcClass, Excel.Entry.class);
      log.trace("entryClass: {}", entryClass.getCanonicalName());
    } catch (ClassNotFoundException t) {
      log.error("{} does not contain implementation of {}", srcClass, Excel.Entry.class);
      ExceptionUtils.wrapAndThrow(t);
      return;
    }

    if (!entryClass.getSimpleName().equals("Entry")) {
      log.warn("entry class {} not named {}",
          entryClass.getCanonicalName(),
          srcClass.getCanonicalName() + "$" + "Entry");
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, false, "UTF-8");
    PrintStream dst = ps; // System.out
    writeSerializer(dst, srcClass, entryClass, binClassName);

    byte[] bytes = IOUtils.toByteArray(out.toInputStream());
    gen.writeBytes(bytes, false);
  }

  private static Class findDeclaredClass(Class c, Class impl) throws ClassNotFoundException {
    final Class[] classes = c.getDeclaredClasses();
    for (Class declaredClass : classes) {
      if (impl.isAssignableFrom(declaredClass)) {
        return declaredClass;
      }
    }

    throw new ClassNotFoundException(c + " does not implement " + impl);
  }

  private void writeSerializer(
      PrintStream out,
      Class<Excel> srcClass,
      Class<Excel.Entry> entryClass,
      String binClassName) {
    Field[] fields = entryClass.getFields();

    out.println("package " + genPackage + ";");
    out.println();
    out.println("import " + StringUtils.class.getCanonicalName() + ";");
    out.println();
    out.println("public class " + binClassName + " {");

    final int spacing = 4;

    // readBin(Excel.Entry entry, ByteInput in)
    {
      out.printf ("  public static void readBin(%s entry, %s in) throws java.io.IOException {%n",
          entryClass.getCanonicalName(), ByteInput.class.getName());

      print_readBin(out, fields, spacing);

      out.println("  }");
    }


    // writeBin(Excel.Entry entry, ByteOutput out)
    {
      out.println();
      out.printf ("  public static void writeBin(%s entry, %s out) throws java.io.IOException {%n",
          entryClass.getCanonicalName(), ByteOutput.class.getName());
      print_writeBin(out, fields, spacing);
      out.println("  }");
    }


    // equals(Excel.Entry e1, Excel.Entry e2)
    {
      out.println();
      out.printf ("  public static boolean equals(%s e1, %s e2) {%n",
          entryClass.getCanonicalName(), entryClass.getCanonicalName());
      print_equals(out, fields, spacing);
      out.println("    return true;");
      out.println("  }");
    }

    // validate(Excel.Entry e1, Excel.Entry e2)
    {
      out.println();
      out.printf ("  public static void validate(%s log, %s e1, %s e2) {%n",
          Logger.class.getCanonicalName(), entryClass.getCanonicalName(), entryClass.getCanonicalName());
      print_validate(out, fields, spacing);
      out.println("  }");
    }

    out.println("}");
    out.println();
  }

  private void print_readBin(PrintStream out, Field[] fields, int spacing) {
    String spaces = StringUtils.repeat(' ', spacing);
    for (Field field : fields) {
      // if (field.getAnnotation(Excel.Entry.Key.class) != null) continue;
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      if (type.isArray()) {
        type = type.getComponentType();
        out.print(spaces);
        out.printf("entry.%s = new %s[%d];%n", field.getName(), type.getSimpleName(), column.endIndex() - column.startIndex());
        out.print(spaces);
        if (type == String.class) {
          out.printf("for (int x = %d; x < %d; x++) entry.%s[x] = in.read%s(%d, %b);%n", 0, column.endIndex() - column.startIndex(), field.getName(), getMethod(field), maxStringLen, true);
        } else {
          out.printf("for (int x = %d; x < %d; x++) entry.%s[x] = in.read%s();%n", 0, column.endIndex() - column.startIndex(), field.getName(), getMethod(field));
        }
      } else {
        out.print(spaces);
        if (type == String.class) {
          out.printf("entry.%s = in.read%s(%d, %b);%n", field.getName(), getMethod(field), maxStringLen, true);
        } else {
          out.printf("entry.%s = in.read%s();%n", field.getName(), getMethod(field));
        }
      }
    }
  }

  private void print_writeBin(PrintStream out, Field[] fields, int spacing) {
    String spaces = StringUtils.repeat(' ', spacing);
    for (Field field : fields) {
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      out.print(spaces);
      if (type.isArray()) {
        type = type.getComponentType();
        if (type == String.class) {
          out.printf("for (%s x : entry.%s) out.write%s(StringUtils.defaultString(x));%n", type.getSimpleName(), field.getName(), getMethod(field));
        } else {
          out.printf("for (%s x : entry.%s) out.write%s(x);%n", type.getSimpleName(), field.getName(), getMethod(field));
        }
      } else {
        if (type == String.class) {
          out.printf("out.write%s(StringUtils.defaultString(entry.%s));%n", getMethod(field), field.getName());
        } else {
          out.printf("out.write%s(entry.%s);%n", getMethod(field), field.getName());
        }
      }
    }
  }

  private void print_equals(PrintStream out, Field[] fields, int spacing) {
    String spaces = StringUtils.repeat(' ', spacing);
    for (Field field : fields) {
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      out.print(spaces);
      if (type.isArray()) {
        out.printf("if (!java.util.Arrays.equals(e1.%s, e2.%s)) return false;%n", field.getName(), field.getName());
      } else if (type.isPrimitive()) {
        out.printf("if (e1.%s != e2.%s) return false;%n", field.getName(), field.getName());
      } else {
        out.printf("if (!java.util.Objects.equals(e1.%s, e2.%s)) return false;%n", field.getName(), field.getName());
      }
    }
  }

  private void print_validate(PrintStream out, Field[] fields, int spacing) {
    String spaces = StringUtils.repeat(' ', spacing);
    for (Field field : fields) {
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      out.print(spaces);
      if (type.isArray()) {
        out.printf("    if (!java.util.Arrays.equals(e1.%s, e2.%s)) log.warn(\"%s does not match: e1=\" + e1.%s + \" e2=\" + e2.%s);%n", field.getName(), field.getName(), field.getName(), field.getName(), field.getName());
      } else if (type.isPrimitive()) {
        out.printf("    if (e1.%s != e2.%s) log.warn(\"%s does not match: e1=\" + e1.%s + \" e2=\" + e2.%s);%n", field.getName(), field.getName(), field.getName(), field.getName(), field.getName());
      } else {
        out.printf("    if (!java.util.Objects.equals(e1.%s, e2.%s)) log.warn(\"%s does not match: e1=\" + e1.%s + \" e2=\" + e2.%s);%n", field.getName(), field.getName(), field.getName(), field.getName(), field.getName());
      }
    }
  }

  private static String getMethod(Field field) {
    Class type = field.getType();
    if (type.isArray()) {
      type = type.getComponentType();
    }

    if (type == String.class) {
      return "String";
    } else if (type == byte.class) {
      return "8";
    } else if (type == short.class) {
      return "16";
    } else if (type == int.class) {
      return "32";
    } else if (type == long.class) {
      return "64";
    } else if (type == boolean.class) {
      return "Boolean";
    } else {
      throw new UnsupportedOperationException(
          "No support for " + type.getCanonicalName() + " fields");
    }
  }
}
