/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import shim.jdi.Wire.In;
import shim.jdi.Wire.Out;

public final class Frame {
  final Vm vm;
  final ObjRef thread;
  final long fid;
  final Location loc;

  private Frame(Vm vm, ObjRef thread, long fid, Location loc) {
    this.vm = vm;
    this.thread = thread;
    this.fid = fid;
    this.loc = loc;
  }

  static StackFrame make(Vm vm, ObjRef thread, long fid, Location loc) {
    return Proxies.make(new Frame(vm, thread, fid, loc), StackFrame.class);
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public Location location() {
    return loc;
  }

  public ThreadReference thread() {
    return (ThreadReference) thread.self;
  }

  public ObjectReference thisObject() {
    In in = vm.call(16, 3, new Out().id(thread.id, vm.w.objectIdSize).id(fid, vm.w.frameIdSize)).data;
    Object v = vm.readValue(in);
    return (ObjectReference) v;
  }

  public List<LocalVariable> visibleVariables() throws AbsentInformationException {
    List<LocalVariable> l = new ArrayList<LocalVariable>();
    StackFrame self = null;
    for (LocalVariable v : loc.method().variables()) {
      long idx = loc.codeIndex();
      RefType.Var var = (RefType.Var) Proxies.implOf(v);
      if (idx >= var.codeIndex && idx < var.codeIndex + var.length) {
        l.add(v);
      }
    }
    return l;
  }

  public LocalVariable visibleVariableByName(String name) throws AbsentInformationException {
    for (LocalVariable v : visibleVariables()) {
      if (v.name().equals(name)) {
        return v;
      }
    }
    return null;
  }

  public Value getValue(LocalVariable v) {
    List<LocalVariable> l = new ArrayList<LocalVariable>();
    l.add(v);
    return getValues(l).get(v);
  }

  public Map<LocalVariable, Value> getValues(List<? extends LocalVariable> vars) {
    Out o = new Out().id(thread.id, vm.w.objectIdSize).id(fid, vm.w.frameIdSize).i32(vars.size());
    for (LocalVariable v : vars) {
      RefType.Var var = (RefType.Var) Proxies.implOf(v);
      o.i32(var.slot).u8(var.sig.charAt(0) == 'L' || var.sig.charAt(0) == '[' ? var.sig.charAt(0) : var.sig.charAt(0));
    }
    In in = vm.call(16, 1, o).data;
    int n = in.i32();
    Map<LocalVariable, Value> m = new LinkedHashMap<LocalVariable, Value>();
    for (int i = 0; i < n; i++) {
      m.put(vars.get(i), (Value) vm.readValue(in));
    }
    return m;
  }

  public void setValue(LocalVariable v, Value value) {
    RefType.Var var = (RefType.Var) Proxies.implOf(v);
    Out o = new Out().id(thread.id, vm.w.objectIdSize).id(fid, vm.w.frameIdSize).i32(1).i32(var.slot);
    vm.writeValue(o, value);
    vm.call(16, 2, o);
  }

  public List<Value> getArgumentValues() {
    try {
      List<LocalVariable> args = loc.method().arguments();
      List<Value> l = new ArrayList<Value>(getValues(args).values());
      return Collections.unmodifiableList(l);
    } catch (AbsentInformationException e) {
      throw new InternalException("absent information");
    }
  }

  public boolean equals(Object o) {
    Object i = Proxies.implOf(o);
    return i instanceof Frame && ((Frame) i).fid == fid;
  }

  public int hashCode() {
    return (int) fid;
  }

  public String toString() {
    return loc + " in thread " + thread.id;
  }
}
