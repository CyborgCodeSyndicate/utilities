package io.cyborgcode.utilities.logging.mock;


import io.cyborgcode.utilities.logging.LogCore;

public class DummyLogCore extends LogCore {

   public DummyLogCore(String loggerName, String markerName) {
      super(loggerName, markerName);
   }
}