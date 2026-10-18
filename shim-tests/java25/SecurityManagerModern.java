/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.concurrent.Callable;
public class SecurityManagerModern {
  static void t(String n, Callable<Object> c) { try { c.call(); System.out.println(n + ": ok"); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  @SuppressWarnings("removal")
  public static void main(String[] a) throws Exception {
    SecurityManager sm = new SecurityManager();
    t("cl", () -> { sm.checkCreateClassLoader(); return 0; });
    t("accessT", () -> { sm.checkAccess(Thread.currentThread()); return 0; });
    t("accessTnull", () -> { sm.checkAccess((Thread) null); return 0; });
    t("accessG", () -> { sm.checkAccess(Thread.currentThread().getThreadGroup()); return 0; });
    t("accessGroot", () -> { ThreadGroup g = Thread.currentThread().getThreadGroup(); while (g.getParent() != null) g = g.getParent(); sm.checkAccess(g); return 0; });
    t("accessGnull", () -> { sm.checkAccess((ThreadGroup) null); return 0; });
    t("exit", () -> { sm.checkExit(3); return 0; });
    t("exec", () -> { sm.checkExec("ls"); return 0; });
    t("execAbs", () -> { sm.checkExec("/bin/ls"); return 0; });
    t("link", () -> { sm.checkLink("foo"); return 0; });
    t("linkNull", () -> { sm.checkLink(null); return 0; });
    t("readFd", () -> { sm.checkRead(FileDescriptor.in); return 0; });
    t("readFdNull", () -> { sm.checkRead((FileDescriptor) null); return 0; });
    t("read", () -> { sm.checkRead("/etc/passwd"); return 0; });
    t("readCtx", () -> { sm.checkRead("/etc/passwd", AccessController.getContext()); return 0; });
    t("readCtxBad", () -> { sm.checkRead("/etc/passwd", "ctx"); return 0; });
    t("writeFd", () -> { sm.checkWrite(FileDescriptor.out); return 0; });
    t("write", () -> { sm.checkWrite("/tmp/x"); return 0; });
    t("delete", () -> { sm.checkDelete("/tmp/x"); return 0; });
    t("connect", () -> { sm.checkConnect("example.com", 80); return 0; });
    t("connectResolve", () -> { sm.checkConnect("example.com", -1); return 0; });
    t("connectV6", () -> { sm.checkConnect("::1", 80); return 0; });
    t("connectNull", () -> { sm.checkConnect(null, 80); return 0; });
    t("connectCtx", () -> { sm.checkConnect("example.com", 80, AccessController.getContext()); return 0; });
    t("listen", () -> { sm.checkListen(8080); return 0; });
    t("listen0", () -> { sm.checkListen(0); return 0; });
    t("accept", () -> { sm.checkAccept("example.com", 80); return 0; });
    t("acceptNull", () -> { sm.checkAccept(null, 80); return 0; });
    t("multicast", () -> { sm.checkMulticast(InetAddress.getByName("224.0.0.1")); return 0; });
    t("props", () -> { sm.checkPropertiesAccess(); return 0; });
    t("prop", () -> { sm.checkPropertyAccess("user.home"); return 0; });
    t("propNull", () -> { sm.checkPropertyAccess(null); return 0; });
    t("propEmpty", () -> { sm.checkPropertyAccess(""); return 0; });
    t("print", () -> { sm.checkPrintJobAccess(); return 0; });
    t("pkg", () -> { sm.checkPackageAccess("java.lang"); return 0; });
    t("pkgRestricted", () -> { sm.checkPackageAccess("sun.misc"); return 0; });
    t("pkgNull", () -> { sm.checkPackageAccess(null); return 0; });
    t("pkgDef", () -> { sm.checkPackageDefinition("java.lang"); return 0; });
    t("pkgDefR", () -> { sm.checkPackageDefinition("sun.misc"); return 0; });
    t("factory", () -> { sm.checkSetFactory(); return 0; });
    t("secAccess", () -> { sm.checkSecurityAccess("getProperty.x"); return 0; });
    t("perm", () -> { sm.checkPermission(new RuntimePermission("x")); return 0; });
    t("permCtx", () -> { sm.checkPermission(new RuntimePermission("x"), AccessController.getContext()); return 0; });
    t("permCtxBad", () -> { sm.checkPermission(new RuntimePermission("x"), null); return 0; });
    t("ctx", () -> sm.getSecurityContext() instanceof AccessControlContext);
    t("tg", () -> sm.getThreadGroup() == Thread.currentThread().getThreadGroup());
  }
}
