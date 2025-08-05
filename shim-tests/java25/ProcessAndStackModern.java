/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessHandle;
import java.lang.StackWalker;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.Cleaner;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessAndStackModern {
  enum TestDay { MON, TUE, WED, THU, FRI, SAT, SUN }

  public static void main(String[] args) throws Exception {
    testProcessHandle();
    testProcessMethods();
    testStackWalker();
    testCleaner();
    testThrowableSuppression();
    testReferenceModern();
    testEnumSetModern();
    System.out.println("ProcessAndStackModern OK");
  }

  private static void testProcessHandle() {
    ProcessHandle current = ProcessHandle.current();
    long pid = current.pid();
    if (pid <= 0) throw new AssertionError("invalid current pid: " + pid);
    if (!current.isAlive()) throw new AssertionError("current process not alive");
    if (!current.supportsNormalTermination()) throw new AssertionError("supportsNormalTermination");

    Optional<ProcessHandle> lookup = ProcessHandle.of(pid);
    if (!lookup.isPresent()) throw new AssertionError("lookup failed for current pid: " + pid);
    if (lookup.get().pid() != pid) throw new AssertionError("lookup pid mismatch");
    if (lookup.get().compareTo(current) != 0) throw new AssertionError("compareTo mismatch");

    ProcessHandle.Info info = current.info();
    if (info == null) throw new AssertionError("info is null");

    long count = ProcessHandle.allProcesses().count();
    if (count <= 0) throw new AssertionError("allProcesses empty");

    if (ProcessHandle.of(-1).isPresent()) {
      throw new AssertionError("expected empty for negative pid");
    }
  }

  private static void testProcessMethods() {
    Process dummy = new Process() {
      private boolean destroyed = false;

      @Override
      public void destroy() {
        destroyed = true;
      }

      @Override
      public int exitValue() {
        if (!destroyed) {
          throw new IllegalThreadStateException("still running");
        }
        return 0;
      }

      @Override
      public InputStream getInputStream() {
        return InputStream.nullInputStream();
      }

      @Override
      public OutputStream getOutputStream() {
        return OutputStream.nullOutputStream();
      }

      @Override
      public InputStream getErrorStream() {
        return InputStream.nullInputStream();
      }

      @Override
      public int waitFor() {
        destroyed = true;
        return 0;
      }
    };

    if (!dummy.isAlive()) throw new AssertionError("dummy should be alive initially");
    dummy.destroyForcibly();
    if (dummy.isAlive()) throw new AssertionError("dummy should not be alive after destroy");
    if (dummy.exitValue() != 0) throw new AssertionError("exitValue mismatch");
  }

  private static void testStackWalker() {
    StackWalker walker = StackWalker.getInstance();
    List<String> names = walker.walk(s -> s.map(StackFrame::getMethodName).toList());
    if (!names.contains("testStackWalker")) {
      throw new AssertionError("walk frames missing testStackWalker: " + names);
    }
    if (!names.contains("main")) {
      throw new AssertionError("walk frames missing main: " + names);
    }

    AtomicInteger frameCount = new AtomicInteger();
    walker.forEach(f -> {
      if (f.getClassName() != null && f.getMethodName() != null) {
        frameCount.incrementAndGet();
      }
    });
    if (frameCount.get() <= 0) throw new AssertionError("forEach visited zero frames");

    StackWalker retainingWalker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    Class<?> caller = invokeGetCaller(retainingWalker);
    if (caller != ProcessAndStackModern.class) {
      throw new AssertionError("caller class mismatch: " + caller);
    }

    retainingWalker.walk(s -> {
      s.forEach(f -> {
        if (f.getMethodName().equals("testStackWalker")) {
          if (f.getDeclaringClass() != ProcessAndStackModern.class) {
            throw new AssertionError("declaring class mismatch in frame: " + f);
          }
          if (!f.toStackTraceElement().getMethodName().equals("testStackWalker")) {
            throw new AssertionError("toStackTraceElement mismatch");
          }
        }
      });
      return null;
    });

    try {
      walker.getCallerClass();
      throw new AssertionError("expected UOE when RETAIN_CLASS_REFERENCE not set");
    } catch (UnsupportedOperationException expected) {
    }

    try {
      StackWalker.getInstance(Set.of(), -5);
      throw new AssertionError("expected IAE for negative depth");
    } catch (IllegalArgumentException expected) {
    }
  }

  private static Class<?> invokeGetCaller(StackWalker walker) {
    return walker.getCallerClass();
  }

  private static void testCleaner() {
    Cleaner cleaner = Cleaner.create();
    AtomicBoolean actionRan = new AtomicBoolean(false);
    Cleaner.Cleanable cleanable = cleaner.register(new Object(), () -> actionRan.set(true));
    cleanable.clean();
    if (!actionRan.get()) throw new AssertionError("cleaner action did not run");

    actionRan.set(false);
    cleanable.clean();
    if (actionRan.get()) throw new AssertionError("cleaner action ran twice (not idempotent)");

    try {
      cleaner.register(null, () -> {});
      throw new AssertionError("expected NPE for null referent");
    } catch (NullPointerException expected) {
    }

    try {
      cleaner.register(new Object(), null);
      throw new AssertionError("expected NPE for null action");
    } catch (NullPointerException expected) {
    }
  }

  private static void testThrowableSuppression() {
    Throwable t = new Throwable("parent");
    Throwable[] initial = t.getSuppressed();
    if (initial == null || initial.length != 0) {
      throw new AssertionError("initial suppressed array should be empty");
    }

    Exception s1 = new Exception("suppressed 1");
    Exception s2 = new Exception("suppressed 2");
    t.addSuppressed(s1);
    t.addSuppressed(s2);

    Throwable[] supp = t.getSuppressed();
    if (supp.length != 2 || supp[0] != s1 || supp[1] != s2) {
      throw new AssertionError("suppressed array mismatch: " + Arrays.toString(supp));
    }

    try {
      t.addSuppressed(null);
      throw new AssertionError("expected NPE on addSuppressed(null)");
    } catch (NullPointerException expected) {
    }

    try {
      t.addSuppressed(t);
      throw new AssertionError("expected IAE on addSuppressed(this)");
    } catch (IllegalArgumentException expected) {
    }

    CustomThrowable disabled = new CustomThrowable("disabled", false);
    disabled.addSuppressed(s1);
    if (disabled.getSuppressed().length != 0) {
      throw new AssertionError("suppression should be disabled");
    }
  }

  private static class CustomThrowable extends Throwable {
    CustomThrowable(String message, boolean enableSuppression) {
      super(message, null, enableSuppression, true);
    }
  }

  private static void testReferenceModern() throws Exception {
    Object target = new Object();
    Reference.reachabilityFence(target);

    WeakReference<Object> wr = new WeakReference<Object>(target);
    if (!wr.refersTo(target)) throw new AssertionError("refersTo target should be true");
    if (wr.refersTo(null)) throw new AssertionError("refersTo null should be false");
    if (wr.refersTo(new Object())) throw new AssertionError("refersTo other should be false");

    ReferenceQueue<Object> q = new ReferenceQueue<Object>();
    PhantomReference<Object> pr = new PhantomReference<Object>(target, q);
    if (!pr.refersTo(target)) throw new AssertionError("phantom refersTo target should be true");
    if (pr.get() != null) throw new AssertionError("phantom get() should be null");

    if (q.poll() != null) throw new AssertionError("poll should be null initially");
    Reference<?> timed = q.remove(10);
    if (timed != null) throw new AssertionError("timed remove should be null on empty queue");
  }

  private static void testEnumSetModern() {
    List<TestDay> list = List.of(TestDay.MON, TestDay.WED, TestDay.FRI);
    EnumSet<TestDay> setFromCollection = EnumSet.copyOf(list);
    if (setFromCollection.size() != 3) throw new AssertionError("size mismatch: " + setFromCollection);
    if (!setFromCollection.contains(TestDay.MON) || !setFromCollection.contains(TestDay.WED) || !setFromCollection.contains(TestDay.FRI)) {
      throw new AssertionError("content mismatch: " + setFromCollection);
    }

    EnumSet<TestDay> copiedSet = EnumSet.copyOf(setFromCollection);
    if (copiedSet.size() != 3) throw new AssertionError("copied set size mismatch");

    EnumSet<TestDay> weekdays = EnumSet.range(TestDay.MON, TestDay.FRI);
    if (weekdays.size() != 5) throw new AssertionError("range size mismatch: " + weekdays);
    if (weekdays.contains(TestDay.SAT) || weekdays.contains(TestDay.SUN)) {
      throw new AssertionError("range contains weekends: " + weekdays);
    }

    try {
      EnumSet.range(TestDay.FRI, TestDay.MON);
      throw new AssertionError("expected IAE for inverted range");
    } catch (IllegalArgumentException expected) {
    }

    try {
      EnumSet.copyOf(List.<TestDay>of());
      throw new AssertionError("expected IAE for empty collection");
    } catch (IllegalArgumentException expected) {
    }
  }
}
