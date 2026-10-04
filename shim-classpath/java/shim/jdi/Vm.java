/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import shim.jdi.Wire.In;
import shim.jdi.Wire.Out;

public final class Vm implements Wire.EventSink {
  final Wire w;
  VirtualMachine proxy;
  final Map<Long, RefType> types = new HashMap<Long, RefType>();
  final Map<Long, ObjRef> objects = new HashMap<Long, ObjRef>();
  final Map<Integer, Req> requests = new HashMap<Integer, Req>();
  final LinkedBlockingQueue<EventSet> queue = new LinkedBlockingQueue<EventSet>();
  String description;
  String vmName;
  String vmVersion;
  int major;
  int minor;
  volatile boolean disposed;
  Process process;

  public Vm(Wire w) throws IOException {
    this.w = w;
    w.start(this);
    In v = w.sendOk(1, 1, null).data;
    description = v.str();
    major = v.i32();
    minor = v.i32();
    vmVersion = v.str();
    vmName = v.str();
    In s = w.sendOk(1, 7, null).data;
    w.fieldIdSize = s.i32();
    w.methodIdSize = s.i32();
    w.objectIdSize = s.i32();
    w.typeIdSize = s.i32();
    w.frameIdSize = s.i32();
    proxy = Proxies.make(new VmImpl(), VirtualMachine.class);
  }

  public VirtualMachine machine() {
    return proxy;
  }

  Wire.Reply call(int set, int cmd, Out body) {
    try {
      Wire.Reply r = w.send(set, cmd, body);
      if (r.error != 0) {
        throw mapError(r.error);
      }
      return r;
    } catch (IOException e) {
      throw new VMDisconnectedException(e.getMessage());
    }
  }

  RuntimeException mapError(int code) {
    switch (code) {
      case 20:
        return new ObjectCollectedException();
      case 10:
      case 11:
        return new ObjectCollectedException();
      case 13:
        return new IllegalThreadStateExceptionWrapper();
      case 15:
        return new InvalidStackFrameException();
      case 31:
        return new AbsentInfoWrapper();
      case 101:
      case 102:
        return new ClassNotPreparedException();
      case 112:
        return new VMOutOfMemoryException("out of memory");
      default:
        return new InternalException("JDWP error " + code, code);
    }
  }

  static final class IllegalThreadStateExceptionWrapper extends RuntimeException {
    IllegalThreadStateExceptionWrapper() {
      super("thread not suspended");
    }
  }

  static final class AbsentInfoWrapper extends RuntimeException {
    AbsentInfoWrapper() {
      super("absent information");
    }
  }

  private long id(In in, int n) {
    return in.id(n);
  }

  /* ---------- id/signature helpers ---------- */

  RefType refType(int tag, long id) {
    synchronized (types) {
      RefType t = types.get(id);
      if (t == null) {
        t = new RefType(this, tag, id);
        t.self = tag == 3 ? Proxies.<ReferenceType>make(t, ArrayType.class)
            : tag == 2 ? Proxies.<ReferenceType>make(t, InterfaceType.class)
            : Proxies.<ReferenceType>make(t, ClassType.class);
        types.put(id, t);
      }
      return t;
    }
  }

  ReferenceType refTypeProxy(int tag, long id) {
    return refType(tag, id).self;
  }

  Object objectOrNull(int tag, long id) {
    if (id == 0) {
      return null;
    }
    synchronized (objects) {
      ObjRef o = objects.get(id);
      if (o == null) {
        o = new ObjRef(this, tag, id);
        Class<?> iface = tag == 's' ? StringReference.class : tag == 't' ? ThreadReference.class
            : tag == 'g' ? ThreadGroupReference.class : tag == 'l' ? ClassLoaderReference.class
            : tag == 'c' ? ClassObjectReference.class : tag == '[' ? ArrayReference.class : ObjectReference.class;
        o.self = Proxies.<ObjectReference>make(o, iface);
        objects.put(id, o);
      }
      return o.self;
    }
  }

  @SuppressWarnings("unchecked")
  <T> T obj(int tag, long id) {
    return (T) objectOrNull(tag, id);
  }

  ObjRef impl(Object proxy) {
    if (proxy == null) {
      return null;
    }
    Object o = Proxies.implOf(proxy);
    if (o instanceof ObjRef) {
      return (ObjRef) o;
    }
    throw new IllegalArgumentException("not an object mirror");
  }

  long idOf(Object ref) {
    return ref == null ? 0 : impl(ref).id;
  }

  Object readValue(In in) {
    int tag = in.u8();
    return readUntagged(tag, in);
  }

