/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

import java.util.EventObject;

public class LineEvent extends EventObject {
  private static final long serialVersionUID = -1274246333383880410L;

  private final Type type;
  private final long position;

  public LineEvent(Line line, Type type, long position) {
    super(line);
    this.type = type;
    this.position = position;
  }

  public final Line getLine() { return (Line) getSource(); }

  public final Type getType() { return type; }

  public final long getFramePosition() { return position; }

  public String toString() {
    String sType = "";
    if (type != null) {
      sType = type.toString() + " ";
    }
    String sLine;
    if (getLine() == null) {
      sLine = "null";
    } else {
      sLine = getLine().toString();
    }
    return new String(sType + "event from line " + sLine);
  }

  public static class Type {
    private final String name;

    public static final Type OPEN = new Type("Open");
    public static final Type CLOSE = new Type("Close");
    public static final Type START = new Type("Start");
    public static final Type STOP = new Type("Stop");

    protected Type(String name) { this.name = name; }

    public final boolean equals(Object obj) { return super.equals(obj); }

    public final int hashCode() { return super.hashCode(); }

    public String toString() { return name; }
  }
}
