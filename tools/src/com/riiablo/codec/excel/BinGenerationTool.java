package com.riiablo.codec.excel;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class BinGenerationTool extends ApplicationAdapter {

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
      generate(args[0], args[1]);
    } catch (Throwable t) {
      throw new GdxRuntimeException(t);
    }

    Gdx.app.exit();
  }

  public void generate(String fileName, String className) throws Exception {
    Class<Excel> excelClass = (Class<Excel>) Class.forName(className);
    if (!Excel.isBinned(excelClass)) {
      System.out.println(excelClass + " is not annotated with " + Excel.Binned.class);
      return;
    }

    File file = new File(fileName);
    //InputStream in = FileUtils.openInputStream(file);
    //TXT txt = TXT.loadFromStream(in);
    FileHandle txt = new FileHandle(file);

    Excel excel = Excel.load(excelClass, txt, null, Excel.EXPANSION);

    String binFileName = generateBinName(fileName);
    File binFile = new File(binFileName);
    OutputStream out = FileUtils.openOutputStream(binFile);
    BufferedOutputStream buffer = new BufferedOutputStream(out);
    LittleEndianDataOutputStream dos = null;
    try {
      dos = new LittleEndianDataOutputStream(buffer);
      excel.writeBin(dos);
    } finally {
      StreamUtils.closeQuietly(dos);
    }

    InputStream in = FileUtils.openInputStream(binFile);
    BufferedInputStream bufferIn = new BufferedInputStream(in);
    LittleEndianDataInputStream dis = null;
    try {
      dis = new LittleEndianDataInputStream(bufferIn);
      Excel copy = excelClass.newInstance();
      copy.readBin(dis);
    } finally {
      StreamUtils.closeQuietly(dis);
    }
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
}
