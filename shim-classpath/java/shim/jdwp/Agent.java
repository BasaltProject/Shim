/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdwp;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** A JDWP agent running inside the VM, started from the -agentlib:jdwp launcher option. */
public final class Agent {
  private static final int E_INVALID_THREAD = 10;
  private static final int E_INVALID_OBJECT = 20;
  private static final int E_INVALID_CLASS = 21;
  private static final int E_INVALID_METHODID = 23;
  private static final int E_INVALID_FIELDID = 25;
  private static final int E_ABSENT_INFORMATION = 101;
  private static final int E_NOT_IMPLEMENTED = 99;
  private static final int E_ILLEGAL_ARGUMENT = 103;

  private static final int K_STEP = 1;
  private static final int K_BREAKPOINT = 2;
  private static final int K_EXCEPTION = 4;
  private static final int K_THREAD_START = 6;
  private static final int K_THREAD_DEATH = 7;
  private static final int K_CLASS_PREPARE = 8;
  private static final int K_METHOD_ENTRY = 40;
  private static final int K_METHOD_EXIT = 41;
  private static final int K_VM_DEATH = 99;

  public static volatile boolean eventsActive;

  private static volatile Agent instance;

  private static native void setHooks(boolean on);

  private static native void setArmed(int mask);

  private static native int[] lineTable(Class<?> c, String name, String desc);

  public static native Object[] allClasses(ClassLoader loader);

  private final Map<Long, Object> objects = new HashMap<Long, Object>();
  private final Map<Object, Long> ids = new IdentityHashMap<Object, Long>();
  private final Map<Long, Executable> methods = new HashMap<Long, Executable>();
  private final Map<Long, Field> fields = new HashMap<Long, Field>();
  private final Map<Executable, Long> methodIds = new HashMap<Executable, Long>();
  private final Map<Field, Long> fieldIds = new HashMap<Field, Long>();
  private final Map<Thread, Integer> suspend = new IdentityHashMap<Thread, Integer>();
  private final Map<Long, Object[]> frames = new HashMap<Long, Object[]>();
  private final Map<Integer, Req> requests = new TreeMap<Integer, Req>();
  private final Map<Thread, Stop> stops = new IdentityHashMap<Thread, Stop>();
  private final Map<Thread, Boolean> inside = new IdentityHashMap<Thread, Boolean>();
  private final LinkedHashSet<Class<?>> seen = new LinkedHashSet<Class<?>>();
  private long next = 1;
  private int nextRequest = 1;
  private int nextPacket = 1;
  private int vmSuspend;
  private DataInputStream in;
  private DataOutputStream out;
  private volatile boolean resumed;
  private boolean suspendOnStart;
  private volatile boolean attached;
  private Thread startThread;
  private final Object lock = new Object();

  private static final class Req {
    int id;
    int kind;
    int policy;
    int count = 1;
    Thread thread;
    Class<?> only;
    List<String> match = new ArrayList<String>();
    List<String> exclude = new ArrayList<String>();
    Class<?> locClass;
    String locName;
    String locDesc;
    long locIndex;
    int stepSize;
    int stepDepth;
    int startDepth;
    int startLine;
    Class<?> startClass;
    String startName;
    boolean done;
    Class<?> excClass;
    boolean caught = true;
    boolean uncaught = true;
  }

  private static final class Shadow {
    Class<?> c;
    String name;
    String desc;
    Locals.Snap snap;
  }

  private final Map<Thread, ArrayList<Shadow>> shadow = new java.util.WeakHashMap<Thread, ArrayList<Shadow>>();

  private void track(Thread t, Class<?> c, String name, String desc, int ip, boolean exit, Locals.Snap snap) {
    ArrayList<Shadow> st;
    synchronized (shadow) {
      st = shadow.get(t);
      if (st == null) {
        st = new ArrayList<Shadow>();
        shadow.put(t, st);
      }
    }
    synchronized (st) {
      if (exit) {
        while (!st.isEmpty()) {
          Shadow top = st.remove(st.size() - 1);
          if (top.c == c && top.name.equals(name) && top.desc.equals(desc)) {
            break;
          }
        }
        return;
      }
      if (ip != 0) {
        int i = st.size() - 1;
        while (i >= 0 && !(st.get(i).c == c && st.get(i).name.equals(name) && st.get(i).desc.equals(desc))) {
          i--;
        }
        if (i >= 0) {
          while (st.size() - 1 > i) {
            st.remove(st.size() - 1);
          }
          st.get(i).snap = snap;
          return;
        }
      }
      Shadow sh = new Shadow();
      sh.c = c;
      sh.name = name;
      sh.desc = desc;
      sh.snap = snap;
      st.add(sh);
    }
  }

  private Locals.Snap shadowFor(Thread t, int frameIndex) {
    StackTraceElement[] real = framesOf(t);
    ArrayList<Shadow> st;
    synchronized (shadow) {
      st = shadow.get(t);
    }
    if (st == null) {
      return null;
    }
    synchronized (st) {
      int ptr = st.size() - 1;
      for (int j = 0; j <= frameIndex && j < real.length; j++) {
        Shadow found = null;
        int k = ptr;
        while (k >= 0) {
          Shadow cand = st.get(k);
          if (cand.c.getName().equals(real[j].getClassName()) && cand.name.equals(real[j].getMethodName())) {
            found = cand;
            break;
          }
          k--;
        }
        if (j == frameIndex) {
          return found == null ? null : found.snap;
        }
        if (found != null) {
          ptr = k - 1;
        }
      }
    }
    return null;
  }

  private static final class Stop {
    Class<?> c;
    String name;
    String desc;
    int ip;
    int line;
    int depth;
    StackTraceElement[] trace;
    Locals.Snap snap;
  }

  public static void start(String options) {
    final Agent a = new Agent();
    instance = a;
    a.startThread = Thread.currentThread();
    setHooks(true);
    try {
      a.run(options);
    } catch (IOException e) {
      System.err.println("JDWP agent failed: " + e);
    }
  }

  private static Map<String, String> parse(String o) {
    Map<String, String> m = new HashMap<String, String>();
    if (o == null) {
      return m;
    }
    for (String kv : o.split(",")) {
      int i = kv.indexOf('=');
      if (i > 0) {
        m.put(kv.substring(0, i), kv.substring(i + 1));
      } else if (!kv.isEmpty()) {
        m.put(kv, "");
      }
    }
    return m;
  }

