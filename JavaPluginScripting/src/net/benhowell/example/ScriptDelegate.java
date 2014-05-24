package net.benhowell.example;

import java.util.List;

/**
 * Created by Ben Howell [ben@benhowell.net] on 22-May-2014.
 */
public class ScriptDelegate {

  public void dispatch(String data){
    System.out.println("new message: ");
    System.out.println(" " + data);
  }

  public void dispatch(List<String> data){
    System.out.println("new messages: ");
    for(String d: data)
      System.out.print(" " + d);
  }
}
