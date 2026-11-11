/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 25
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.EnumSet;

public class RecordClassesModern {
  static void show(String name) throws Exception {
    Class<?> c = Class.forName(name);
    System.out.println(name + " record=" + c.isRecord() + " mods=" + Modifier.toString(c.getModifiers()));
    for (RecordComponent rc : c.getRecordComponents()) {
      System.out.println("  " + rc.getGenericType().getTypeName() + " " + rc.getName() + " via "
          + rc.getAccessor().getName());
    }
    System.out.println("  ctors=" + c.getDeclaredConstructors().length);
  }

  public static void main(String[] args) throws Exception {
    show("jdk.jshell.SourceCodeAnalysis$Highlight");
    show("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter$TargetDescription");
    show("jdk.net.UnixDomainPrincipal");
    var a = new jdk.jshell.SourceCodeAnalysis.Highlight(1, 4, EnumSet.of(jdk.jshell.SourceCodeAnalysis.Attribute.KEYWORD));
    var b = new jdk.jshell.SourceCodeAnalysis.Highlight(1, 4, EnumSet.of(jdk.jshell.SourceCodeAnalysis.Attribute.KEYWORD));
    System.out.println(a + " " + a.equals(b) + " " + (a.hashCode() == b.hashCode()));
    try {
      new jdk.net.UnixDomainPrincipal(null, null);
    } catch (NullPointerException e) {
      System.out.println("npe");
    }
  }
}
