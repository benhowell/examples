package net.benhowell.example;

import java.util.List;

/**
 * Created by Ben Howell [ben@benhowell.net] on 22-May-2014.
 */
public class ScriptDelegate {
  private Presenter presenter;

  public ScriptDelegate(Presenter presenter) {
    this.presenter = presenter;
  }

  public void dispatch (String message){
    presenter.print(message);
  }

  public void dispatch(List<String> messages) {
    presenter.print(messages);
  }
}
