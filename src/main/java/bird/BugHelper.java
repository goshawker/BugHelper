package bird;

import com.sun.tools.attach.VirtualMachine;
import sun.jvmstat.monitor.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * 在线修改java类 1、导出为可执行jar Manifest-Version: 1.0 Agent-Class:
 * com.package.AgentLoader.agentNameHere Permissions : all-permissions
 *
 * @author mike
 */
public class BugHelper {
  private static final ArrayList<String> PIDS = new ArrayList<>();

  public BugHelper() {

  }

  public static void main(String[] args) {
    try {
      __Run();
    } catch (MonitorException | URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void __Run() throws MonitorException, URISyntaxException {
    __GETALLPID();

    String SELECTED_PID, HOTFIX_ARGS;
    Scanner scanner = new Scanner(System.in);
    System.out.println("1.[请选择PID]:");
    SELECTED_PID = scanner.nextLine();

    while (!PIDS.contains(SELECTED_PID)) {
      System.out.println("输入PID不正确，请重新输入！");
      SELECTED_PID = scanner.nextLine();
    }

    System.out.println("2.[请输入需要更新的class信息，格式：包.类名@class文件绝对路径]:");
    HOTFIX_ARGS = scanner.nextLine();
    while (HOTFIX_ARGS.split("@").length < 2) {
      if(HOTFIX_ARGS.toLowerCase().equals("quit")){
        System.exit(0);
      }
      System.out.println("输入参数的格式不正确，请重新输入！");
      HOTFIX_ARGS = scanner.nextLine();
    }
    //package.class
    String args1 = HOTFIX_ARGS.split("@")[0];
    String FirstFileName = args1.substring(args1.lastIndexOf(".") + 1);

    //class-file path
    String classpath = HOTFIX_ARGS.split("@")[1];
    File classfile = new File(classpath);
    String fullFileName = classfile.getName();

    String fileNameNoExt = fullFileName.substring(0, fullFileName.lastIndexOf("."));


    while (!classpath.endsWith("class") || !classfile.exists() || !FirstFileName.equals(fileNameNoExt)) {
      System.out.printf("输入的参数不正确,请重新输入！ %s", classpath);
      HOTFIX_ARGS = scanner.nextLine();
      classpath = HOTFIX_ARGS.split("@")[1];
      classfile = new File(classpath);
    }

    System.out.println("JVM进程实例编号（PID）: " + SELECTED_PID);
    System.out.println("输入参数: " + HOTFIX_ARGS);

    // 获取当前 jar 路径
    URL locationPath = BugHelper.class.getProtectionDomain().getCodeSource().getLocation();
    String jarFilePath = locationPath.getPath();

    try {
      VirtualMachine vm = VirtualMachine.attach(SELECTED_PID);
      String OSNAME = System.getProperty("os.name");
      String __PATH = jarFilePath;
      if (__PATH != null && __PATH.startsWith("/") && OSNAME.startsWith("Windows")) {
        jarFilePath = __PATH.substring(1);
      }

      // pro
      vm.loadAgent(jarFilePath, HOTFIX_ARGS);

      // dev
      // vm.loadAgent("d:/bugHelper.jar", HOTFIX_ARGS);

      Thread.sleep(2000);
      System.out.println("done.");
      System.out.println("3.[是否继续(Y/N)]:");
      String CONTINUES = scanner.nextLine();
      if (CONTINUES.equalsIgnoreCase("Y")) {
        __Run();
      } else {
        System.out.println("Bye.");
        scanner.close();
        System.exit(0);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void __GETALLPID() throws MonitorException, URISyntaxException {
    PIDS.clear();
    MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");
    Set<?> vmlist = new HashSet<Object>(local.activeVms());
    System.out.println("============SYSTEM PROCESS ID===============");
    for (Object process : vmlist) {
      VmIdentifier vid = new VmIdentifier("//" + process);
      MonitoredVm vm = local.getMonitoredVm(vid);
      String PN = MonitoredVmUtil.mainClass(vm, true);
      System.out.printf("PID:%s \t 名称:%s%n", process, PN);
      PIDS.add(process.toString());
    }
    System.out.println();
  }

}
