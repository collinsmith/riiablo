package com.riiablo.excel.gen;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.excel.Excel;
import com.riiablo.excel.Excel.Entry;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.ClassUtils;

public class SerializerGenerator {
  private static final Logger log = LogManager.getLogger(SerializerGenerator.class);

  public String txtPackage;
  public String binPackage;

  public FileHandle txtDir;
  public FileHandle binDir;

  public int maxStringLen;

  public SerializerGenerator() {}

  public String getSerializerPart() {
    return "Bin";
  }

  public String formatSerializerName(String name) {
    return StringUtils.appendIfMissingIgnoreCase(name, getSerializerPart());
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

  public void generateSerializers(FileHandle root) {
    log.info("Generating serializers for {}...", root);
    FileHandle[] txtFiles = root.list("java");
    for (FileHandle txtFile : txtFiles) {
      try {
        generateSerializer(txtFile);
      } catch (Throwable t) {
        log.error("Error generating serializer for {}", txtFile, t);
      }
    }
  }

  public void generateSerializer(FileHandle txtFile)
      throws ClassNotFoundException {
    String txtName = txtFile.nameWithoutExtension();
    log.trace("txtName: {}", txtName);
    Class txtClass = Class.forName(txtPackage + "." + txtName);

    // Prevent serializing literal Excel.class and non-subclasses of Excel.class
    if (txtClass == Excel.class || !Excel.class.isAssignableFrom(txtClass)) {
      return;
    }

    String binName = formatSerializerName(txtName);
    log.trace("binName: {}", binName);
    FileHandle binFile = binDir.child(binName + "." + "java");
    log.info("{}->{}", txtFile, binFile);

    // Find impls of Entry.class within txtClass
    Class entryClass;
    Class[] entryClasses = ClassUtils.findDeclaredClasses(txtClass, Entry.class);
    switch (entryClasses.length) {
      case 0:
        log.error("{} does not contain an implementation of {}", txtClass, Entry.class);
        return;
      case 1:
        entryClass = entryClasses[0];
        log.trace("entryClass: {}", entryClass.getCanonicalName());
        break;
      default:
        log.error("{} contains ambiguous implementations of {}: {}",
            txtClass, Entry.class, Arrays.toString(entryClasses));
        return;
    }

    if (!entryClass.getSimpleName().equals(Entry.class.getSimpleName())) {
      log.warn("entry class {} not named {}",
          entryClass.getCanonicalName(),
          txtClass.getCanonicalName() + "$" + Entry.class.getSimpleName());
      // return; // Allow it for now
    }

    Context context = new Context();
    context.txtClass = txtClass;
    context.entryClass = entryClass;
    context.binName = binName;
    context.binPackage = binPackage;

    PrintStream out = System.out;
    try {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      out = new PrintStream(outStream, false, "UTF-8");
      writeSerializer(out, context);

      byte[] bytes = IOUtils.toByteArray(outStream.toInputStream());
      binFile.writeBytes(bytes, false);
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
      return;
    }
  }

  static class Context {
    public Class txtClass;
    public Class entryClass;
    public String binName;
    public String binPackage;
    public int indent = 2;
    public int currentIndent = 0;
    String indentSpaces = "";

    public String push() {
      return indentSpaces = StringUtils.repeat(' ', currentIndent += indent);
    }

    public String pop() {
      assert currentIndent >= 0 : "currentIndent(" + currentIndent + ") < " + 0;
      return indentSpaces = StringUtils.repeat(' ', currentIndent -= indent);
    }

    public String peek() {
      return indentSpaces;
    }
  }

  void writeSerializer(PrintStream out, Context context) {
    out.println("package " + context.binPackage + ";");
    out.println();
    out.println("import " + StringUtils.class.getCanonicalName() + ";");
    out.println();
    out.println("public class " + context.binName + " {");
    context.push();

    print_readBin(out, context);
    out.println();
    print_writeBin(out, context);
    out.println();
    print_equals(out, context);
    out.println();
    print_validate(out, context);

    context.pop();
    out.println("}");
    out.println();
  }

  void print_readBin(PrintStream out, Context context) {
    out.print(context.peek());
    out.printf("public static void readBin(%s entry, %s in) throws java.io.IOException {%n",
        context.entryClass.getCanonicalName(), ByteInput.class.getName());
    String indent = context.push();

    for (Field field : context.entryClass.getFields()) {
      log.trace("Creating readBin for {}", field);
      // if (field.getAnnotation(Excel.Entry.Key.class) != null) continue;
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      if (type.isArray()) {
        type = type.getComponentType();
        out.print(indent);
        out.printf("entry.%s = new %s[%d];%n", field.getName(), type.getSimpleName(), column.endIndex() - column.startIndex());
        out.print(indent);
        if (type == String.class) {
          out.printf("for (int x = %d; x < %d; x++) entry.%s[x] = in.read%s(%d, %b);%n", 0, column.endIndex() - column.startIndex(), field.getName(), getMethod(field), maxStringLen, true);
        } else {
          out.printf("for (int x = %d; x < %d; x++) entry.%s[x] = in.read%s();%n", 0, column.endIndex() - column.startIndex(), field.getName(), getMethod(field));
        }
      } else {
        out.print(indent);
        if (type == String.class) {
          out.printf("entry.%s = in.read%s(%d, %b);%n", field.getName(), getMethod(field), maxStringLen, true);
        } else {
          out.printf("entry.%s = in.read%s();%n", field.getName(), getMethod(field));
        }
      }
    }

    out.print(context.pop());
    out.println("}");
  }

  void print_writeBin(PrintStream out, Context context) {
    out.print(context.peek());
    out.printf("public static void writeBin(%s entry, %s out) throws java.io.IOException {%n",
        context.entryClass.getCanonicalName(), ByteOutput.class.getName());
    String indent = context.push();

    for (Field field : context.entryClass.getFields()) {
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      out.print(indent);
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

    out.print(context.pop());
    out.println("}");
  }

  void print_equals(PrintStream out, Context context) {
    out.print(context.peek());
    out.printf("public static boolean equals(%s e1, %s e2) {%n",
        context.entryClass.getCanonicalName(), context.entryClass.getCanonicalName());
    String indent = context.push();

    for (Field field : context.entryClass.getFields()) {
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      out.print(indent);
      if (type.isArray()) {
        out.printf("if (!java.util.Arrays.equals(e1.%s, e2.%s)) return false;%n", field.getName(), field.getName());
      } else if (type.isPrimitive()) {
        out.printf("if (e1.%s != e2.%s) return false;%n", field.getName(), field.getName());
      } else {
        out.printf("if (!java.util.Objects.equals(e1.%s, e2.%s)) return false;%n", field.getName(), field.getName());
      }
    }

    out.print(indent);
    out.println("return true;");

    out.print(context.pop());
    out.println("}");
  }

  void print_validate(PrintStream out, Context context) {
    out.print(context.peek());
    out.printf("public static void validate(%s log, %s e1, %s e2) {%n",
        Logger.class.getCanonicalName(), context.entryClass.getCanonicalName(), context.entryClass.getCanonicalName());
    String indent = context.push();

    for (Field field : context.entryClass.getFields()) {
      Excel.Entry.Column column = field.getAnnotation(Excel.Entry.Column.class);
      if (!column.bin()) continue;
      Class type = field.getType();
      out.print(indent);
      if (type.isArray()) {
        out.printf("if (!java.util.Arrays.equals(e1.%s, e2.%s)) log.warn(\"%s does not match: e1=\" + e1.%s + \" e2=\" + e2.%s);%n", field.getName(), field.getName(), field.getName(), field.getName(), field.getName());
      } else if (type.isPrimitive()) {
        out.printf("if (e1.%s != e2.%s) log.warn(\"%s does not match: e1=\" + e1.%s + \" e2=\" + e2.%s);%n", field.getName(), field.getName(), field.getName(), field.getName(), field.getName());
      } else {
        out.printf("if (!java.util.Objects.equals(e1.%s, e2.%s)) log.warn(\"%s does not match: e1=\" + e1.%s + \" e2=\" + e2.%s);%n", field.getName(), field.getName(), field.getName(), field.getName(), field.getName());
      }
    }

    out.print(context.pop());
    out.println("}");
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
