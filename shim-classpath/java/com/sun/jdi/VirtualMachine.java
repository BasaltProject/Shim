/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface VirtualMachine extends com.sun.jdi.Mirror {
  int TRACE_NONE = 0;
  int TRACE_SENDS = 1;
  int TRACE_RECEIVES = 2;
  int TRACE_EVENTS = 4;
  int TRACE_REFTYPES = 8;
  int TRACE_OBJREFS = 16;
  int TRACE_ALL = 16777215;

  java.util.List<com.sun.jdi.ReferenceType> allClasses();
  default java.util.List<com.sun.jdi.ModuleReference> allModules() {
    throw new UnsupportedOperationException();
  }
  java.util.List<com.sun.jdi.ThreadReference> allThreads();
  boolean canAddMethod();
  boolean canBeModified();
  boolean canForceEarlyReturn();
  boolean canGetBytecodes();
  boolean canGetClassFileVersion();
  boolean canGetConstantPool();
  boolean canGetCurrentContendedMonitor();
  boolean canGetInstanceInfo();
  boolean canGetMethodReturnValues();
  default boolean canGetModuleInfo() {
    throw new UnsupportedOperationException();
  }
  boolean canGetMonitorFrameInfo();
  boolean canGetMonitorInfo();
  boolean canGetOwnedMonitorInfo();
  boolean canGetSourceDebugExtension();
  boolean canGetSyntheticAttribute();
  boolean canPopFrames();
  boolean canRedefineClasses();
  boolean canRequestMonitorEvents();
  boolean canRequestVMDeathEvent();
  boolean canUnrestrictedlyRedefineClasses();
  boolean canUseInstanceFilters();
  boolean canUseSourceNameFilters();
  boolean canWatchFieldAccess();
  boolean canWatchFieldModification();
  java.util.List<com.sun.jdi.ReferenceType> classesByName(java.lang.String string);
  java.lang.String description();
  void dispose();
  com.sun.jdi.event.EventQueue eventQueue();
  com.sun.jdi.request.EventRequestManager eventRequestManager();
  void exit(int i);
  java.lang.String getDefaultStratum();
  long[] instanceCounts(java.util.List<? extends com.sun.jdi.ReferenceType> list);
  com.sun.jdi.BooleanValue mirrorOf(boolean flag);
  com.sun.jdi.ByteValue mirrorOf(byte b);
  com.sun.jdi.CharValue mirrorOf(char c);
  com.sun.jdi.StringReference mirrorOf(java.lang.String string);
  com.sun.jdi.DoubleValue mirrorOf(double d);
  com.sun.jdi.FloatValue mirrorOf(float f);
  com.sun.jdi.IntegerValue mirrorOf(int i);
  com.sun.jdi.LongValue mirrorOf(long l);
  com.sun.jdi.ShortValue mirrorOf(short s);
  com.sun.jdi.VoidValue mirrorOfVoid();
  java.lang.String name();
  java.lang.Process process();
  void redefineClasses(java.util.Map<? extends com.sun.jdi.ReferenceType, byte[]> map);
  void resume();
  void setDebugTraceMode(int i);
  void setDefaultStratum(java.lang.String string);
  void suspend();
  java.util.List<com.sun.jdi.ThreadGroupReference> topLevelThreadGroups();
  java.lang.String version();
}
