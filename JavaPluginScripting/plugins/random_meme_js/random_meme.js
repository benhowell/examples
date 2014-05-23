/**
 * Crude example script.
 * Script is passed a ScriptDelegate which is accessed via the "delegate" variable.
 *
 * External Java packages and classes can be referenced in 3 different ways.
 * 1. by fully qualified name, e.g. com.example.Thing
 * 2. by global import using importClass() and by importPackage(). These are
 * globally accessible within the script.
 * 3. by scoped import using JavaImporter(). Code access to these scoped packages
 * and classes are enabled by surrounding them in a with(){ ... } block. e.g.
 * with(JavaScanner){ ... }
 *
 * Javascript has no native threading so there is a bit of cool hackery
 * going on in this script using Java threads and concurrency.
 *
 */

// not required for Java versions < Java 8
load("nashorn:mozilla_compat.js");

// Global imports
importClass(java.lang.Thread);
importClass(java.lang.Runnable);
importPackage(java.util.concurrent.locks);


// Scoped imports (just for an example)
var JavaScanner = new JavaImporter(
  java.util.Scanner,
  java.net.URL
);


/**
 * This is the entry point for this script.
 * Return the running script instance.
 */
function run(){
  return new RandomMeme();
}


/**
 * Return true if running, else false.
 */
function isRunning(){
  var self = instance;
  if(self != null)
    return self.thread.isAlive();
  else
    return false;
}


/**
 * Allows script to perform its own cleanup routine before shutting down.
 */
function shutDown(){
  var self = instance;
  self.threadCancelled = true;
  //block while waiting for thread to terminate
  while (self.thread.isAlive()){
    try {
      Thread.sleep(1000);
    }
    catch (e) {
      self.threadCancelled = true;
    }
  }
  return true;
}


/**
 * An interruptable sleep routine.
 */
function Sleeper(){
  var self = this;
  this.lock = new ReentrantLock();
  this.wake = this.lock.newCondition();

  /**
   * Starts a thread containing a sleep routine.
   * @param interval the sleep interval in seconds.
   */
  this.sleep = function(interval){
    self.thread = new Thread(new Runnable(){run: self.sleeper(self, interval)});
    self.thread.start();
    self.lock.lock();
    self.wake.await();
    self.lock.unlock();
  };


  /**
   * Interruptable sleep thread.
   * @param self a reference to our containing self who spawned this thread
   * routine (i.e. Sleeper().this).
   * @param interval the sleep interval in seconds
   * @return the inner function declaration.
   */
  this.sleeper = function(self, interval){
    function inner(){
      try {
	      Thread.sleep(interval * 1000);
      }
      catch (e) {
        var je = e.javaException;
        if (!(je instanceof java.lang.InterruptedException)) {
          delegate.dispatch("Unexpected error " + je.toString())
        }
      }
      finally{
        self.lock.lock();
        self.wake.signalAll();
        self.lock.unlock();
      }
    }
    return inner;
  };


  /**
   * Interrupts the sleep thread. This breaks sleep without waiting for sleep
   * interval to complete.
   */
  this.waken = function(){
    self.thread.interrupt();
  };
};


/**
 * Constructor.
 */
function RandomMeme(){
  var self = this;
  self.sleeper = new Sleeper();
  this.threadCancelled = false;
  this.thread = new Thread(new Runnable(){run: main(self)});
  this.thread.start();
};

/**
 * Main loop.
 * @param self a reference to our containing self who spawned this thread
 * routine (i.e. RandomMeme().this).
 * @return the inner function declaration.
 */
function main(self){
  function inner(){
    while(!self.threadCancelled){
      with(JavaScanner){
        var scanner = new Scanner(new URL('http://api.automeme.net/text?lines=3').openStream(), "UTF-8");
        delegate.dispatch(scanner.toString());
        scanner.close();
      }
      self.sleeper.sleep(3); // just wait around a bit...
    }
  }
  return inner;
}
