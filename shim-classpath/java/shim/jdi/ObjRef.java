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

public final class ObjRef {
  final Vm vm;
  final int tag;
  final long id;
  ObjectReference self;

  ObjRef(Vm vm, int tag, long id) {
    this.vm = vm;
    this.tag = tag;
    this.id = id;
  }

  private Out oid() {
    return new Out().id(id, vm.w.objectIdSize);
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public long uniqueID() {
    return id;
  }

  public ReferenceType referenceType() {
    In in = vm.call(9, 1, oid()).data;
    int t = in.u8();
    return vm.refTypeProxy(t, in.id(vm.w.typeIdSize));
  }

  public Type type() {
    return referenceType();
  }

  public Value getValue(Field f) {
    List<Field> l = new ArrayList<Field>();
    l.add(f);
    return getValues(l).get(f);
  }

  public Map<Field, Value> getValues(List<? extends Field> fs) {
    Out o = oid().i32(fs.size());
    for (Field f : fs) {
      o.id(((RefType.Fld) Proxies.implOf(f)).id, vm.w.fieldIdSize);
    }
    In in = vm.call(9, 2, o).data;
    int n = in.i32();
    Map<Field, Value> m = new LinkedHashMap<Field, Value>();
    for (int i = 0; i < n; i++) {
      m.put(fs.get(i), (Value) vm.readValue(in));
    }
    return m;
  }

  public void setValue(Field f, Value v) {
    Out o = oid().i32(1).id(((RefType.Fld) Proxies.implOf(f)).id, vm.w.fieldIdSize);
    writeUntagged(o, v);
    vm.call(9, 3, o);
  }

  private void writeUntagged(Out o, Object v) {
    Out tmp = new Out();
    vm.writeValue(tmp, v);
    byte[] b = tmp.bytes();
    for (int i = 1; i < b.length; i++) {
      o.u8(b[i]);
    }
  }

  public Value invokeMethod(ThreadReference thread, Method method, List<? extends Value> args, int options)
      throws InvocationException {
    RefType.Mth m = (RefType.Mth) Proxies.implOf(method);
    Out o = oid().id(vm.idOf(thread), vm.w.objectIdSize).id(m.owner.id, vm.w.typeIdSize)
        .id(m.id, vm.w.methodIdSize).i32(args.size());
    for (Value a : args) {
      vm.writeValue(o, a);
    }
    o.i32(options);
    In in = vm.call(9, 6, o).data;
    Object v = vm.readValue(in);
    in.u8();
    long ex = in.id(vm.w.objectIdSize);
    if (ex != 0) {
      throw new InvocationException(vm.<ObjectReference>obj('L', ex));
    }
    return (Value) v;
  }

  public boolean isCollected() {
    return vm.call(9, 9, oid()).data.bool();
  }

  public void disableCollection() {
    vm.call(9, 7, oid());
  }

  public void enableCollection() {
    vm.call(9, 8, oid());
  }

  public int entryCount() {
    return vm.call(9, 5, oid()).data.i32();
  }

  public List<ObjectReference> referringObjects(long max) {
    return new ArrayList<ObjectReference>();
  }

  public boolean equals(Object o) {
    Object i = Proxies.implOf(o);
    return i instanceof ObjRef && ((ObjRef) i).id == id;
  }

  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  public String toString() {
    if (tag == 's') {
      return "\"" + value() + "\"";
    }
    return "instance of " + referenceType().name() + "(id=" + id + ")";
  }

  /* ---------- StringReference ---------- */

  public String value() {
    return vm.call(10, 1, oid()).data.str();
  }

  /* ---------- ClassObjectReference ---------- */

  public ReferenceType reflectedType() {
    In in = vm.call(17, 1, oid()).data;
    int t = in.u8();
    return vm.refTypeProxy(t, in.id(vm.w.typeIdSize));
  }

  /* ---------- ClassLoaderReference ---------- */

  public List<ReferenceType> visibleClasses() {
    In in = vm.call(14, 1, oid()).data;
    int n = in.i32();
    List<ReferenceType> l = new ArrayList<ReferenceType>();
    for (int i = 0; i < n; i++) {
      int t = in.u8();
      l.add(vm.refTypeProxy(t, in.id(vm.w.typeIdSize)));
    }
    return l;
  }

  public List<ReferenceType> definedClasses() {
    List<ReferenceType> l = new ArrayList<ReferenceType>();
    for (ReferenceType t : vm.proxy.allClasses()) {
      if (self.equals(t.classLoader())) {
        l.add(t);
      }
    }
    return l;
  }

  /* ---------- ArrayReference ---------- */

  public int length() {
    return vm.call(13, 1, oid()).data.i32();
  }

  public Value getValueAt(int index) {
    return getValuesAt(index, 1).get(0);
  }

  public List<Value> getValuesAt(int first, int len) {
    if (len < 0) {
      len = length() - first;
    }
    In in = vm.call(13, 2, oid().i32(first).i32(len)).data;
    int t = in.u8();
    int n = in.i32();
    List<Value> l = new ArrayList<Value>();
    for (int i = 0; i < n; i++) {
      boolean obj = t == 'L' || t == '[' || t == 's' || t == 't' || t == 'g' || t == 'l' || t == 'c';
      l.add((Value) (obj ? vm.readValue(in) : vm.readUntagged(t, in)));
    }
    return Collections.unmodifiableList(l);
  }

  public List<Value> getValues() {
    return getValuesAt(0, -1);
  }

  public void setValueAt(int index, Value v) {
    Out o = oid().i32(index).i32(1);
    writeUntagged(o, v);
    vm.call(13, 3, o);
  }

  /* ---------- ThreadGroupReference ---------- */

  public String name() {
    return vm.call(tag == 'g' ? 12 : 11, 1, oid()).data.str();
  }

  public ThreadGroupReference parent() {
    long p = vm.call(12, 2, oid()).data.id(vm.w.objectIdSize);
    return p == 0 ? null : vm.<ThreadGroupReference>obj('g', p);
  }

  public List<ThreadReference> threads() {
    In in = vm.call(12, 3, oid()).data;
    int n = in.i32();
    List<ThreadReference> l = new ArrayList<ThreadReference>();
    for (int i = 0; i < n; i++) {
      l.add(vm.<ThreadReference>obj('t', in.id(vm.w.objectIdSize)));
    }
    return l;
  }

  public List<ThreadGroupReference> threadGroups() {
    In in = vm.call(12, 3, oid()).data;
    int n = in.i32();
    for (int i = 0; i < n; i++) {
      in.id(vm.w.objectIdSize);
    }
    int g = in.i32();
    List<ThreadGroupReference> l = new ArrayList<ThreadGroupReference>();
    for (int i = 0; i < g; i++) {
      l.add(vm.<ThreadGroupReference>obj('g', in.id(vm.w.objectIdSize)));
    }
    return l;
  }

  /* ---------- ThreadReference ---------- */

  public void suspend() {
    vm.call(tag == 'g' ? 1 : 11, 2, tag == 'g' ? null : oid());
  }

  public void resume() {
    vm.call(tag == 'g' ? 1 : 11, 3, tag == 'g' ? null : oid());
  }

  public int status() {
    int s = vm.call(11, 4, oid()).data.i32();
    return s == 0 ? ThreadReference.THREAD_STATUS_ZOMBIE : s == 1 ? ThreadReference.THREAD_STATUS_RUNNING
        : s == 2 ? ThreadReference.THREAD_STATUS_SLEEPING : s == 3 ? ThreadReference.THREAD_STATUS_MONITOR
        : s == 4 ? ThreadReference.THREAD_STATUS_WAIT : ThreadReference.THREAD_STATUS_UNKNOWN;
  }

  public boolean isSuspended() {
    In in = vm.call(11, 4, oid()).data;
    in.i32();
    return in.i32() == 1;
  }

  public boolean isAtBreakpoint() {
    try {
      return isSuspended() && frameCount() > 0;
    } catch (RuntimeException e) {
      return false;
    }
  }

  public int suspendCount() {
    return vm.call(11, 12, oid()).data.i32();
  }

  public ThreadGroupReference threadGroup() {
    return vm.<ThreadGroupReference>obj('g', vm.call(11, 5, oid()).data.id(vm.w.objectIdSize));
  }

  public int frameCount() {
    return vm.call(11, 7, oid()).data.i32();
  }

  public List<StackFrame> frames() {
    return frames(0, -1);
  }

  public List<StackFrame> frames(int start, int length) {
    In in = vm.call(11, 6, oid().i32(start).i32(length)).data;
    int n = in.i32();
    List<StackFrame> l = new ArrayList<StackFrame>();
    for (int i = 0; i < n; i++) {
      long fid = in.id(vm.w.frameIdSize);
      Location loc = vm.readLocation(in);
      l.add(Frame.make(vm, this, fid, loc));
    }
    return Collections.unmodifiableList(l);
  }

  public StackFrame frame(int index) {
    List<StackFrame> l = frames(index, 1);
    if (l.isEmpty()) {
      throw new IndexOutOfBoundsException();
    }
    return l.get(0);
  }

  public void interrupt() {
    vm.call(11, 11, oid());
  }

  public void stop(ObjectReference exception) {
    vm.call(11, 10, oid().id(vm.idOf(exception), vm.w.objectIdSize));
  }

  public ObjectReference currentContendedMonitor() {
    long o = vm.call(11, 9, oid()).data.id(vm.w.objectIdSize);
    return vm.<ObjectReference>obj('L', o);
  }

  public List<ObjectReference> ownedMonitors() {
    In in = vm.call(11, 8, oid()).data;
    int n = in.i32();
    List<ObjectReference> l = new ArrayList<ObjectReference>();
    for (int i = 0; i < n; i++) {
      l.add((ObjectReference) vm.readValue(in));
    }
    return l;
  }

  public void popFrames(StackFrame f) {
    Frame fr = (Frame) Proxies.implOf(f);
    vm.call(16, 4, new Out().id(id, vm.w.objectIdSize).id(fr.fid, vm.w.frameIdSize));
  }
}
