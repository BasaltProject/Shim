/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public interface Line extends java.lang.AutoCloseable {
  boolean isOpen();
  void close();
  void open() throws javax.sound.sampled.LineUnavailableException;
  javax.sound.sampled.Control getControl(javax.sound.sampled.Control.Type p0);
  javax.sound.sampled.Line.Info getLineInfo();
  javax.sound.sampled.Control[] getControls();
  boolean isControlSupported(javax.sound.sampled.Control.Type p0);
  void addLineListener(javax.sound.sampled.LineListener p0);
  void removeLineListener(javax.sound.sampled.LineListener p0);

  public static class Info {
    private final Class<?> lineClass;

    public Info(Class<?> lineClass) {
      if (lineClass == null) {
        this.lineClass = Line.class;
      } else {
        this.lineClass = lineClass;
      }
    }

    public Class<?> getLineClass() { return lineClass; }

    public boolean matches(Info info) {
      if (!(this.getClass().isInstance(info))) {
        return false;
      }
      Class<?> infoClass = info.getLineClass();
      if (!(getLineClass().isAssignableFrom(infoClass))) {
        return false;
      }
      return true;
    }

    public String toString() {
      String fullPackagePath = "javax.sound.sampled.";
      String initialString = new String(lineClass.toString());
      String finalString;
      int index = initialString.indexOf(fullPackagePath);
      if (index != -1) {
        finalString = initialString.substring(0, index)
            + initialString.substring((index + fullPackagePath.length()), initialString.length());
      } else {
        finalString = initialString;
      }
      return finalString;
    }
  }
}
