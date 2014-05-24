package net.benhowell.example;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 ScriptManager allows execution of scripts such as JavaScript and Python Ruby,
 Groovy, Judoscript, Haskell, Tcl, Awk and PHP amongst others. Java8 supports
 over 30 different language engines.

 This implementation uses dynamic invocation of individual functions and
 objects within scripts using Invocable where available, and where not
 available, implements a graceful fallback strategy to compilation of script
 (if available) and passing the function name to execute to the script.
 Where neither Invocable nor Compilable are available, scripts are interpreted
 each time they are run.

 We can expose our application objects as global variables to a script. The
 script can access each variable and can call public methods on it. The syntax
 to access Java objects, methods and fields is dependent on the scripting
 language used.

 We can pass any object, value or variable to the script using the following:
 ScriptManager.setParameter("parameterName", Object);

 We can access any object, value or variable passed to or created by the script
 itself from java using the following:
 ScriptManager.getParameter("parameterName");
 */


/**
 * Created by Ben Howell [ben@benhowell.net] on 22-May-2014.
 */
public class ScriptManager {

  private ScriptEngineManager manager;
  private String engineName;
  private String name;
  private String script;
  private ScriptEngine engine;
  private CompiledScript compiledScript;
  private Invocable invocable;

  /**
   * Constructor. Creates script engine manager, loads script and sets delegate
   * parameter before script is parsed.
   * @param engineName the engine name that runs the script (e.g. "python",
   * "javascript", etc).
   * @param scriptPath the plugin path
   * @param delegate the delegate containing application functions callable
   * from scripts.
   */
  public ScriptManager(String engineName, File scriptPath, ScriptDelegate delegate) {
    this.manager = new ScriptEngineManager();
    this.engineName = engineName;
    this.name = scriptPath.getName();
    this.script = scriptPath.getAbsolutePath();
    this.setEngine(engineName);
    this.setParameter("delegate", delegate);
    this.loadScript();
  }

  /**
   * Retrieves the name of the plugin.
   * @return the name of the plugin.
   */
  public String getName(){
    return this.name;
  }

  /**
   * Sets a global variable in the script engine. This variable will be
   * callable from anywhere in the script.
   * @param name parameter name.
   * @param value parameter value.
   */
  public void setParameter(String name, Object value) {
    this.engine.put(name, value);
  }

  /**
   * Retrieves a global variable from the script engine.
   * @param name the name of the parameter to retrieve.
   * @return the parameter requested.
   */
  public Object getParameter(String name) {
    return this.engine.get(name);
  }

