package com.riiablo.codec.excel;

import com.google.common.io.LittleEndianDataOutputStream;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.riiablo.mpq.MPQFileHandleResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassPathUtils;

import java.io.OutputStream;
import java.lang.reflect.Method;

public class BinGenerationTool extends ApplicationAdapter {
  private static final String TAG = "BinGenerationTool";

  private static final boolean VALIDATE_BIN = true;

  public static void main(String[] args) throws Exception {
    new HeadlessApplication(new BinGenerationTool(args));
  }

  final String[] args;

  BinGenerationTool(String[] args) {
    this.args = args;
  }

  @Override
  public void create() {
    try {
      Package pkg = Package.getPackage(args[0]);
      Gdx.app.log(TAG, "package=" + pkg.getName());

      String pkgPath = pkg.getName().replace('.', '/');

      Gdx.app.log(TAG, "home=" + Gdx.files.getLocalStoragePath());
      FileHandle home = new FileHandle(Gdx.files.getLocalStoragePath());
      FileHandle src = home.child("core/src").child(pkgPath);
      Gdx.app.log(TAG, "src=" + src);

      FileHandle dst = home.child("android/assets").child(args[1]);
      Gdx.app.log(TAG, "dst=" + dst);

      FileHandle mpqPath = Gdx.files.absolute(args[2]);
      Gdx.app.log(TAG, "mpqs=" + mpqPath);
      MPQFileHandleResolver mpqs = new MPQFileHandleResolver(mpqPath);

      FileHandle[] children = src.list("java");
      for (FileHandle child : children) {
        String name = child.nameWithoutExtension();
        String className = ClassPathUtils.toFullyQualifiedName(pkg, name);
        Class clazz = Class.forName(className);
        if (clazz == Excel.class || !Excel.class.isAssignableFrom(clazz)) {
          continue;
        } else if (!Excel.isBinned(clazz)) {
          //continue;
        }

        FileHandle txt = mpqs.resolve("data\\global\\excel\\" + name + ".txt");
        if (txt == null) { // attempt to resolve one of my own txts
          txt = home.child("android/assets/data").child(name + ".txt");
          if (!txt.exists()) continue;
        }
        FileHandle bin = dst.child(name + ".bin");
        Class<Excel> excelClass = (Class<Excel>) clazz;
        generate(txt, bin, excelClass);
      }
    } catch (Throwable t) {
      throw new GdxRuntimeException(t);
    }

    Gdx.app.exit();
  }

  @SuppressWarnings("unchecked")
  public void generate(FileHandle txt, FileHandle bin, Class<Excel> excelClass) throws Exception {
    Gdx.app.log(TAG, bin.toString());
    Excel<Excel.Entry> excel = Excel.load(excelClass, txt, Excel.EXPANSION);
    OutputStream out = null;
    try {
      out = bin.write(false, 8192);
      LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);
      excel.writeBin(dos);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
      IOUtils.closeQuietly(out);
    }

    if (VALIDATE_BIN) {
      Class<Excel.Entry> entryClass = getEntryClass(excelClass);
      String binClassName = excelClass.getName() + "Bin";
      Class binClass = Class.forName(binClassName);
      Method equals = binClass.getMethod("equals", entryClass, entryClass);
      Excel binExcel = Excel.load(excelClass, txt, bin, null);
      if (binExcel.size() != excel.size()) Gdx.app.error(TAG, "excel sizes do not match!");
      for (Excel.Entry e1 : excel) {
        Excel.Entry eq = getEqual(equals, e1, binExcel);
        if (eq == null) {
          Gdx.app.log(TAG, "ERROR at index " + e1);
          //break;
        } else {
          //Gdx.app.log(TAG, e1 + "=" + eq);
        }
      }
    }
  }

  private static Excel.Entry getEqual(Method equals, Excel.Entry e1, Excel<Excel.Entry> binExcel) throws Exception {
    for (Excel.Entry e2 : binExcel) {
      Object result = equals.invoke(null, e1, e2);
      if (((Boolean) result).booleanValue()) {
        return e2;
      }
    }

    return null;
  }

  private static String generateBinName(String fileName) {
    return FilenameUtils.getPath(fileName) + FilenameUtils.getBaseName(fileName) + ".bin";
  }

  private static Method getMethod(Class clazz, Class annotation) {
    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      if (method.getAnnotation(annotation) != null) {
        return method;
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private static Class<Excel.Entry> getEntryClass(Class excelClass) {
    Class[] declaredClasses = excelClass.getDeclaredClasses();
    for (Class declaredClass : declaredClasses) {
      if (Excel.Entry.class.isAssignableFrom(declaredClass)) {
        return (Class<Excel.Entry>) declaredClass;
      }
    }

    return null;
  }
}