  private void run(String options) throws IOException {
    Map<String, String> o = parse(options);
    String transport = o.containsKey("transport") ? o.get("transport") : "dt_socket";
    if (!transport.equals("dt_socket")) {
      System.err.println("ERROR: transport library not found: " + transport);
      System.exit(1);
    }
    boolean server = "y".equals(o.get("server"));
    suspendOnStart = !"n".equals(o.get("suspend"));
    String address = o.containsKey("address") ? o.get("address") : "";
    String host = null;
    int port = 0;
    if (!address.isEmpty()) {
      int c = address.lastIndexOf(':');
      if (c >= 0) {
        host = address.substring(0, c);
        port = Integer.parseInt(address.substring(c + 1));
      } else {
        port = Integer.parseInt(address);
      }
    }
    final Socket sock;
    if (server) {
      ServerSocket ss = host == null || host.equals("*") ? new ServerSocket(port)
          : new ServerSocket(port, 50, java.net.InetAddress.getByName(host));
      System.err.println("Listening for transport dt_socket at address: " + ss.getLocalPort());
      if (suspendOnStart) {
        sock = ss.accept();
        attach(sock);
      } else {
        final ServerSocket fss = ss;
        Thread t = new Thread(new Runnable() {
          public void run() {
            try {
              attach(fss.accept());
            } catch (IOException e) {
              // no debugger
            }
          }
        }, "JDWP Transport Listener: dt_socket");
        t.setDaemon(true);
        t.start();
        return;
      }
    } else {
      sock = new Socket();
      sock.connect(new InetSocketAddress(host == null ? "localhost" : host, port));
      attach(sock);
    }
    if (suspendOnStart) {
      synchronized (lock) {
        while (!resumed) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            return;
          }
        }
      }
    }
  }

  private void attach(Socket s) throws IOException {
    in = new DataInputStream(s.getInputStream());
    out = new DataOutputStream(s.getOutputStream());
    byte[] hs = new byte[14];
    in.readFully(hs);
    if (!new String(hs, StandardCharsets.US_ASCII).equals("JDWP-Handshake")) {
      s.close();
      throw new IOException("bad handshake");
    }
    out.write("JDWP-Handshake".getBytes(StandardCharsets.US_ASCII));
    out.flush();
    attached = true;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        vmDeath();
      }
    });
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          loop();
        } catch (IOException e) {
          // debugger went away
        }
        attached = false;
        synchronized (Agent.this) {
          requests.clear();
          vmSuspend = 0;
          suspend.clear();
          rearm();
        }
        synchronized (lock) {
          resumed = true;
          lock.notifyAll();
        }
      }
    }, "JDWP Command Reader");
    t.setDaemon(true);
    t.start();
    if (suspendOnStart) {
      changeVm(1);
    }
    post(new Buf().u8(suspendOnStart ? 2 : 0).i32w(1).u8(90).i32w(0).i64(idOf(startThread)));
  }

  private void loop() throws IOException {
    while (true) {
      int len = in.readInt();
      final int id = in.readInt();
      in.readUnsignedByte();
      final int set = in.readUnsignedByte();
      final int cmd = in.readUnsignedByte();
      byte[] data = new byte[len - 11];
      in.readFully(data);
      final Buf req = new Buf(data);
      if (set == 1 && cmd == 10) {
        reply(id, 0, new byte[0]);
        Runtime.getRuntime().halt(req.ri32());
      }
      if (invokes(set, cmd)) {
        Thread worker = new Thread(new Runnable() {
          public void run() {
            try {
              dispatch(id, set, cmd, req);
            } catch (IOException e) {
              // debugger went away
            }
          }
        }, "JDWP Invoke");
        worker.setDaemon(true);
        worker.start();
      } else {
        dispatch(id, set, cmd, req);
      }
    }
  }

  private static boolean invokes(int set, int cmd) {
    return (set == 3 && (cmd == 3 || cmd == 4)) || (set == 9 && cmd == 6);
  }

  private void dispatch(int id, int set, int cmd, Buf req) throws IOException {
    Buf rep = new Buf();
    int err;
    if (System.getProperty("shim.jdwp.debug") != null) {
      System.err.println("jdwp cmd " + set + "/" + cmd + " ...");
    }
    try {
      err = handle(set, cmd, req, rep);
    } catch (Throwable e) {
      err = E_ILLEGAL_ARGUMENT;
      if (System.getProperty("shim.jdwp.debug") != null) {
        e.printStackTrace();
      }
    }
    if (System.getProperty("shim.jdwp.debug") != null) {
      System.err.println("jdwp cmd " + set + "/" + cmd + " -> " + err);
    }
    reply(id, err, err == 0 ? rep.bytes() : new byte[0]);
  }

  private void reply(int id, int err, byte[] body) throws IOException {
    synchronized (out) {
      out.writeInt(11 + body.length);
      out.writeInt(id);
      out.writeByte(0x80);
      out.writeShort(err);
      out.write(body);
      out.flush();
    }
  }

  private void post(Buf body) {
    if (TRACE) {
      System.err.println("jdwp post event " + body.bytes().length);
    }
    if (!attached) {
      return;
    }
    try {
      byte[] b = body.bytes();
      synchronized (out) {
        out.writeInt(11 + b.length);
        out.writeInt(nextPacket++);
        out.writeByte(0);
        out.writeByte(64);
        out.writeByte(100);
        out.write(b);
        out.flush();
      }
    } catch (IOException e) {
      attached = false;
    }
  }

  /* ---------- buffers ---------- */

  private static final class Buf {
    private final ByteArrayOutputStream o = new ByteArrayOutputStream();
    private byte[] d;
    private int p;

    Buf() {
    }

    Buf(byte[] d) {
      this.d = d;
    }

    byte[] bytes() {
      return o.toByteArray();
    }

    Buf u8(int v) {
      o.write(v);
      return this;
    }

    Buf bool(boolean v) {
      return u8(v ? 1 : 0);
    }

    Buf i16(int v) {
      o.write(v >>> 8);
      o.write(v);
      return this;
    }

    Buf i32w(int v) {
      o.write(v >>> 24);
      o.write(v >>> 16);
      o.write(v >>> 8);
      o.write(v);
      return this;
    }

    Buf i64(long v) {
      i32w((int) (v >>> 32));
      return i32w((int) v);
    }

    Buf str(String s) {
      byte[] b = s.getBytes(StandardCharsets.UTF_8);
      i32w(b.length);
      o.write(b, 0, b.length);
      return this;
    }

    Buf raw(byte[] b) {
      o.write(b, 0, b.length);
      return this;
    }

    int ru8() {
      return d[p++] & 0xff;
    }

    int ri16() {
      int v = ((d[p] & 0xff) << 8) | (d[p + 1] & 0xff);
      p += 2;
      return v;
    }

    int ri32() {
      int v = ((d[p] & 0xff) << 24) | ((d[p + 1] & 0xff) << 16) | ((d[p + 2] & 0xff) << 8) | (d[p + 3] & 0xff);
      p += 4;
      return v;
    }

    long ri64() {
      long hi = ri32() & 0xffffffffL;
      long lo = ri32() & 0xffffffffL;
      return (hi << 32) | lo;
    }

    String rstr() {
      int n = ri32();
      String s = new String(d, p, n, StandardCharsets.UTF_8);
      p += n;
      return s;
    }
  }

  /* ---------- id registries ---------- */

  private synchronized long idOf(Object o) {
    if (o == null) {
      return 0;
    }
    Long l = ids.get(o);
    if (l == null) {
      l = Long.valueOf(next++);
      ids.put(o, l);
      objects.put(l, o);
    }
    return l.longValue();
  }

  private synchronized Object obj(long id) {
    return objects.get(Long.valueOf(id));
  }

  private synchronized long methodId(Executable m) {
    Long l = methodIds.get(m);
    if (l == null) {
      l = Long.valueOf(next++);
      methodIds.put(m, l);
      methods.put(l, m);
    }
    return l.longValue();
  }

  private synchronized long fieldId(Field f) {
    Long l = fieldIds.get(f);
    if (l == null) {
      l = Long.valueOf(next++);
      fieldIds.put(f, l);
      fields.put(l, f);
    }
    return l.longValue();
  }

  private synchronized Executable method(long id) {
    return methods.get(Long.valueOf(id));
  }

  private synchronized Field field(long id) {
    return fields.get(Long.valueOf(id));
  }

  static String sig(Class<?> c) {
    if (c.isPrimitive()) {
      return String.valueOf(primTag(c));
    }
    if (c.isArray()) {
      String n = c.getName();
      int d = 0;
      while (d < n.length() && n.charAt(d) == '[') {
        d++;
      }
      String e = n.substring(d);
      if (e.length() == 1 || (e.startsWith("L") && e.endsWith(";"))) {
        return n.replace('.', '/');
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < d; i++) {
        sb.append('[');
      }
      return sb.append('L').append(e.replace(";", "").replace('.', '/')).append(';').toString();
    }
    return "L" + c.getName().replace('.', '/') + ";";
  }

  private static char primTag(Class<?> c) {
    if (c == int.class) {
      return 'I';
    }
    if (c == long.class) {
      return 'J';
    }
    if (c == boolean.class) {
      return 'Z';
    }
    if (c == byte.class) {
      return 'B';
    }
    if (c == char.class) {
      return 'C';
    }
    if (c == short.class) {
      return 'S';
    }
    if (c == float.class) {
      return 'F';
    }
    if (c == double.class) {
      return 'D';
    }
    return 'V';
  }

  private static int typeTag(Class<?> c) {
    return c.isArray() ? 3 : c.isInterface() ? 2 : 1;
  }

  private static String descriptor(Executable e) {
    StringBuilder sb = new StringBuilder("(");
    for (Class<?> p : e.getParameterTypes()) {
      sb.append(sig(p));
    }
    Class<?> r = e instanceof Method ? ((Method) e).getReturnType() : void.class;
    return sb.append(')').append(sig(r)).toString();
  }

  private static String nameOf(Executable e) {
    return e instanceof Method ? e.getName() : "<init>";
  }

  private Thread[] threads() {
    Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
    return m.keySet().toArray(new Thread[0]);
  }

  private List<Class<?>> loaded() {
    LinkedHashSet<Class<?>> set = new LinkedHashSet<Class<?>>();
    Class<?>[] seeds = {Object.class, String.class, Class.class, Thread.class, System.class, Throwable.class,
        Runnable.class, Agent.class, Integer.class, Exception.class};
    for (Class<?> c : seeds) {
      set.add(c);
    }
    try {
      for (Object c : allClasses(null)) {
        set.add((Class<?>) c);
      }
      for (Object c : allClasses(ClassLoader.getSystemClassLoader())) {
        set.add((Class<?>) c);
      }
    } catch (Throwable t) {
      // fall back to the classes seen so far
    }
    synchronized (this) {
      set.addAll(seen);
      for (Object o : new ArrayList<Object>(objects.values())) {
        if (o instanceof Class) {
          set.add((Class<?>) o);
        }
      }
    }
    List<Class<?>> out = new ArrayList<Class<?>>();
    java.util.HashSet<String> sigs = new java.util.HashSet<String>();
    for (Class<?> c : set) {
      if (c.isArray() != c.getName().startsWith("[") || !sigs.add(sig(c))) {
        continue;
      }
      out.add(c);
    }
    return out;
  }

  private Class<?> bySignature(String s) {
    try {
      if (s.startsWith("L") && s.endsWith(";")) {
        return Class.forName(s.substring(1, s.length() - 1).replace('/', '.'), false,
            ClassLoader.getSystemClassLoader());
      }
      if (s.startsWith("[")) {
        return Class.forName(s.replace('/', '.'), false, ClassLoader.getSystemClassLoader());
      }
    } catch (Throwable t) {
      return null;
    }
    return null;
  }

  /* ---------- suspension and events ---------- */

  private int count(Thread t) {
    Integer c = suspend.get(t);
    return c == null ? 0 : c.intValue();
  }

  private synchronized void rearm() {
    int mask = 0;
    boolean events = false;
    boolean suspended = vmSuspend > 0 || !suspend.isEmpty();
    for (Req r : requests.values()) {
      switch (r.kind) {
        case K_STEP:
        case K_BREAKPOINT:
        case K_METHOD_ENTRY:
        case K_METHOD_EXIT:
        case 42:
          mask |= 1;
          break;
        case K_CLASS_PREPARE:
          mask |= 2;
          break;
        case K_EXCEPTION:
        case K_THREAD_START:
        case K_THREAD_DEATH:
          events = true;
          break;
        default:
          break;
      }
    }
    if (suspended) {
      mask |= 1;
    }
    eventsActive = events;
    setArmed(mask);
  }

  private void awaitResume(Thread t) {
    synchronized (lock) {
      boolean interrupted = false;
      while (attached && (vmSuspend > 0 || count(t) > 0)) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          interrupted = true;
        }
      }
      if (interrupted) {
        t.interrupt();
      }
    }
  }

  private void changeCount(Thread t, int delta) {
    synchronized (lock) {
      int n = Math.max(count(t) + delta, 0);
      if (n == 0) {
        suspend.remove(t);
      } else {
        suspend.put(t, Integer.valueOf(n));
      }
      lock.notifyAll();
    }
    rearm();
  }

  private void changeVm(int delta) {
    synchronized (lock) {
      vmSuspend = Math.max(vmSuspend + delta, 0);
      lock.notifyAll();
    }
    rearm();
  }

  private boolean enter(Thread t) {
    if (t.getName().startsWith("JDWP ")) {
      return false;
    }
    synchronized (inside) {
      if (inside.containsKey(t)) {
        return false;
      }
      inside.put(t, Boolean.TRUE);
      return true;
    }
  }

  private void leave(Thread t) {
    synchronized (inside) {
      inside.remove(t);
    }
  }

  private static int depthOf(Thread t) {
    return userFrames(t.getStackTrace()).length;
  }

  private static boolean globMatch(String pattern, String name) {
    if (pattern.startsWith("*")) {
      return name.endsWith(pattern.substring(1));
    }
    if (pattern.endsWith("*")) {
      return name.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return name.equals(pattern);
  }

  private boolean passes(Req r, Thread t, Class<?> c) {
    if (r.thread != null && r.thread != t) {
      return false;
    }
    if (c != null) {
      if (r.only != null && !r.only.isAssignableFrom(c)) {
        return false;
      }
      String n = c.getName();
      if (!r.match.isEmpty()) {
        boolean any = false;
        for (String m : r.match) {
          any |= globMatch(m, n);
        }
        if (!any) {
          return false;
        }
      }
      for (String m : r.exclude) {
        if (globMatch(m, n)) {
          return false;
        }
      }
    }
    return true;
  }

  public static void hit(Class<?> c, String name, String desc, int ip, int line) {
    Agent a = instance;
    if (a != null && a.attached) {
      a.onHit(c, name, desc, ip, line);
    }
  }

  private static final boolean TRACE = System.getProperty("shim.jdwp.debug") != null;

  public static void classPrepared(Class<?> c) {
    if (TRACE) {
      System.err.println("jdwp classPrepared " + c.getName());
    }
    Agent a = instance;
    if (a != null && a.attached) {
      a.onClassPrepare(c);
    }
  }

  public static void threadStarted(Thread t) {
    Agent a = instance;
    if (a != null && a.attached) {
      a.onThread(K_THREAD_START, t);
    }
  }

  public static void threadDied(Thread t) {
    Agent a = instance;
    if (a != null && a.attached) {
      a.onThread(K_THREAD_DEATH, t);
    }
  }

  public static void thrown(Throwable e) {
    Agent a = instance;
    if (a != null && a.attached) {
      a.onThrown(e);
    }
  }

  private void onHit(Class<?> c, String name, String desc, int rawIp, int line) {
    Thread t = Thread.currentThread();
    if (!enter(t)) {
      return;
    }
    try {
      boolean exit = rawIp < 0;
      int ip = exit ? -rawIp - 1 : rawIp;
      Locals.Snap snap = exit ? null : Locals.capture();
      track(t, c, name, desc, ip, exit, snap);
      awaitResume(t);
      List<Req> hits = new ArrayList<Req>();
      int depth = -1;
      synchronized (this) {
        boolean stepping = false;
        for (Req r : requests.values()) {
          if (r.kind == K_STEP && r.thread == t && !r.done) {
            stepping = true;
          }
        }
        if (stepping) {
          depth = depthOf(t);
        }
        for (Req r : new ArrayList<Req>(requests.values())) {
          switch (r.kind) {
            case K_BREAKPOINT:
              if (!exit && r.locClass == c && r.locIndex == ip && r.locName.equals(name) && r.locDesc.equals(desc)
                  && passes(r, t, c)) {
                hits.add(r);
              }
              break;
            case K_METHOD_ENTRY:
              if (!exit && ip == 0 && passes(r, t, c)) {
                hits.add(r);
              }
              break;
            case K_METHOD_EXIT:
            case 42:
              if (exit && passes(r, t, c)) {
                hits.add(r);
              }
              break;
            case K_STEP:
              if (!exit && !r.done && r.thread == t && stepDue(r, c, name, line, depth)) {
                r.done = true;
                hits.add(r);
              }
              break;
            default:
              break;
          }
        }
        hits = limit(hits);
      }
      if (TRACE) {
        System.err.println("jdwp hit " + c.getName() + "." + name + " ip=" + ip + " line=" + line + " exit=" + exit
            + " depth=" + depth + " hits=" + hits.size());
      }
      if (!hits.isEmpty()) {
        Stop s = new Stop();
        s.c = c;
        s.name = name;
        s.desc = desc;
        s.ip = ip;
        s.line = line;
        s.snap = snap;
        s.trace = userFrames(t.getStackTrace());
        s.depth = s.trace.length;
        reportAndWait(hits, t, s);
      }
    } finally {
      leave(t);
    }
  }

  private List<Req> limit(List<Req> hits) {
    List<Req> out = new ArrayList<Req>();
    for (Req r : hits) {
      if (r.kind == K_STEP || --r.count <= 0) {
        out.add(r);
        r.count = 1;
      }
    }
    return out;
  }

  private boolean stepDue(Req r, Class<?> c, String name, int line, int depth) {
    boolean sameLine = c == r.startClass && name.equals(r.startName) && line == r.startLine && depth == r.startDepth;
    switch (r.stepDepth) {
      case 0:
        return !sameLine;
      case 1:
        return depth < r.startDepth || (depth == r.startDepth && !sameLine);
      default:
        return depth < r.startDepth;
    }
  }

  private void reportAndWait(List<Req> hits, Thread t, Stop s) {
    int policy = 0;
    for (Req r : hits) {
      policy = Math.max(policy, r.policy);
    }
    synchronized (this) {
      stops.put(t, s);
    }
    if (policy == 1) {
      changeCount(t, 1);
    } else if (policy == 2) {
      changeVm(1);
    }
    Buf b = new Buf().u8(policy).i32w(hits.size());
    for (Req r : hits) {
      b.u8(r.kind).i32w(r.id).i64(idOf(t));
      location(b, s.c, s.name, s.desc, s.ip);
      if (r.kind == 42) {
        b.u8('V');
      }
    }
    post(b);
    awaitResume(t);
    if (s.snap != null) {
      Locals.apply(s.snap);
    }
    synchronized (this) {
      stops.remove(t);
    }
  }

  private void onClassPrepare(Class<?> c) {
    Thread t = Thread.currentThread();
    if (!enter(t)) {
      return;
    }
    try {
      List<Req> hits = new ArrayList<Req>();
      synchronized (this) {
        if (!seen.add(c)) {
          return;
        }
        for (Req r : requests.values()) {
          if (r.kind == K_CLASS_PREPARE && passes(r, t, c)) {
            hits.add(r);
          }
        }
        hits = limit(hits);
      }
      if (hits.isEmpty()) {
        return;
      }
      int policy = 0;
      for (Req r : hits) {
        policy = Math.max(policy, r.policy);
      }
      if (policy == 1) {
        changeCount(t, 1);
      } else if (policy == 2) {
        changeVm(1);
      }
      Buf b = new Buf().u8(policy).i32w(hits.size());
      for (Req r : hits) {
        b.u8(K_CLASS_PREPARE).i32w(r.id).i64(idOf(t)).u8(typeTag(c)).i64(idOf(c)).str(sig(c)).i32w(7);
      }
      post(b);
      awaitResume(t);
    } finally {
      leave(t);
    }
  }

  private void onThread(int kind, Thread th) {
    Thread t = Thread.currentThread();
    if (!enter(t)) {
      return;
    }
    try {
      List<Req> hits = new ArrayList<Req>();
      synchronized (this) {
        for (Req r : requests.values()) {
          if (r.kind == kind && (r.thread == null || r.thread == th)) {
            hits.add(r);
          }
        }
        hits = limit(hits);
      }
      if (hits.isEmpty()) {
        return;
      }
      int policy = 0;
      for (Req r : hits) {
        policy = Math.max(policy, r.policy);
      }
      if (policy == 1) {
        changeCount(t, 1);
      } else if (policy == 2) {
        changeVm(1);
      }
      Buf b = new Buf().u8(policy).i32w(hits.size());
      for (Req r : hits) {
        b.u8(kind).i32w(r.id).i64(idOf(th));
      }
      post(b);
      awaitResume(t);
    } finally {
      leave(t);
    }
  }

  private void onThrown(Throwable e) {
    Thread t = Thread.currentThread();
    if (!enter(t)) {
      return;
    }
    try {
      List<Req> hits = new ArrayList<Req>();
      StackTraceElement[] st = e.getStackTrace();
      int i = 0;
      while (i < st.length && (st[i].getClassName().startsWith("shim.jdwp.") || isThrowableInit(st[i], e))) {
        i++;
      }
      synchronized (this) {
        for (Req r : requests.values()) {
          if (r.kind == K_EXCEPTION && passes(r, t, null)
              && (r.excClass == null || r.excClass.isInstance(e)) && r.caught) {
            hits.add(r);
          }
        }
        hits = limit(hits);
      }
      if (hits.isEmpty() || i >= st.length) {
        return;
      }
      int policy = 0;
      for (Req r : hits) {
        policy = Math.max(policy, r.policy);
      }
      if (policy == 1) {
        changeCount(t, 1);
      } else if (policy == 2) {
        changeVm(1);
      }
      Stop s = new Stop();
      s.c = classOf(st[i]);
      s.name = st[i].getMethodName();
      s.line = st[i].getLineNumber();
      s.desc = null;
      Buf b = new Buf().u8(policy).i32w(hits.size());
      for (Req r : hits) {
        b.u8(K_EXCEPTION).i32w(r.id).i64(idOf(t));
        locationByLine(b, st[i]);
        b.u8('L').i64(idOf(e));
        b.u8(1).i64(0).i64(0).i64(0);
      }
      synchronized (this) {
        stops.put(t, s);
      }
      post(b);
      awaitResume(t);
      synchronized (this) {
        stops.remove(t);
      }
    } finally {
      leave(t);
    }
  }

  private static boolean isThrowableInit(StackTraceElement el, Throwable e) {
    String n = el.getClassName();
    return n.equals("java.lang.Throwable") || el.getMethodName().equals("<init>") && isSuperOf(n, e);
  }

  private static boolean isSuperOf(String name, Throwable e) {
    for (Class<?> c = e.getClass(); c != null; c = c.getSuperclass()) {
      if (c.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  private void vmDeath() {
    if (!attached) {
      return;
    }
    List<Req> hits = new ArrayList<Req>();
    synchronized (this) {
      for (Req r : requests.values()) {
        if (r.kind == K_VM_DEATH) {
          hits.add(r);
        }
      }
    }
    if (hits.isEmpty()) {
      return;
    }
    Buf b = new Buf().u8(0).i32w(hits.size());
    for (Req r : hits) {
      b.u8(K_VM_DEATH).i32w(r.id);
    }
    post(b);
    long end = System.currentTimeMillis() + 3000;
    while (attached && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  /* ---------- locations ---------- */

  private final Map<Class<?>, Map<String, int[]>> lineCache = new HashMap<Class<?>, Map<String, int[]>>();

  private int[] lines(Class<?> c, String name, String desc) {
    Map<String, int[]> m;
    synchronized (lineCache) {
      m = lineCache.get(c);
    }
    if (m == null) {
      m = parseLines(c);
      synchronized (lineCache) {
        lineCache.put(c, m);
      }
    }
    int[] r = m.get(name + desc);
    return r != null ? r : lineTable(c, name, desc);
  }

  private static Map<String, int[]> parseLines(Class<?> c) {
    Map<String, int[]> out = new HashMap<String, int[]>();
    try {
      String res = c.getName().replace('.', '/') + ".class";
      ClassLoader l = c.getClassLoader();
      java.io.InputStream is = l == null ? ClassLoader.getSystemResourceAsStream(res) : l.getResourceAsStream(res);
      if (is == null) {
        return out;
      }
      byte[] bytes;
      try {
        bytes = is.readAllBytes();
      } finally {
        is.close();
      }
      java.lang.classfile.ClassModel cm = java.lang.classfile.ClassFile.of().parse(bytes);
      for (java.lang.classfile.MethodModel mm : cm.methods()) {
        java.util.Optional<java.lang.classfile.CodeModel> code = mm.code();
        if (!code.isPresent()) {
          continue;
        }
        List<Integer> flat = new ArrayList<Integer>();
        java.util.Optional<java.lang.classfile.attribute.LineNumberTableAttribute> lnt =
            code.get().findAttribute(java.lang.classfile.Attributes.lineNumberTable());
        if (lnt.isPresent()) {
          for (java.lang.classfile.attribute.LineNumberInfo li : lnt.get().lineNumbers()) {
            flat.add(Integer.valueOf(li.startPc()));
            flat.add(Integer.valueOf(li.lineNumber()));
          }
        }
        int[] a = new int[flat.size()];
        for (int i = 0; i < a.length; i++) {
          a[i] = flat.get(i).intValue();
        }
        out.put(mm.methodName().stringValue() + mm.methodType().stringValue(), a);
      }
    } catch (Throwable t) {
      return out;
    }
    return out;
  }

  private Class<?> classOf(StackTraceElement e) {
    try {
      return Class.forName(e.getClassName(), false, ClassLoader.getSystemClassLoader());
    } catch (Throwable t) {
      return Object.class;
    }
  }

  private Executable findMethod(Class<?> c, String name, String desc) {
    try {
      if (name.equals("<init>")) {
        for (Constructor<?> k : c.getDeclaredConstructors()) {
          if (desc == null || descriptor(k).equals(desc)) {
            return k;
          }
        }
      } else {
        for (Method m : c.getDeclaredMethods()) {
          if (m.getName().equals(name) && (desc == null || descriptor(m).equals(desc))) {
            return m;
          }
        }
      }
    } catch (Throwable t) {
      return null;
    }
    return null;
  }

  private Executable findByLine(Class<?> c, String name, int line) {
    Executable best = null;
    try {
      List<Executable> all = new ArrayList<Executable>();
      for (Constructor<?> k : c.getDeclaredConstructors()) {
        all.add(k);
      }
      for (Method m : c.getDeclaredMethods()) {
        all.add(m);
      }
      for (Executable e : all) {
        if (!nameOf(e).equals(name)) {
          continue;
        }
        int[] lt = lines(c, name, descriptor(e));
        int min = Integer.MAX_VALUE;
        int max = -1;
        for (int i = 1; i < lt.length; i += 2) {
          min = Math.min(min, lt[i]);
          max = Math.max(max, lt[i]);
        }
        if (line >= min && line <= max) {
          return e;
        }
        if (best == null) {
          best = e;
        }
      }
    } catch (Throwable t) {
      return best;
    }
    return best;
  }

  private long indexOfLine(Class<?> c, Executable e, int line) {
    if (e == null) {
      return 0;
    }
    int[] lt = lines(c, nameOf(e), descriptor(e));
    long best = 0;
    int bestLine = -1;
    for (int i = 0; i + 1 < lt.length; i += 2) {
      if (lt[i + 1] <= line && lt[i + 1] > bestLine) {
        bestLine = lt[i + 1];
        best = lt[i];
      }
    }
    return best;
  }

  private void location(Buf b, Class<?> c, String name, String desc, long ip) {
    Executable e = findMethod(c, name, desc);
    b.u8(typeTag(c)).i64(idOf(c)).i64(e == null ? 0 : methodId(e)).i64(ip);
  }

  private void locationByLine(Buf b, StackTraceElement el) {
    Class<?> c = classOf(el);
    Executable e = findByLine(c, el.getMethodName(), el.getLineNumber());
    if (TRACE) {
      System.err.println("jdwp frame " + el + " line " + el.getLineNumber() + " -> " + e + " idx "
          + indexOfLine(c, e, el.getLineNumber()));
    }
    b.u8(typeTag(c)).i64(idOf(c)).i64(e == null ? 0 : methodId(e)).i64(indexOfLine(c, e, el.getLineNumber()));
  }

  /* ---------- values ---------- */

  private int tagFor(Object o) {
    if (o == null) {
      return 'L';
    }
    if (o instanceof String) {
      return 's';
    }
    if (o instanceof Thread) {
      return 't';
    }
    if (o instanceof ThreadGroup) {
      return 'g';
    }
    if (o instanceof ClassLoader) {
      return 'l';
    }
    if (o instanceof Class) {
      return 'c';
    }
    if (o.getClass().isArray()) {
      return '[';
    }
    return 'L';
  }

  private void tagged(Buf b, Object v, Class<?> type) {
    if (type.isPrimitive()) {
      char tag = primTag(type);
      b.u8(tag);
      untagged(b, v, tag);
    } else {
      b.u8(tagFor(v)).i64(idOf(v));
    }
  }

  private void untagged(Buf b, Object v, int tag) {
    switch (tag) {
      case 'B':
        b.u8(((Number) v).intValue());
        break;
      case 'C':
        b.i16(((Character) v).charValue());
        break;
      case 'F':
        b.i32w(Float.floatToRawIntBits(((Number) v).floatValue()));
        break;
      case 'D':
        b.i64(Double.doubleToRawLongBits(((Number) v).doubleValue()));
        break;
      case 'I':
        b.i32w(((Number) v).intValue());
        break;
      case 'J':
        b.i64(((Number) v).longValue());
        break;
      case 'S':
        b.i16(((Number) v).intValue());
        break;
      case 'Z':
        b.bool(((Boolean) v).booleanValue());
        break;
      case 'V':
        break;
      default:
        b.i64(idOf(v));
        break;
    }
  }

  private Object readUntagged(Buf r, int tag) {
    switch (tag) {
      case 'B':
        return Byte.valueOf((byte) r.ru8());
      case 'C':
        return Character.valueOf((char) r.ri16());
      case 'F':
        return Float.valueOf(Float.intBitsToFloat(r.ri32()));
      case 'D':
        return Double.valueOf(Double.longBitsToDouble(r.ri64()));
      case 'I':
        return Integer.valueOf(r.ri32());
      case 'J':
        return Long.valueOf(r.ri64());
      case 'S':
        return Short.valueOf((short) r.ri16());
      case 'Z':
        return Boolean.valueOf(r.ru8() != 0);
      case 'V':
        return null;
      default:
        return obj(r.ri64());
    }
  }

  private Object readTagged(Buf r) {
    return readUntagged(r, r.ru8());
  }

  private static int tagOfType(Class<?> t) {
    return t.isPrimitive() ? primTag(t) : 'L';
  }

  private Object[] readArgs(Buf req) {
    int n = req.ri32();
    Object[] a = new Object[n];
    for (int i = 0; i < n; i++) {
      a[i] = readTagged(req);
    }
    return a;
  }

  private static Object coerce(Object v, Class<?> t) {
    if (v instanceof Number && t.isPrimitive()) {
      Number n = (Number) v;
      if (t == int.class) {
        return Integer.valueOf(n.intValue());
      }
      if (t == long.class) {
        return Long.valueOf(n.longValue());
      }
      if (t == short.class) {
        return Short.valueOf(n.shortValue());
      }
      if (t == byte.class) {
        return Byte.valueOf(n.byteValue());
      }
      if (t == float.class) {
        return Float.valueOf(n.floatValue());
      }
      if (t == double.class) {
        return Double.valueOf(n.doubleValue());
      }
    }
    return v;
  }

  private static Object[] coerceAll(Object[] a, Class<?>[] types) {
    for (int i = 0; i < a.length && i < types.length; i++) {
      a[i] = coerce(a[i], types[i]);
    }
    return a;
  }

  /* ---------- command handling ---------- */

  private int handle(int set, int cmd, Buf req, Buf rep) {
    switch (set) {
      case 1:
        return vm(cmd, req, rep);
      case 2:
        return refType(cmd, req, rep);
      case 3:
        return classType(cmd, req, rep);
      case 4:
        return arrayType(cmd, req, rep);
      case 6:
        return methodCmd(cmd, req, rep);
      case 9:
        return objRef(cmd, req, rep);
      case 10:
        return strRef(cmd, req, rep);
      case 11:
        return thread(cmd, req, rep);
      case 12:
        return threadGroup(cmd, req, rep);
      case 13:
        return arrayRef(cmd, req, rep);
      case 14:
        return loaderRef(cmd, req, rep);
      case 15:
        return eventRequest(cmd, req, rep);
      case 16:
        return frame(cmd, req, rep);
      case 17:
        return classObject(cmd, req, rep);
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int vm(int cmd, Buf req, Buf rep) {
    switch (cmd) {
      case 1:
        rep.str("Basalt Shim JDWP").i32w(1).i32w(8).str(System.getProperty("java.version", "25"))
            .str("Basalt Shim");
        return 0;
      case 2: {
        String s = req.rstr();
        Class<?> c = bySignature(s);
        if (c == null) {
          rep.i32w(0);
        } else {
          rep.i32w(1).u8(typeTag(c)).i64(idOf(c)).i32w(7);
        }
        return 0;
      }
      case 3:
      case 20: {
        List<Class<?>> cs = loaded();
        rep.i32w(cs.size());
        for (Class<?> c : cs) {
          rep.u8(typeTag(c)).i64(idOf(c)).str(sig(c));
          if (cmd == 20) {
            rep.str("");
          }
          rep.i32w(7);
        }
        return 0;
      }
      case 4: {
        Thread[] ts = threads();
        rep.i32w(ts.length);
        for (Thread t : ts) {
          rep.i64(idOf(t));
        }
        return 0;
      }
      case 5:
        rep.i32w(1).i64(idOf(rootGroup()));
        return 0;
      case 6:
        synchronized (this) {
          requests.clear();
        }
        changeVm(-vmSuspend);
        synchronized (lock) {
          suspend.clear();
          resumed = true;
          lock.notifyAll();
        }
        rearm();
        return 0;
      case 7:
        rep.i32w(8).i32w(8).i32w(8).i32w(8).i32w(8);
        return 0;
      case 8:
        changeVm(1);
        return 0;
      case 9:
        synchronized (lock) {
          resumed = true;
          lock.notifyAll();
        }
        changeVm(-1);
        return 0;
      case 11:
        rep.i64(idOf(req.rstr()));
        return 0;
      case 12:
        for (int i = 0; i < 7; i++) {
          rep.bool(false);
        }
        return 0;
      case 17:
        for (int i = 0; i < 32; i++) {
          rep.bool(false);
        }
        return 0;
      case 13: {
        rep.str(System.getProperty("user.dir", "."));
        String cp = System.getProperty("java.class.path", ".");
        String[] parts = cp.split(java.io.File.pathSeparator);
        rep.i32w(parts.length);
        for (String x : parts) {
          rep.str(x);
        }
        rep.i32w(0);
        return 0;
      }
      case 14:
      case 15:
      case 16:
        return 0;
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private static ThreadGroup rootGroup() {
    ThreadGroup g = Thread.currentThread().getThreadGroup();
    while (g.getParent() != null) {
      g = g.getParent();
    }
    return g;
  }

  private int refType(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (!(o instanceof Class)) {
      return E_INVALID_CLASS;
    }
    Class<?> c = (Class<?>) o;
    switch (cmd) {
      case 1:
        rep.str(sig(c));
        return 0;
      case 13:
        rep.str(sig(c)).str("");
        return 0;
      case 2:
        rep.i64(idOf(c.getClassLoader()));
        return 0;
      case 3:
        rep.i32w(c.getModifiers());
        return 0;
      case 4:
      case 14: {
        Field[] fs = c.getDeclaredFields();
        rep.i32w(fs.length);
        for (Field f : fs) {
          rep.i64(fieldId(f)).str(f.getName()).str(sig(f.getType()));
          if (cmd == 14) {
            rep.str("");
          }
          rep.i32w(f.getModifiers() | (f.isSynthetic() ? 0x1000 : 0));
        }
        return 0;
      }
      case 5:
      case 15: {
        List<Executable> ms = new ArrayList<Executable>();
        for (Constructor<?> k : c.getDeclaredConstructors()) {
          ms.add(k);
        }
        for (Method m : c.getDeclaredMethods()) {
          ms.add(m);
        }
        rep.i32w(ms.size());
        for (Executable m : ms) {
          rep.i64(methodId(m)).str(nameOf(m)).str(descriptor(m));
          if (cmd == 15) {
            rep.str("");
          }
          rep.i32w(m.getModifiers() | (m.isSynthetic() ? 0x1000 : 0));
        }
        return 0;
      }
      case 6: {
        int n = req.ri32();
        rep.i32w(n);
        for (int i = 0; i < n; i++) {
          Field f = field(req.ri64());
          if (f == null) {
            return E_INVALID_FIELDID;
          }
          try {
            f.setAccessible(true);
            tagged(rep, f.get(null), f.getType());
          } catch (Exception e) {
            return E_ILLEGAL_ARGUMENT;
          }
        }
        return 0;
      }
      case 7: {
        String n = c.getName();
        int dot = n.lastIndexOf('.');
        n = n.substring(dot + 1);
        int dollar = n.indexOf('$');
        rep.str((dollar > 0 ? n.substring(0, dollar) : n) + ".java");
        return 0;
      }
      case 8: {
        Class<?>[] ns = c.getDeclaredClasses();
        rep.i32w(ns.length);
        for (Class<?> n : ns) {
          rep.u8(typeTag(n)).i64(idOf(n));
        }
        return 0;
      }
      case 9:
        rep.i32w(7);
        return 0;
      case 10: {
        Class<?>[] is = c.getInterfaces();
        rep.i32w(is.length);
        for (Class<?> i : is) {
          rep.i64(idOf(i));
        }
        return 0;
      }
      case 11:
        rep.i64(idOf(c));
        return 0;
      case 17:
        rep.i32w(69).i32w(0);
        return 0;
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int classType(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (!(o instanceof Class)) {
      return E_INVALID_CLASS;
    }
    Class<?> c = (Class<?>) o;
    switch (cmd) {
      case 1:
        rep.i64(idOf(c.getSuperclass()));
        return 0;
      case 2: {
        int n = req.ri32();
        for (int i = 0; i < n; i++) {
          Field f = field(req.ri64());
          if (f == null) {
            return E_INVALID_FIELDID;
          }
          try {
            f.setAccessible(true);
            f.set(null, coerce(readUntagged(req, tagOfType(f.getType())), f.getType()));
          } catch (Exception e) {
            return E_ILLEGAL_ARGUMENT;
          }
        }
        return 0;
      }
      case 3: {
        req.ri64();
        Executable m = method(req.ri64());
        if (!(m instanceof Method)) {
          return E_INVALID_METHODID;
        }
        Object[] args = coerceAll(readArgs(req), m.getParameterTypes());
        return invoke((Method) m, null, args, rep);
      }
      case 4: {
        req.ri64();
        Executable m = method(req.ri64());
        if (!(m instanceof Constructor)) {
          return E_INVALID_METHODID;
        }
        Object[] args = coerceAll(readArgs(req), m.getParameterTypes());
        try {
          ((Constructor<?>) m).setAccessible(true);
          Object r = ((Constructor<?>) m).newInstance(args);
          rep.u8(tagFor(r)).i64(idOf(r)).u8('L').i64(0);
        } catch (InvocationTargetException e) {
          rep.u8('L').i64(0).u8('L').i64(idOf(e.getCause()));
        } catch (Exception e) {
          return E_ILLEGAL_ARGUMENT;
        }
        return 0;
      }
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int invoke(Method m, Object target, Object[] args, Buf rep) {
    try {
      m.setAccessible(true);
      Object r = m.invoke(target, args);
      Class<?> rt = m.getReturnType();
      if (rt == void.class) {
        rep.u8('V');
      } else {
        tagged(rep, r, rt);
      }
      rep.u8('L').i64(0);
    } catch (InvocationTargetException e) {
      rep.u8('L').i64(0).u8('L').i64(idOf(e.getCause()));
    } catch (Exception e) {
      return E_ILLEGAL_ARGUMENT;
    }
    return 0;
  }

  private int arrayType(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (cmd != 1 || !(o instanceof Class) || !((Class<?>) o).isArray()) {
      return E_INVALID_CLASS;
    }
    Object a = Array.newInstance(((Class<?>) o).getComponentType(), req.ri32());
    rep.u8('[').i64(idOf(a));
    return 0;
  }

  private int methodCmd(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    Executable m = method(req.ri64());
    if (!(o instanceof Class)) {
      return E_INVALID_CLASS;
    }
    if (m == null) {
      return E_INVALID_METHODID;
    }
    Class<?> c = (Class<?>) o;
    switch (cmd) {
      case 1: {
        int[] lt = lineTable(c, nameOf(m), descriptor(m));
        int pairs = lt.length / 2;
        if (pairs == 0) {
          rep.i64(0).i64(0).i32w(0);
          return 0;
        }
        long last = 0;
        for (int i = 0; i < pairs; i++) {
          last = Math.max(last, lt[i * 2]);
        }
        rep.i64(0).i64(last + 1).i32w(pairs);
        for (int i = 0; i < pairs; i++) {
          rep.i64(lt[i * 2]).i32w(lt[i * 2 + 1]);
        }
        return 0;
      }
      case 2:
      case 5: {
        Locals.Info info = Locals.info(c, nameOf(m), descriptor(m));
        if (info == null || info.vars.isEmpty()) {
          return E_ABSENT_INFORMATION;
        }
        rep.i32w(info.argSlots).i32w(info.vars.size());
        for (Locals.Var v : info.vars) {
          rep.i64(v.start).str(v.name).str(v.sig);
          if (cmd == 5) {
            rep.str(v.generic);
          }
          rep.i32w(v.length).i32w(v.slot);
        }
        return 0;
      }
      case 4:
        rep.bool(false);
        return 0;
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int objRef(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (o == null) {
      return E_INVALID_OBJECT;
    }
    switch (cmd) {
      case 1:
        rep.u8(typeTag(o.getClass())).i64(idOf(o.getClass()));
        return 0;
      case 2: {
        int n = req.ri32();
        rep.i32w(n);
        for (int i = 0; i < n; i++) {
          Field f = field(req.ri64());
          if (f == null) {
            return E_INVALID_FIELDID;
          }
          try {
            f.setAccessible(true);
            tagged(rep, f.get(o), f.getType());
          } catch (Exception e) {
            return E_ILLEGAL_ARGUMENT;
          }
        }
        return 0;
      }
      case 3: {
        int n = req.ri32();
        for (int i = 0; i < n; i++) {
          Field f = field(req.ri64());
          if (f == null) {
            return E_INVALID_FIELDID;
          }
          try {
            f.setAccessible(true);
            f.set(o, coerce(readUntagged(req, tagOfType(f.getType())), f.getType()));
          } catch (Exception e) {
            return E_ILLEGAL_ARGUMENT;
          }
        }
        return 0;
      }
      case 5: {
        Thread owner = Locals.monitorOwner(o);
        rep.i64(owner == null ? 0 : idOf(owner)).i32w(Locals.monitorDepth(o)).i32w(0);
        return 0;
      }
      case 6: {
        req.ri64();
        req.ri64();
        Executable m = method(req.ri64());
        if (!(m instanceof Method)) {
          return E_INVALID_METHODID;
        }
        Object[] args = coerceAll(readArgs(req), m.getParameterTypes());
        return invoke((Method) m, o, args, rep);
      }
      case 7:
      case 8:
        return 0;
      case 9:
        rep.bool(false);
        return 0;
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int strRef(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (!(o instanceof String)) {
      return E_INVALID_OBJECT;
    }
    if (cmd == 1) {
      rep.str((String) o);
      return 0;
    }
    return E_NOT_IMPLEMENTED;
  }

  private int arrayRef(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (o == null || !o.getClass().isArray()) {
      return E_INVALID_OBJECT;
    }
    switch (cmd) {
      case 1:
        rep.i32w(Array.getLength(o));
        return 0;
      case 2: {
        int first = req.ri32();
        int n = req.ri32();
        Class<?> ct = o.getClass().getComponentType();
        rep.u8(ct.isPrimitive() ? primTag(ct) : ct.isArray() ? '[' : ct == String.class ? 's' : 'L');
        rep.i32w(n);
        for (int i = first; i < first + n; i++) {
          Object v = Array.get(o, i);
          if (ct.isPrimitive()) {
            untagged(rep, v, primTag(ct));
          } else {
            rep.u8(tagFor(v)).i64(idOf(v));
          }
        }
        return 0;
      }
      case 3: {
        int first = req.ri32();
        int n = req.ri32();
        Class<?> ct = o.getClass().getComponentType();
        for (int i = first; i < first + n; i++) {
          Object v = ct.isPrimitive() ? readUntagged(req, primTag(ct)) : readTagged(req);
          Array.set(o, i, coerce(v, ct));
        }
        return 0;
      }
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int loaderRef(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (cmd != 1 || !(o instanceof ClassLoader)) {
      return E_INVALID_OBJECT;
    }
    List<Class<?>> out = new ArrayList<Class<?>>();
    for (Class<?> c : loaded()) {
      if (c.getClassLoader() == o) {
        out.add(c);
      }
    }
    rep.i32w(out.size());
    for (Class<?> c : out) {
      rep.u8(typeTag(c)).i64(idOf(c));
    }
    return 0;
  }

  private int classObject(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (cmd != 1 || !(o instanceof Class)) {
      return E_INVALID_OBJECT;
    }
    rep.u8(typeTag((Class<?>) o)).i64(idOf(o));
    return 0;
  }

  private int status(Thread t) {
    switch (t.getState()) {
      case RUNNABLE:
        return 1;
      case TIMED_WAITING:
        return 2;
      case BLOCKED:
        return 3;
      case WAITING:
        return 4;
      case TERMINATED:
        return 0;
      default:
        return 1;
    }
  }

  private static StackTraceElement[] userFrames(StackTraceElement[] st) {
    int i = 0;
    while (i < st.length && (st[i].getClassName().startsWith("shim.jdwp.") || st[i].getClassName().equals("java.lang.Thread"))) {
      i++;
    }
    StackTraceElement[] r = new StackTraceElement[st.length - i];
    System.arraycopy(st, i, r, 0, r.length);
    return r;
  }

  private StackTraceElement[] framesOf(Thread t) {
    Stop stop;
    synchronized (this) {
      stop = stops.get(t);
    }
    return stop != null && stop.trace != null ? stop.trace : userFrames(t.getStackTrace());
  }

  private int thread(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (!(o instanceof Thread)) {
      return E_INVALID_THREAD;
    }
    Thread t = (Thread) o;
    switch (cmd) {
      case 1:
        rep.str(t.getName());
        return 0;
      case 2:
        changeCount(t, 1);
        return 0;
      case 3:
        changeCount(t, -1);
        return 0;
      case 4:
        rep.i32w(status(t)).i32w(vmSuspend > 0 || count(t) > 0 ? 1 : 0);
        return 0;
      case 5:
        rep.i64(idOf(t.getThreadGroup()));
        return 0;
      case 6: {
        int start = req.ri32();
        int length = req.ri32();
        StackTraceElement[] st = framesOf(t);
        Stop stop;
        synchronized (this) {
          stop = stops.get(t);
        }
        int n = length < 0 ? st.length - start : Math.min(length, st.length - start);
        rep.i32w(Math.max(n, 0));
        for (int i = start; i < start + n; i++) {
          long fid;
          synchronized (this) {
            fid = next++;
            frames.put(Long.valueOf(fid), new Object[] {t, Integer.valueOf(i)});
          }
          rep.i64(fid);
          if (i == 0 && stop != null && stop.desc != null) {
            location(rep, stop.c, stop.name, stop.desc, stop.ip);
          } else {
            locationByLine(rep, st[i]);
          }
        }
        return 0;
      }
      case 7:
        rep.i32w(framesOf(t).length);
        return 0;
      case 8:
        rep.i32w(0);
        return 0;
      case 9:
        rep.u8('L').i64(0);
        return 0;
      case 11:
        t.interrupt();
        return 0;
      case 12:
        rep.i32w(count(t));
        return 0;
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int threadGroup(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    if (!(o instanceof ThreadGroup)) {
      return E_INVALID_OBJECT;
    }
    ThreadGroup g = (ThreadGroup) o;
    switch (cmd) {
      case 1:
        rep.str(g.getName());
        return 0;
      case 2:
        rep.i64(idOf(g.getParent()));
        return 0;
      case 3: {
        Thread[] ts = new Thread[g.activeCount() + 8];
        int n = g.enumerate(ts, false);
        rep.i32w(n);
        for (int i = 0; i < n; i++) {
          rep.i64(idOf(ts[i]));
        }
        ThreadGroup[] gs = new ThreadGroup[g.activeGroupCount() + 8];
        int m = g.enumerate(gs, false);
        rep.i32w(m);
        for (int i = 0; i < m; i++) {
          rep.i64(idOf(gs[i]));
        }
        return 0;
      }
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private int eventRequest(int cmd, Buf req, Buf rep) {
    switch (cmd) {
      case 1: {
        Req r = new Req();
        r.kind = req.ru8();
        r.policy = req.ru8();
        int mods = req.ri32();
        for (int i = 0; i < mods; i++) {
          int k = req.ru8();
          switch (k) {
            case 1:
              r.count = req.ri32();
              break;
            case 2:
              req.ri32();
              break;
            case 3: {
              Object t = obj(req.ri64());
              if (!(t instanceof Thread)) {
                return E_INVALID_THREAD;
              }
              r.thread = (Thread) t;
              break;
            }
            case 4: {
              Object c = obj(req.ri64());
              if (!(c instanceof Class)) {
                return E_INVALID_CLASS;
              }
              r.only = (Class<?>) c;
              break;
            }
            case 5:
              r.match.add(req.rstr());
              break;
            case 6:
              r.exclude.add(req.rstr());
              break;
            case 7: {
              req.ru8();
              Object c = obj(req.ri64());
              Executable m = method(req.ri64());
              long idx = req.ri64();
              if (!(c instanceof Class)) {
                return E_INVALID_CLASS;
              }
              if (m == null) {
                return E_INVALID_METHODID;
              }
              r.locClass = (Class<?>) c;
              r.locName = nameOf(m);
              r.locDesc = descriptor(m);
              r.locIndex = idx;
              break;
            }
            case 8: {
              long id = req.ri64();
              Object c = id == 0 ? null : obj(id);
              r.excClass = c instanceof Class ? (Class<?>) c : null;
              r.caught = req.ru8() != 0;
              r.uncaught = req.ru8() != 0;
              break;
            }
            case 9:
              req.ri64();
              req.ri64();
              break;
            case 10: {
              Object t = obj(req.ri64());
              if (!(t instanceof Thread)) {
                return E_INVALID_THREAD;
              }
              r.thread = (Thread) t;
              r.stepSize = req.ri32();
              r.stepDepth = req.ri32();
              break;
            }
            case 11:
              req.ri64();
              break;
            case 12:
              req.rstr();
              break;
            default:
              break;
          }
        }
        if (r.kind == K_STEP) {
          Stop s;
          synchronized (this) {
            s = stops.get(r.thread);
          }
          if (s == null) {
            return E_ILLEGAL_ARGUMENT;
          }
          r.startDepth = s.depth;
          r.startLine = s.line;
          r.startClass = s.c;
          r.startName = s.name;
        }
        synchronized (this) {
          r.id = nextRequest++;
          requests.put(Integer.valueOf(r.id), r);
        }
        rep.i32w(r.id);
        rearm();
        return 0;
      }
      case 2: {
        req.ru8();
        int id = req.ri32();
        synchronized (this) {
          requests.remove(Integer.valueOf(id));
        }
        rearm();
        return 0;
      }
      case 3:
        synchronized (this) {
          for (Req r : new ArrayList<Req>(requests.values())) {
            if (r.kind == K_BREAKPOINT) {
              requests.remove(Integer.valueOf(r.id));
            }
          }
        }
        rearm();
        return 0;
      default:
        return E_NOT_IMPLEMENTED;
    }
  }

  private Locals.Snap snapOf(Thread t, int index) {
    Stop stop;
    synchronized (this) {
      stop = stops.get(t);
    }
    if (index == 0) {
      return stop != null ? stop.snap : null;
    }
    return stop != null ? shadowFor(t, index) : null;
  }

  private int frame(int cmd, Buf req, Buf rep) {
    Object o = obj(req.ri64());
    long fid = req.ri64();
    Object[] fr;
    synchronized (this) {
      fr = frames.get(Long.valueOf(fid));
    }
    if (!(o instanceof Thread)) {
      return E_INVALID_THREAD;
    }
    if (fr == null) {
      return 30;
    }
    Thread t = (Thread) o;
    Locals.Snap snap = snapOf(t, ((Integer) fr[1]).intValue());
    switch (cmd) {
      case 1: {
        int n = req.ri32();
        if (n > 0 && snap == null) {
          return E_ABSENT_INFORMATION;
        }
        rep.i32w(n);
        for (int i = 0; i < n; i++) {
          int slot = req.ri32();
          int tag = req.ru8();
          if (slot < 0) {
            return E_ILLEGAL_ARGUMENT;
          }
          Object pending = snap.pending.get(Integer.valueOf(slot));
          long bits = pending instanceof Locals.Raw ? ((Locals.Raw) pending).bits
              : slot < snap.bits.length ? snap.bits[slot] : 0;
          Object ref = snap.pending.containsKey(Integer.valueOf(slot)) && !(pending instanceof Locals.Raw) ? pending
              : slot < snap.refs.length ? snap.refs[slot] : null;
          if (ref == Locals.NULL) {
            ref = null;
          }
          switch (tag) {
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
              rep.u8(tag);
              untagged(rep, tag == 'Z' ? (Object) Boolean.valueOf((int) bits != 0)
                  : tag == 'C' ? (Object) Character.valueOf((char) bits) : (Object) Integer.valueOf((int) bits), tag);
              break;
            case 'J':
              rep.u8(tag).i64(bits);
              break;
            case 'F':
              rep.u8(tag).i32w((int) bits);
              break;
            case 'D':
              rep.u8(tag).i64(bits);
              break;
            default:
              rep.u8(ref == null ? tag : tagFor(ref)).i64(idOf(ref));
              break;
          }
        }
        return 0;
      }
      case 2: {
        int n = req.ri32();
        if (n > 0 && snap == null) {
          return E_ABSENT_INFORMATION;
        }
        for (int i = 0; i < n; i++) {
          int slot = req.ri32();
          int tag = req.ru8();
          Object v = readUntagged(req, tag);
          Integer key = Integer.valueOf(slot);
          switch (tag) {
            case 'Z':
              snap.pending.put(key, new Locals.Raw(((Boolean) v).booleanValue() ? 1 : 0));
              break;
            case 'C':
              snap.pending.put(key, new Locals.Raw(((Character) v).charValue()));
              break;
            case 'B':
            case 'S':
            case 'I':
            case 'J':
              snap.pending.put(key, new Locals.Raw(((Number) v).longValue()));
              break;
            case 'F':
              snap.pending.put(key, new Locals.Raw(Float.floatToRawIntBits(((Float) v).floatValue()) & 0xffffffffL));
              break;
            case 'D':
              snap.pending.put(key, new Locals.Raw(Double.doubleToRawLongBits(((Double) v).doubleValue())));
              break;
            default:
              snap.pending.put(key, v == null ? Locals.NULL : v);
              break;
          }
        }
        return 0;
      }
      case 3: {
        Object self = snap == null || snap.refs.length == 0 ? null : snap.refs[0];
        Stop stop;
        synchronized (this) {
          stop = stops.get(t);
        }
        if (snap != null && stop != null && ((Integer) fr[1]).intValue() == 0) {
          Locals.Info info = Locals.info(stop.c, stop.name, stop.desc);
          if (info != null && info.isStatic) {
            self = null;
          }
        }
        rep.u8(self == null ? 'L' : tagFor(self)).i64(idOf(self));
        return 0;
      }
      default:
        return E_NOT_IMPLEMENTED;
    }
  }
}
