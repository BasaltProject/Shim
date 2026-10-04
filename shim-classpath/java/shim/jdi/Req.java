/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import shim.jdi.Wire.In;
import shim.jdi.Wire.Out;

public final class Req {
  final Vm vm;
  final int kind;
  int policy = 2;
  int id = -1;
  boolean enabled;
  EventRequest self;
  final List<byte[]> mods = new ArrayList<byte[]>();
  final Map<Object, Object> props = new HashMap<Object, Object>();
  Location location;
  ThreadReference thread;
  int size;
  int depth;
  ReferenceType exceptionType;
  boolean caught;
  boolean uncaught;

  Req(Vm vm, int kind) {
    this.vm = vm;
    this.kind = kind;
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public void enable() {
    setEnabled(true);
  }

  public void disable() {
    setEnabled(false);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public synchronized void setEnabled(boolean on) {
    if (on == enabled) {
      return;
    }
    if (on) {
      Out o = new Out().u8(kind).u8(policy).i32(mods.size());
      for (byte[] m : mods) {
        for (byte b : m) {
          o.u8(b);
        }
      }
      id = vm.call(15, 1, o).data.i32();
      vm.requests.put(id, this);
    } else {
      vm.call(15, 2, new Out().u8(kind).i32(id));
      vm.requests.remove(id);
    }
    enabled = on;
  }

  public void setSuspendPolicy(int p) {
    if (enabled) {
      throw new InvalidRequestStateException();
    }
    policy = p == EventRequest.SUSPEND_NONE ? 0 : p == EventRequest.SUSPEND_EVENT_THREAD ? 1 : 2;
  }

  public int suspendPolicy() {
    return policy == 0 ? EventRequest.SUSPEND_NONE : policy == 1 ? EventRequest.SUSPEND_EVENT_THREAD
        : EventRequest.SUSPEND_ALL;
  }

  public void putProperty(Object k, Object v) {
    props.put(k, v);
  }

  public Object getProperty(Object k) {
    return props.get(k);
  }

  private void check() {
    if (enabled) {
      throw new InvalidRequestStateException();
    }
  }

  private void mod(Out o) {
    check();
    mods.add(o.bytes());
  }

  public void addCountFilter(int n) {
    if (n < 1) {
      throw new IllegalArgumentException("count is less than one");
    }
    mod(new Out().u8(1).i32(n));
  }

  public void addThreadFilter(ThreadReference t) {
    mod(new Out().u8(3).id(vm.idOf(t), vm.w.objectIdSize));
  }

  public void addClassFilter(ReferenceType t) {
    mod(new Out().u8(4).id(((RefType) Proxies.implOf(t)).id, vm.w.typeIdSize));
  }

  public void addClassFilter(String pattern) {
    mod(new Out().u8(5).str(pattern));
  }

  public void addClassExclusionFilter(String pattern) {
    mod(new Out().u8(6).str(pattern));
  }

  public void addInstanceFilter(ObjectReference o) {
    mod(new Out().u8(11).id(vm.idOf(o), vm.w.objectIdSize));
  }

  public void addSourceNameFilter(String pattern) {
    mod(new Out().u8(12).str(pattern));
  }

  public void addLocationFilter(Location l) {
    Out o = new Out().u8(7);
    ((Loc) Proxies.implOf(l)).write(o);
    mod(o);
  }

  public Location location() {
    return location;
  }

  public ThreadReference thread() {
    return thread;
  }

  public int size() {
    return size;
  }

  public int depth() {
    return depth;
  }

  public ReferenceType exception() {
    return exceptionType;
  }

  public boolean notifyCaught() {
    return caught;
  }

  public boolean notifyUncaught() {
    return uncaught;
  }

  public Field field() {
    return null;
  }

  public String toString() {
    return "request kind=" + kind + " id=" + id;
  }

  /* ---------- manager ---------- */

  public static final class Manager {
    final Vm vm;
    final List<Req> all = new ArrayList<Req>();

    Manager(Vm vm) {
      this.vm = vm;
    }

    public VirtualMachine virtualMachine() {
      return vm.proxy;
    }

    private synchronized <T extends EventRequest> T add(Req r, Class<T> c) {
      r.self = Proxies.make(r, c);
      all.add(r);
      @SuppressWarnings("unchecked")
      T t = (T) r.self;
      return t;
    }

    public BreakpointRequest createBreakpointRequest(Location l) {
      Req r = new Req(vm, 2);
      r.location = l;
      r.addLocationFilter(l);
      return add(r, BreakpointRequest.class);
    }

    public StepRequest createStepRequest(ThreadReference t, int size, int depth) {
      Req r = new Req(vm, 1);
      r.thread = t;
      r.size = size;
      r.depth = depth;
      r.policy = 1;
      r.mods.add(new Out().u8(10).id(vm.idOf(t), vm.w.objectIdSize).i32(size == StepRequest.STEP_MIN ? 0 : 1)
          .i32(depth == StepRequest.STEP_INTO ? 0 : depth == StepRequest.STEP_OVER ? 1 : 2).bytes());
      return add(r, StepRequest.class);
    }

    public ClassPrepareRequest createClassPrepareRequest() {
      return add(new Req(vm, 8), ClassPrepareRequest.class);
    }

    public ClassUnloadRequest createClassUnloadRequest() {
      return add(new Req(vm, 9), ClassUnloadRequest.class);
    }

    public ThreadStartRequest createThreadStartRequest() {
      return add(new Req(vm, 6), ThreadStartRequest.class);
    }

    public ThreadDeathRequest createThreadDeathRequest() {
      return add(new Req(vm, 7), ThreadDeathRequest.class);
    }

    public ExceptionRequest createExceptionRequest(ReferenceType t, boolean caught, boolean uncaught) {
      Req r = new Req(vm, 4);
      r.exceptionType = t;
      r.caught = caught;
      r.uncaught = uncaught;
      r.mods.add(new Out().u8(8).id(t == null ? 0 : ((RefType) Proxies.implOf(t)).id, vm.w.typeIdSize)
          .bool(caught).bool(uncaught).bytes());
      return add(r, ExceptionRequest.class);
    }

    public MethodEntryRequest createMethodEntryRequest() {
      return add(new Req(vm, 40), MethodEntryRequest.class);
    }

    public MethodExitRequest createMethodExitRequest() {
      return add(new Req(vm, 41), MethodExitRequest.class);
    }

    public VMDeathRequest createVMDeathRequest() {
      return add(new Req(vm, 99), VMDeathRequest.class);
    }

    private <T> List<T> of(int kind) {
      List<T> l = new ArrayList<T>();
      synchronized (this) {
        for (Req r : all) {
          if (r.kind == kind) {
            @SuppressWarnings("unchecked")
            T t = (T) r.self;
            l.add(t);
          }
        }
      }
      return l;
    }

    public List<StepRequest> stepRequests() {
      return of(1);
    }

    public List<BreakpointRequest> breakpointRequests() {
      return of(2);
    }

    public List<ClassPrepareRequest> classPrepareRequests() {
      return of(8);
    }

    public List<ClassUnloadRequest> classUnloadRequests() {
      return of(9);
    }

    public List<ThreadStartRequest> threadStartRequests() {
      return of(6);
    }

    public List<ThreadDeathRequest> threadDeathRequests() {
      return of(7);
    }

    public List<ExceptionRequest> exceptionRequests() {
      return of(4);
    }

    public List<MethodEntryRequest> methodEntryRequests() {
      return of(40);
    }

    public List<MethodExitRequest> methodExitRequests() {
      return of(41);
    }

    public List<VMDeathRequest> vmDeathRequests() {
      return of(99);
    }

    public synchronized void deleteEventRequest(EventRequest er) {
      Req r = (Req) Proxies.implOf(er);
      if (r.enabled) {
        r.disable();
      }
      all.remove(r);
    }

    public void deleteEventRequests(List<? extends EventRequest> l) {
      for (EventRequest r : new ArrayList<EventRequest>(l)) {
        deleteEventRequest(r);
      }
    }

    public synchronized void deleteAllBreakpoints() {
      for (Req r : new ArrayList<Req>(all)) {
        if (r.kind == 2) {
          deleteEventRequest(r.self);
        }
      }
    }
  }
}
