
package net.benhowell.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben Howell [ben@benhowell.net] on 22-May-2014.
 */
public final class Main {
  public static void main(String[] args) {

    String filePath = new File("").getAbsolutePath();
    List<ScriptManager> managers = loadPlugins(filePath+"/plugins/");

    System.out.println("Available script engines");
    for(String engine: managers.get(0).getAvailableEngines()){
      System.out.println("- engine: " + engine);
    }

    for(ScriptManager m: managers){
      Object instance = m.executeFunction("run");
      m.setParameter("instance", instance);
      System.out.println(m.getName() + " running = " + m.executeFunction("isRunning"));
    }

    //whatever else your app does... probably an event loop or something...
    try {
      //run for 30 seconds
      Thread.sleep(1000 * 30);
    }
    catch(InterruptedException e){}

    for(ScriptManager m: managers){
      System.out.println("shutting down: " + m.getName() + "...");
      m.executeFunction("shutDown");
    }

    System.exit(0);
  }

  public static List<ScriptManager> loadPlugins(String directoryName) {
    List<ScriptManager> managers = new ArrayList<ScriptManager>();
    File directory = new File(directoryName);
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        System.out.println(file.toString());
        for (File f : file.listFiles()) {
          System.out.println(f.toString());
          if(f.getAbsolutePath().endsWith(".py")){
            managers.add(new ScriptManager("python", f, new ScriptDelegate()));
          }
          else if(f.getAbsolutePath().endsWith(".js")){
            managers.add(new ScriptManager("javascript", f, new ScriptDelegate()));
          }
          // else: whatever other engines you want to support
        }
      }
    }
    return managers;
  }


}
