/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import p.Target;

public class AccessChecks {
  interface Probe { int run(Target target); }

  static class Child extends Target {
    int protectedOwn() { return protectedMethod() + protectedField; }
  }

  static void probe(String label, Probe probe) {
    try {
      System.out.println(label + ":" + probe.run(new Target()));
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName());
    }
  }

  static int privateField(Target t) { return t.privateField; }
  static int packageField(Target t) { return t.packageField; }
  static int protectedField(Target t) { return t.protectedField; }
  static int privateStatic(Target t) { return Target.privateStatic(); }
  static int packageMethod(Target t) { return t.packageMethod(); }
  static int protectedMethod(Target t) { return t.protectedMethod(); }

  public static void main(String[] args) {
    probe("private-field", AccessChecks::privateField);
    probe("package-field", AccessChecks::packageField);
    probe("protected-field", AccessChecks::protectedField);
    probe("private-static", AccessChecks::privateStatic);
    probe("package-method", AccessChecks::packageMethod);
    probe("protected-method", AccessChecks::protectedMethod);
    probe("public-field", t -> t.publicField);
    probe("public-method", t -> t.publicMethod());
    probe("class-static", t -> p.Hidden.value());
    probe("class-array", t -> new p.Hidden[2].length);
    probe("class-instanceof", t -> ((Object) t instanceof p.Hidden) ? 1 : 0);
    probe("nest", t -> Target.nested());
    probe("subclass", t -> new Child().protectedOwn());
  }
}
