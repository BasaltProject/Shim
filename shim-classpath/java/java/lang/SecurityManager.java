/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;

public class SecurityManager {
  public SecurityManager() {
  }

  protected Class<?>[] getClassContext() {
    Class<?>[] classes = new Throwable().stackClasses();
    if (classes == null) return new Class<?>[0];
    int skip = 0;
    while (skip < classes.length
           && (classes[skip] == Throwable.class || classes[skip] == SecurityManager.class)) {
      skip++;
    }
    Class<?>[] context = new Class<?>[classes.length - skip];
    System.arraycopy(classes, skip, context, 0, context.length);
    return context;
  }

  public Object getSecurityContext() {
    return AccessController.getContext();
  }

  public void checkPermission(Permission perm) {
    throw new SecurityException();
  }

  public void checkPermission(Permission perm, Object context) {
    throw new SecurityException();
  }

  public void checkCreateClassLoader() {
    throw new SecurityException();
  }

  public void checkAccess(Thread t) {
    throw new SecurityException();
  }

  public void checkAccess(ThreadGroup g) {
    throw new SecurityException();
  }

  public void checkExit(int status) {
    throw new SecurityException();
  }

  public void checkExec(String cmd) {
    throw new SecurityException();
  }

  public void checkLink(String lib) {
    throw new SecurityException();
  }

  public void checkRead(FileDescriptor fd) {
    throw new SecurityException();
  }

  public void checkRead(String file) {
    throw new SecurityException();
  }

  public void checkRead(String file, Object context) {
    throw new SecurityException();
  }

  public void checkWrite(FileDescriptor fd) {
    throw new SecurityException();
  }

  public void checkWrite(String file) {
    throw new SecurityException();
  }

  public void checkDelete(String file) {
    throw new SecurityException();
  }

  public void checkConnect(String host, int port) {
    throw new SecurityException();
  }

  public void checkConnect(String host, int port, Object context) {
    throw new SecurityException();
  }

  public void checkListen(int port) {
    throw new SecurityException();
  }

  public void checkAccept(String host, int port) {
    throw new SecurityException();
  }

  public void checkMulticast(InetAddress maddr) {
    throw new SecurityException();
  }

  public void checkPropertiesAccess() {
    throw new SecurityException();
  }

  public void checkPropertyAccess(String key) {
    throw new SecurityException();
  }

  public void checkPrintJobAccess() {
    throw new SecurityException();
  }

  public void checkPackageAccess(String pkg) {
    throw new SecurityException();
  }

  public void checkPackageDefinition(String pkg) {
    throw new SecurityException();
  }

  public void checkSetFactory() {
    throw new SecurityException();
  }

  public void checkSecurityAccess(String target) {
    throw new SecurityException();
  }

  @Deprecated(since = "1.4")
  public void checkMulticast(InetAddress maddr, byte ttl) {
    throw new SecurityException();
  }

  public ThreadGroup getThreadGroup() {
    return Thread.currentThread().getThreadGroup();
  }
}
