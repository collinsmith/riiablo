package com.riiablo.mpq;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class MPQLibBuilder {
  private MPQLibBuilder() {}

  public static void main(String[] args) throws Exception {
    NativeCodeGenerator jnigen = new NativeCodeGenerator();
    jnigen.generate("src", "build/classes/java/main", "jni");

    BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
    win32.compilerPrefix = "";

    BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);

    new AntScriptGenerator().generate(new BuildConfig("mpqlib"), win32, win64);
    BuildExecutor.executeAnt("jni/build-windows32.xml", "-v -Dhas-compiler=true clean postcompile");
    BuildExecutor.executeAnt("jni/build-windows64.xml", "-v -Dhas-compiler=true clean postcompile");
    // BuildExecutor.executeAnt("jni/build-linux32.xml", "-v -Dhas-compiler=true clean postcompile");
    // BuildExecutor.executeAnt("jni/build-linux64.xml", "-v -Dhas-compiler=true clean postcompile");
    // BuildExecutor.executeAnt("jni/build-macosx32.xml", "-v -Dhas-compiler=true  clean postcompile");
    BuildExecutor.executeAnt("jni/build.xml", "-v");
  }
}
