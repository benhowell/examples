
package net.benhowell.example;

/**
 * Created by Ben Howell [ben@benhowell.net] on 22-May-2014.
 */
public final class Main {
    public static void main(String[] args) {

      Presenter presenter = new Presenter();
      ScriptDelegate delegate = new ScriptDelegate(presenter);

      String engineName = "python";
      String memeScriptFile = "file:///plugins/random_meme.py";
      String bitCoinScriptFile = "file:///plugins/bit_coin_price.py";

      ScriptManager randomMemeManager = new ScriptManager(engineName, memeScriptFile, delegate);
      System.out.println("Available script engines");
      for(String engine: randomMemeManager.getAvailableEngines()){
        System.out.println("- engine: " + engine);
      }

      Object memeScript = randomMemeManager.executeFunction("run");
      randomMemeManager.setParameter("instance", memeScript);
      Boolean isRunning = (Boolean)randomMemeManager.executeFunction("isRunning");
      System.out.println("random_meme running = " + isRunning.toString());

      ScriptManager bitCoinManager = new ScriptManager(engineName, bitCoinScriptFile, delegate);
      Object bitCoinScript = bitCoinManager.executeFunction("run");
      bitCoinManager.setParameter("instance", bitCoinScript);
      isRunning = (Boolean)bitCoinManager.executeFunction("isRunning");
      System.out.println("bit_coin_price running = " + isRunning.toString());

      //whatever else your app does... probably an event loop or something...
      try {
        //run for 30 seconds
        Thread.sleep(1000 * 30);
      }
      catch(InterruptedException e){}

      System.out.println("shutting down services...");
      randomMemeManager.executeFunction("shutDown");
      bitCoinManager.executeFunction("shutDown");
      System.exit(0);
    }
}
