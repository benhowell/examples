"""
* Crude example script.
* Script is passed a ScriptDelegate which is accessed via the "delegate" variable.
"""

import time
from threading import Thread, InterruptedException
from urllib import urlopen
from java.lang import Boolean

def run():
  """
  This is the entry point for this script.
  Returns the running script instance.
  """
  return WeatherWatch()


def isRunning():
  """
  Return true if running, else false.
  """
  if instance is not None:
    if instance.is_running():
      return Boolean("True")
  return Boolean("False")


def shutDown():
  """
  Allows script to perform its own cleanup routine before shutting down.
  """
  instance.shut_down()


class WeatherWatch():
  def __init__(self):
    """
    Constructor.
    """
    self.thread_cancelled = False
    self.thread = Thread(target=self.run)
    self.thread.start()


  def run(self):
    """
    Main loop.
    """
    while not self.thread_cancelled:
      try:
        for line in urlopen('http://api.openweathermap.org/data/2.5/weather?q=Hobart,au'):
          delegate.dispatch(line + '\n')
        time.sleep(5) # just wait around a bit...
      except InterruptedException:
        self.thread_cancelled = True


  def is_running(self):
    """
    Returns true if main loop is running.
    """
    return self.thread.isAlive()


  def shut_down(self):
    """
    Performs cleanup routine.
    Return true after shutdown.
    """
    self.thread_cancelled = True
    while self.thread.isAlive():
      time.sleep(1)
    return True