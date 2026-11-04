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
import com.sun.jdi.request.EventRequest;

public final class Evt {
  final Vm vm;
  final int kind;
  final Req req;
  ThreadReference thread;
  Location location;
  Location catchLocation;
  RefType refType;
  Object exception;
  Object value;
  String className;

  Evt(Vm vm, int kind, Req req) {
    this.vm = vm;
    this.kind = kind;
    this.req = req;
  }

  Event make() {
    Class<?> c;
    switch (kind) {
      case 1:
        c = StepEvent.class;
        break;
      case 2:
        c = BreakpointEvent.class;
        break;
      case 4:
        c = ExceptionEvent.class;
        break;
      case 6:
        c = ThreadStartEvent.class;
        break;
      case 7:
        c = ThreadDeathEvent.class;
        break;
      case 8:
        c = ClassPrepareEvent.class;
        break;
      case 9:
        c = ClassUnloadEvent.class;
        break;
      case 40:
        c = MethodEntryEvent.class;
        break;
      case 41:
      case 42:
        c = MethodExitEvent.class;
        break;
      case 90:
        c = VMStartEvent.class;
        break;
      case 99:
        c = VMDeathEvent.class;
        break;
      default:
        return null;
    }
    return Proxies.make(this, c);
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public EventRequest request() {
    return req == null ? null : req.self;
  }

  public ThreadReference thread() {
    return thread;
  }

  public Location location() {
    return location;
  }

  public Method method() {
    return location.method();
  }

  public ReferenceType referenceType() {
    return refType.self;
  }

  public ObjectReference exception() {
    return (ObjectReference) exception;
  }

  public Location catchLocation() {
    return catchLocation;
  }

  public Value returnValue() {
    return (Value) value;
  }

  public String classSignature() {
    return className;
  }

  public String className() {
    return className;
  }

  public String toString() {
    return "Event[kind=" + kind + "]";
  }
}