  /**
   * Convenience function for setting a number of parameters.
   * @param parameters HashMap of parameter name/value pairs.
   */
  public void setParameters(HashMap<String, Object> parameters) {
    for (Map.Entry<String, Object> entry : parameters.entrySet()){
      this.engine.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Returns the available engine types supported.
   * @return the names of all engines supported.
   */
  public List<String> getAvailableEngines() {
    List<String> engines = new ArrayList<String>();
    for (ScriptEngineFactory factory : manager.getEngineFactories()) {
      engines.add(factory.getLanguageName());
    }
    return engines;
  }

  /**
   * Executes an entire script.
   * @return the result.
   */
  public Object execute(){
    if(this.isCompilable())
      return execute(this.compiledScript);
    else
      return execute(this.script);
  }

  /**
   * Executes a function within the currently set script.
   * @param function the function to execute.
   * @param parameters the parameters to set.
   * @return the result.
   */
  public Object executeFunction(String function, HashMap<String, Object> parameters){
    this.setParameters(parameters);
    return this.executeFunction(function);
  }

  /**
   * Executes a function within the currently set script.
   * @param function the function to execute.
   * @return the result.
   */
  public Object executeFunction(String function){
    //if script engine is invocable, invoke method directly
    if(this.isInvocable()) {
      try {
        return invocable.invokeFunction(function);
      }
      catch (ScriptException e) {
        System.out.println("ScriptException error: " + e.toString());
      }
      catch (NoSuchMethodException e) {
        System.out.println("function: " + function + " not found in script: " + e.toString());
      }
    }
    //else pass function argument and execute script
    else {
      Object obj;
      this.setParameter("func", function);
      if(this.isCompilable()) {
        obj = this.execute(this.compiledScript);
      }
      else {
        obj = this.execute(this.script);
      }
      //reset function argument
      this.setParameter("func", null);
      return obj;
    }
    return null;
  }

  /**
   * Returns true if script is Invocable, else false.
   * @return true if script is Invocable, else false.
   */
  private Boolean isInvocable() {
    return invocable != null;
  }

  /**
   * Returns true if script is Compilable, else if script is Invocable or not
   * Compilable returns false.
   * @return true if script is Compilable, else if script is Invocable or not
   * Compilable returns false.
   */
  private Boolean isCompilable() {
    return compiledScript != null;
  }

  /**
   * Sets the engine type for this ScriptManager.
   * @param engineName the name of the engine to set.
   */
  private void setEngine(String engineName) {
    this.engine = manager.getEngineByName(engineName);
  }

  /**
   * Loads the script. Attempts to make script Invocable or Compilable as
   * necessary.
   */
  private void loadScript() {
    this.setScript(this.script);

    //set invocable if available
    this.setInvocable(this.script);

    //if not invocable, set compilable if available
    if(!this.isInvocable())
      this.setCompilable(this.script);

    //if not invocable or compilable, script will be interpreted
    // every time it is called.
  }

  /**
   * Streams the script from file.
   * @returns entire script as a String.
   */
  private static String convertStreamToString(InputStreamReader is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }


  /**
   * Retrieves the script and stores it as a String for further use.
   * @param scriptPath the script itself or the location of the script.
   */
  private void setScript(String scriptPath) {
    if(scriptPath != null) {
      try {
        BufferedInputStream bis;
        bis = new BufferedInputStream(new FileInputStream(scriptPath));
        InputStreamReader in = new InputStreamReader(bis);
        this.script = convertStreamToString(in);
      }
      catch (FileNotFoundException e) {
        System.out.println("error whilst retrieving script from file: " + e.toString());
      }
    }
    else {
      System.out.println("scriptPath is null");
    }
  }

  /**
   * Attempts to interpret and set the given script to Invocable.
   * @param script the script to make Invocable.
   */
  private void setInvocable(String script) {
    //if available, set invocable interface
    this.invocable = null;
    if(script == null) {
      System.out.println("script code has not been set");
    }
    if(this.engine instanceof Invocable) {
      this.execute(script); //parse script
      invocable = (Invocable) this.engine;
    }
    else {
      System.out.println("Invocable is not implemented by engine: " + this.engineName);
    }
  }

  /**
   * Attempts to compile the given script.
   * @param script the script to compile.
   */
  private void setCompilable(String script) {
    this.compiledScript = null;
    if(script == null) {
      System.out.println("script code has not been set");
    }
    if(this.engine instanceof Compilable) {
      try {
        Compilable compilable = (Compilable) engine;
        this.compiledScript = compilable.compile(script);
        compiledScript.eval(); //parse script
      }
      catch (ScriptException e) {
        System.out.println("exception thrown whilst setting compilable: " + e.toString());
      }
    }
    else {
      System.out.println("Compilable is not implemented by engine: " + this.engineName);
    }
  }

  /**
   * Interprets and executes a script.
   * @param script the script to execute.
   * @return the result (if any).
   */
  public Object execute(String script){
    try {
      return this.engine.eval(script);
    }
    catch (ScriptException e) {
      System.out.println("error whilst executing script: " + e.toString());
    }
    return null;
  }

  /**
   * Executes a compiled script.
   * @param script the script to execute.
   * @return the result (if any).
   */
  private Object execute(CompiledScript script){
    try {
      return script.eval();
    }
    catch (ScriptException e) {
      System.out.println("error whilst executing compiled script: " + e.toString());
    }
    return null;
  }
}