  Object readUntagged(int tag, In in) {
    switch (tag) {
      case 'B':
        return Prim.of(this, 'B', Byte.valueOf((byte) in.u8()));
      case 'C':
        return Prim.of(this, 'C', Character.valueOf((char) in.i16()));
      case 'F':
        return Prim.of(this, 'F', Float.valueOf(Float.intBitsToFloat(in.i32())));
      case 'D':
        return Prim.of(this, 'D', Double.valueOf(Double.longBitsToDouble(in.i64())));
      case 'I':
        return Prim.of(this, 'I', Integer.valueOf(in.i32()));
      case 'J':
        return Prim.of(this, 'J', Long.valueOf(in.i64()));
      case 'S':
        return Prim.of(this, 'S', Short.valueOf((short) in.i16()));
      case 'Z':
        return Prim.of(this, 'Z', Boolean.valueOf(in.bool()));
      case 'V':
        return Prim.of(this, 'V', null);
      default:
        return objectOrNull(tag, in.id(w.objectIdSize));
    }
  }

  void writeValue(Out o, Object v) {
    if (v == null) {
      o.u8('L').id(0, w.objectIdSize);
      return;
    }
    if (v instanceof Value) {
      Object impl = Proxies.implOf(v);
      if (impl instanceof Prim) {
        ((Prim) impl).write(o);
        return;
      }
      ObjRef r = impl(v);
      o.u8(r.tag).id(r.id, w.objectIdSize);
      return;
    }
    throw new IllegalArgumentException("not a value");
  }

  Type typeOfSignature(String sig) throws ClassNotLoadedException {
    char c = sig.charAt(0);
    switch (c) {
      case 'Z':
      case 'B':
      case 'C':
      case 'S':
      case 'I':
      case 'J':
      case 'F':
      case 'D':
      case 'V':
        return Prim.type(this, c);
      default:
        List<ReferenceType> l = classesBySignature(sig);
        if (l.isEmpty()) {
          throw new ClassNotLoadedException(sig.substring(1, sig.length() - 1).replace('/', '.'));
        }
        return l.get(0);
    }
  }

  List<ReferenceType> classesBySignature(String sig) {
    In in = call(1, 2, new Out().str(sig)).data;
    int n = in.i32();
    List<ReferenceType> l = new ArrayList<ReferenceType>();
    for (int i = 0; i < n; i++) {
      int tag = in.u8();
      long id = in.id(w.typeIdSize);
      in.i32();
      RefType t = refType(tag, id);
      t.signature = sig;
      l.add(t.self);
    }
    return l;
  }

  Location readLocation(In in) {
    int tag = in.u8();
    long cls = in.id(w.typeIdSize);
    long meth = in.id(w.methodIdSize);
    long idx = in.i64();
    return Loc.make(this, refType(tag, cls), meth, idx);
  }

  /* ---------- VirtualMachine ---------- */

  public final class VmImpl {
    public VirtualMachine virtualMachine() {
      return proxy;
    }

    public String name() {
      return vmName;
    }

    public String version() {
      return vmVersion;
    }

    public String description() {
      return description;
    }

    public int jdwpMajor() {
      return major;
    }

    public int jdwpMinor() {
      return minor;
    }

    public List<ReferenceType> allClasses() {
      In in = call(1, 3, null).data;
      int n = in.i32();
      List<ReferenceType> l = new ArrayList<ReferenceType>();
      for (int i = 0; i < n; i++) {
        int tag = in.u8();
        long id = in.id(w.typeIdSize);
        String sig = in.str();
        in.i32();
        RefType t = refType(tag, id);
        t.signature = sig;
        l.add(t.self);
      }
      return Collections.unmodifiableList(l);
    }

    public List<ReferenceType> classesByName(String name) {
      String sig;
      if (name.endsWith("[]")) {
        String base = name.substring(0, name.length() - 2);
        sig = "[" + sigOfName(base);
      } else {
        sig = "L" + name.replace('.', '/') + ";";
      }
      return Collections.unmodifiableList(classesBySignature(sig));
    }

    public List<ThreadReference> allThreads() {
      In in = call(1, 4, null).data;
      int n = in.i32();
      List<ThreadReference> l = new ArrayList<ThreadReference>();
      for (int i = 0; i < n; i++) {
        l.add(Vm.this.<ThreadReference>obj('t', in.id(w.objectIdSize)));
      }
      return Collections.unmodifiableList(l);
    }

    public List<ThreadGroupReference> topLevelThreadGroups() {
      In in = call(1, 5, null).data;
      int n = in.i32();
      List<ThreadGroupReference> l = new ArrayList<ThreadGroupReference>();
      for (int i = 0; i < n; i++) {
        l.add(Vm.this.<ThreadGroupReference>obj('g', in.id(w.objectIdSize)));
      }
      return Collections.unmodifiableList(l);
    }

