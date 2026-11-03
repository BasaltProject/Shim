/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public interface Port extends javax.sound.sampled.Line {

  public static class Info extends Line.Info {
    public static final Info MICROPHONE = new Info(Port.class, "MICROPHONE", true);
    public static final Info LINE_IN = new Info(Port.class, "LINE_IN", true);
    public static final Info COMPACT_DISC = new Info(Port.class, "COMPACT_DISC", true);
    public static final Info SPEAKER = new Info(Port.class, "SPEAKER", false);
    public static final Info HEADPHONE = new Info(Port.class, "HEADPHONE", false);
    public static final Info LINE_OUT = new Info(Port.class, "LINE_OUT", false);

    private String name;
    private boolean isSource;

    public Info(Class<?> lineClass, String name, boolean isSource) {
      super(lineClass);
      this.name = name;
      this.isSource = isSource;
    }

    public String getName() { return name; }

    public boolean isSource() { return isSource; }

    public boolean matches(Line.Info info) {
      if (!(super.matches(info))) {
        return false;
      }
      if (!(name.equals(((Info) info).getName()))) {
        return false;
      }
      if (isSource != ((Info) info).isSource()) {
        return false;
      }
      return true;
    }

    public final boolean equals(Object obj) { return super.equals(obj); }

    public final int hashCode() { return super.hashCode(); }

    public final String toString() { return (name + ((isSource == true) ? " source" : " target") + " port"); }
  }
}
