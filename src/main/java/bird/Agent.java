package bird;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

public class Agent {
  public static void agentmain(String agentArgs, Instrumentation inst) {
    String clzName = agentArgs.split("@")[0];
    String clzfile = agentArgs.split("@")[1];
    try {
      for (Class clazz : inst.getAllLoadedClasses()) {
        if (clazz.getName().equals(clzName)) {
          File f = new File(clzfile);
          byte[] b = getBytes(f);
          System.out.println("class name:" + clazz.getName() + "class length(byte):" + b.length);
          ClassDefinition cdf = new ClassDefinition(clazz, b);
          System.out.println("definition class:" + cdf.getDefinitionClass());
          inst.redefineClasses(cdf);

          System.out.println("Done.");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static byte[] getBytes(File classfile) throws IOException {
    FileInputStream in = new FileInputStream(classfile);
    byte[] b = new byte[in.available()];
    in.read(b);
    in.close();
    return b;
  }
}