    public void suspend() {
      call(1, 8, null);
    }

    public void resume() {
      call(1, 9, null);
    }

    public void exit(int code) {
      try {
        w.send(1, 10, new Out().i32(code));
      } catch (IOException e) {
        // target exits before replying
      }
      disposed = true;
    }

    public void dispose() {
      try {
        w.send(1, 6, null);
      } catch (IOException e) {
        // already gone
      }
      disposed = true;
      w.close();
    }

    public Process process() {
      return process;
    }

    public StringReference mirrorOf(String s) {
      In in = call(1, 11, new Out().str(s)).data;
      return Vm.this.<StringReference>obj('s', in.id(w.objectIdSize));
    }

    public BooleanValue mirrorOf(boolean v) {
      return Prim.of(Vm.this, 'Z', v);
    }

    public ByteValue mirrorOf(byte v) {
      return Prim.of(Vm.this, 'B', v);
    }

    public CharValue mirrorOf(char v) {
      return Prim.of(Vm.this, 'C', v);
    }

    public ShortValue mirrorOf(short v) {
      return Prim.of(Vm.this, 'S', v);
    }

    public IntegerValue mirrorOf(int v) {
      return Prim.of(Vm.this, 'I', v);
    }

    public LongValue mirrorOf(long v) {
      return Prim.of(Vm.this, 'J', v);
    }

    public FloatValue mirrorOf(float v) {
      return Prim.of(Vm.this, 'F', v);
    }

    public DoubleValue mirrorOf(double v) {
      return Prim.of(Vm.this, 'D', v);
    }

    public VoidValue mirrorOfVoid() {
      return Prim.of(Vm.this, 'V', null);
    }

    public EventQueue eventQueue() {
      return eventQueueProxy;
    }

    public EventRequestManager eventRequestManager() {
      return erm;
    }

    public boolean canWatchFieldModification() {
      return caps()[0];
    }

    public boolean canWatchFieldAccess() {
      return caps()[1];
    }

    public boolean canGetBytecodes() {
      return caps()[2];
    }

    public boolean canGetSyntheticAttribute() {
      return caps()[3];
    }

    public boolean canGetOwnedMonitorInfo() {
      return caps()[4];
    }

    public boolean canGetCurrentContendedMonitor() {
      return caps()[5];
    }

    public boolean canGetMonitorInfo() {
      return caps()[6];
    }

    public boolean canUseInstanceFilters() {
      return caps()[8];
    }

    public boolean canRedefineClasses() {
      return caps()[9];
    }

    public boolean canAddMethod() {
      return false;
    }

    public boolean canUnrestrictedlyRedefineClasses() {
      return false;
    }

    public boolean canPopFrames() {
      return caps()[11];
    }

    public boolean canGetSourceDebugExtension() {
      return caps()[13];
    }

    public boolean canRequestVMDeathEvent() {
      return caps()[14];
    }

    public boolean canGetMethodReturnValues() {
      return caps()[15];
    }

    public boolean canForceEarlyReturn() {
      return caps()[20];
    }

    public boolean canBeModified() {
      return true;
    }

    public boolean canGetInstanceInfo() {
      return caps()[17];
    }

    public boolean canUseSourceNameFilters() {
      return caps()[19];
    }

    public boolean canGetClassFileVersion() {
      return true;
    }

    public boolean canGetConstantPool() {
      return caps()[18];
    }

    public boolean canRequestMonitorEvents() {
      return caps()[16];
    }

    public boolean canGetMonitorFrameInfo() {
      return caps()[21];
    }

    public boolean canGetSourceDebugExtension_() {
      return false;
    }

    public void setDebugTraceMode(int mode) {
    }

    public String getDefaultStratum() {
      return "Java";
    }

    public void setDefaultStratum(String s) {
    }

    public void redefineClasses(Map<?, ?> m) {
      throw new UnsupportedOperationException();
    }

    public long[] instanceCounts(List<?> types) {
      long[] r = new long[types.size()];
      return r;
    }

    public List<String> classPath() {
      In in = call(1, 13, null).data;
      in.str();
      int n = in.i32();
      List<String> l = new ArrayList<String>();
      for (int i = 0; i < n; i++) {
        l.add(in.str());
      }
      return l;
    }

    public List<String> bootClassPath() {
      In in = call(1, 13, null).data;
      in.str();
      int n = in.i32();
      for (int i = 0; i < n; i++) {
        in.str();
      }
      int b = in.i32();
      List<String> l = new ArrayList<String>();
      for (int i = 0; i < b; i++) {
        l.add(in.str());
      }
      return l;
    }

