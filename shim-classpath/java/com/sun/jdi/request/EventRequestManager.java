/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.request;

public interface EventRequestManager extends com.sun.jdi.Mirror {
  java.util.List<com.sun.jdi.request.AccessWatchpointRequest> accessWatchpointRequests();
  java.util.List<com.sun.jdi.request.BreakpointRequest> breakpointRequests();
  java.util.List<com.sun.jdi.request.ClassPrepareRequest> classPrepareRequests();
  java.util.List<com.sun.jdi.request.ClassUnloadRequest> classUnloadRequests();
  com.sun.jdi.request.AccessWatchpointRequest createAccessWatchpointRequest(com.sun.jdi.Field field);
  com.sun.jdi.request.BreakpointRequest createBreakpointRequest(com.sun.jdi.Location location);
  com.sun.jdi.request.ClassPrepareRequest createClassPrepareRequest();
  com.sun.jdi.request.ClassUnloadRequest createClassUnloadRequest();
  com.sun.jdi.request.ExceptionRequest createExceptionRequest(com.sun.jdi.ReferenceType referenceType, boolean flag, boolean flag2);
  com.sun.jdi.request.MethodEntryRequest createMethodEntryRequest();
  com.sun.jdi.request.MethodExitRequest createMethodExitRequest();
  com.sun.jdi.request.ModificationWatchpointRequest createModificationWatchpointRequest(com.sun.jdi.Field field);
  com.sun.jdi.request.MonitorContendedEnterRequest createMonitorContendedEnterRequest();
  com.sun.jdi.request.MonitorContendedEnteredRequest createMonitorContendedEnteredRequest();
  com.sun.jdi.request.MonitorWaitRequest createMonitorWaitRequest();
  com.sun.jdi.request.MonitorWaitedRequest createMonitorWaitedRequest();
  com.sun.jdi.request.StepRequest createStepRequest(com.sun.jdi.ThreadReference threadReference, int i, int i2);
  com.sun.jdi.request.ThreadDeathRequest createThreadDeathRequest();
  com.sun.jdi.request.ThreadStartRequest createThreadStartRequest();
  com.sun.jdi.request.VMDeathRequest createVMDeathRequest();
  void deleteAllBreakpoints();
  void deleteEventRequest(com.sun.jdi.request.EventRequest eventRequest);
  void deleteEventRequests(java.util.List<? extends com.sun.jdi.request.EventRequest> list);
  java.util.List<com.sun.jdi.request.ExceptionRequest> exceptionRequests();
  java.util.List<com.sun.jdi.request.MethodEntryRequest> methodEntryRequests();
  java.util.List<com.sun.jdi.request.MethodExitRequest> methodExitRequests();
  java.util.List<com.sun.jdi.request.ModificationWatchpointRequest> modificationWatchpointRequests();
  java.util.List<com.sun.jdi.request.MonitorContendedEnterRequest> monitorContendedEnterRequests();
  java.util.List<com.sun.jdi.request.MonitorContendedEnteredRequest> monitorContendedEnteredRequests();
  java.util.List<com.sun.jdi.request.MonitorWaitRequest> monitorWaitRequests();
  java.util.List<com.sun.jdi.request.MonitorWaitedRequest> monitorWaitedRequests();
  java.util.List<com.sun.jdi.request.StepRequest> stepRequests();
  java.util.List<com.sun.jdi.request.ThreadDeathRequest> threadDeathRequests();
  java.util.List<com.sun.jdi.request.ThreadStartRequest> threadStartRequests();
  java.util.List<com.sun.jdi.request.VMDeathRequest> vmDeathRequests();
}
