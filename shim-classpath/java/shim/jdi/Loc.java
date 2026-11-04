/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.*;
import shim.jdi.Wire.Out;

public final class Loc {
  final Vm vm;
  final RefType type;
  final long method;
  final long index;
  int line = -2;

  private Loc(Vm vm, RefType type, long method, long index) {
    this.vm = vm;
    this.type = type;
    this.method = method;
    this.index = index;
  }

  static Location make(Vm vm, RefType type, long method, long index) {
    return Proxies.make(new Loc(vm, type, method, index), Location.class);
  }

  static Location make(Vm vm, RefType type, long method, long index, int line) {
    Loc l = new Loc(vm, type, method, index);
    l.line = line;
    return Proxies.make(l, Location.class);
  }

  void write(Out o) {
    o.u8(type.tag).id(type.id, vm.w.typeIdSize).id(method, vm.w.methodIdSize).i64(index);
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public ReferenceType declaringType() {
    return type.self;
  }

  public Method method() {
    RefType.Mth m = type.methodById(method);
    return m == null ? null : m.self;
  }

  public long codeIndex() {
    return index;
  }

  public int lineNumber() {
    if (line == -2) {
      line = -1;
      try {
        for (Location l : ((Method) method()).allLineLocations()) {
          if (l.codeIndex() <= index && l.lineNumber() > 0) {
            line = l.lineNumber();
          }
        }
      } catch (AbsentInformationException e) {
        line = -1;
      }
    }
    return line;
  }

  public int lineNumber(String stratum) {
    return lineNumber();
  }

  public String sourceName() throws AbsentInformationException {
    return type.sourceName();
  }

  public String sourceName(String stratum) throws AbsentInformationException {
    return type.sourceName();
  }

  public String sourcePath() throws AbsentInformationException {
    String n = type.sourceName();
    String t = type.name();
    int dot = t.lastIndexOf('.');
    return dot < 0 ? n : t.substring(0, dot).replace('.', '/') + "/" + n;
  }

  public String sourcePath(String stratum) throws AbsentInformationException {
    return sourcePath();
  }

  public int compareTo(Object o) {
    Loc other = (Loc) Proxies.implOf(o);
    int c = Long.compare(method, other.method);
    return c != 0 ? c : Long.compare(index, other.index);
  }

  public boolean equals(Object o) {
    Object i = Proxies.implOf(o);
    return i instanceof Loc && ((Loc) i).method == method && ((Loc) i).index == index
        && ((Loc) i).type.id == type.id;
  }

  public int hashCode() {
    return (int) (method * 31 + index);
  }

  public String toString() {
    int l = lineNumber();
    return l >= 0 ? type.name() + ":" + l : type.name() + "." + ((Method) method()).name();
  }
}