    public String baseDirectory() {
      return call(1, 13, null).data.str();
    }

    public boolean[] caps() {
      return capabilities();
    }

    public String toString() {
      return description;
    }
  }

  private boolean[] capCache;

  boolean[] capabilities() {
    if (capCache == null) {
      In in = call(1, 17, null).data;
      boolean[] b = new boolean[32];
      for (int i = 0; i < 22 && in.more(); i++) {
        b[i] = in.bool();
      }
      capCache = b;
    }
    return capCache;
  }

  static String sigOfName(String n) {
    switch (n) {
      case "boolean":
        return "Z";
      case "byte":
        return "B";
      case "char":
        return "C";
      case "short":
        return "S";
      case "int":
        return "I";
      case "long":
        return "J";
      case "float":
        return "F";
      case "double":
        return "D";
      default:
        if (n.endsWith("[]")) {
          return "[" + sigOfName(n.substring(0, n.length() - 2));
        }
        return "L" + n.replace('.', '/') + ";";
    }
  }

  static String nameOfSig(String sig) {
    switch (sig.charAt(0)) {
      case 'Z':
        return "boolean";
      case 'B':
        return "byte";
      case 'C':
        return "char";
      case 'S':
        return "short";
      case 'I':
        return "int";
      case 'J':
        return "long";
      case 'F':
        return "float";
      case 'D':
        return "double";
      case 'V':
        return "void";
      case '[':
        return nameOfSig(sig.substring(1)) + "[]";
      default:
        return sig.substring(1, sig.length() - 1).replace('/', '.');
    }
  }

  /* ---------- events ---------- */

  final EventQueue eventQueueProxy = Proxies.make(new QueueImpl(), EventQueue.class);
  final EventRequestManager erm = Proxies.make(new Req.Manager(this), EventRequestManager.class);

  public final class QueueImpl {
    public VirtualMachine virtualMachine() {
      return proxy;
    }

    public EventSet remove() throws InterruptedException {
      return remove(0);
    }

    public EventSet remove(long timeout) throws InterruptedException {
      while (true) {
        EventSet s = timeout <= 0 ? queue.poll(100, TimeUnit.MILLISECONDS) : queue.poll(timeout, TimeUnit.MILLISECONDS);
        if (s != null) {
          return s;
        }
        if (disposed || w.isClosed()) {
          throw new VMDisconnectedException();
        }
        if (timeout > 0) {
          return null;
        }
      }
    }
  }

  public void events(In in) {
    int policy = in.u8();
    int n = in.i32();
    List<Event> evs = new ArrayList<Event>();
    boolean death = false;
    for (int i = 0; i < n; i++) {
      int kind = in.u8();
      int reqId = in.i32();
      Req req = requests.get(reqId);
      Evt e = new Evt(this, kind, req);
      switch (kind) {
        case 90:
        case 6:
        case 7:
          e.thread = Vm.this.<ThreadReference>obj('t', in.id(w.objectIdSize));
          break;
        case 1:
        case 2:
        case 40:
        case 41:
        case 42:
          e.thread = Vm.this.<ThreadReference>obj('t', in.id(w.objectIdSize));
          e.location = readLocation(in);
          if (kind == 42) {
            e.value = readValue(in);
          }
          break;
        case 4:
          e.thread = Vm.this.<ThreadReference>obj('t', in.id(w.objectIdSize));
          e.location = readLocation(in);
          e.exception = readValue(in);
          e.catchLocation = readLocation(in);
          break;
        case 8:
        case 10:
          e.thread = Vm.this.<ThreadReference>obj('t', in.id(w.objectIdSize));
          int tag = in.u8();
          long tid = in.id(w.typeIdSize);
          String sig = in.str();
          in.i32();
          e.refType = refType(tag, tid);
          e.refType.signature = sig;
          break;
        case 9:
          e.className = in.str();
          break;
        case 99:
          death = true;
          break;
        default:
          break;
      }
      Event ev = e.make();
      if (ev != null) {
        evs.add(ev);
      }
    }
    if (!evs.isEmpty()) {
      queue.add(new EvSet(this, evs, policy));
    }
    if (death) {
      disposed = true;
    }
  }

  public void closed() {
    disposed = true;
    List<Event> l = new ArrayList<Event>();
    l.add(Proxies.<VMDisconnectEvent>make(new Evt(this, -1, null), VMDisconnectEvent.class));
    queue.add(new EvSet(this, l, 0));
  }
}
