/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.nio.file.attribute.FileTime;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.zip.*;

public class ZipEntryAndLocksModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void main(String[] a) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    ZipEntry e = new ZipEntry("x");
    t("zeInit", () -> e.getLastModifiedTime() != null);
    e.setTime(1700000000000L);
    t("zt", () -> e.getTime() + " " + e.getLastModifiedTime() + " " + e.getLastAccessTime() + " " + e.getCreationTime());
    t("zlocal", () -> e.getTimeLocal());
    t("setLocal", () -> { e.setTimeLocal(LocalDateTime.of(2001, 2, 3, 4, 5, 7)); return e.getTime() + " " + e.getTimeLocal() + " " + e.getLastModifiedTime(); });
    t("setLocalOld", () -> { e.setTimeLocal(LocalDateTime.of(1970, 2, 3, 4, 5, 7)); return e.getTime() + " " + e.getTimeLocal(); });
    t("setMtime", () -> { e.setLastModifiedTime(FileTime.fromMillis(1234567890123L)); return e.getTime() + " " + e.getLastModifiedTime() + " " + e.getTimeLocal(); });
    t("setAtime", () -> { e.setLastAccessTime(FileTime.fromMillis(1000L)); e.setCreationTime(FileTime.fromMillis(2000L)); return e.getLastAccessTime() + " " + e.getCreationTime() + " " + e.getTime(); });
    t("setNull", () -> e.setLastModifiedTime(null));
    t("setTimeNeg", () -> { e.setTime(-5); return e.getTime(); });
    t("setTimeLocalNull", () -> { e.setTimeLocal(null); return e.getTime(); });
    t("zeSetReturn", () -> e.setLastAccessTime(FileTime.fromMillis(5)) == e);
    t("zosm", () -> { ByteArrayOutputStream b = new ByteArrayOutputStream(); ZipOutputStream z = new ZipOutputStream(b); z.setMethod(ZipOutputStream.STORED); z.setLevel(9); z.setComment("hi"); ZipEntry s = new ZipEntry("s"); s.setMethod(ZipEntry.STORED); s.setSize(1); s.setCompressedSize(1); s.setCrc(0); try { z.putNextEntry(s); } catch (Exception ex) { return ex.getMessage(); } return "ok"; });
    t("zosBadMethod", () -> { new ZipOutputStream(new ByteArrayOutputStream()).setMethod(3); return "ok"; });
    t("zosBadLevel", () -> { new ZipOutputStream(new ByteArrayOutputStream()).setLevel(10); return "ok"; });
    t("zosComment", () -> { new ZipOutputStream(new ByteArrayOutputStream()).setComment("x".repeat(70000)); return "ok"; });
    t("zosCharset", () -> { ByteArrayOutputStream b = new ByteArrayOutputStream(); ZipOutputStream z = new ZipOutputStream(b, java.nio.charset.StandardCharsets.UTF_8); z.putNextEntry(new ZipEntry("é.txt")); z.write(1); z.close(); return b.size() > 0; });
    t("zosRoundtrip", () -> { ByteArrayOutputStream b = new ByteArrayOutputStream(); ZipOutputStream z = new ZipOutputStream(b); z.setComment("cmt"); z.setLevel(0); ZipEntry ze = new ZipEntry("a.txt"); ze.setTime(1700000000000L); z.putNextEntry(ze); z.write(new byte[100]); z.close(); ZipInputStream zi = new ZipInputStream(new ByteArrayInputStream(b.toByteArray())); ZipEntry r = zi.getNextEntry(); return r.getName() + " " + r.getMethod() + " " + (r.getTime() / 2000 * 2000) ; });
    StampedLock sl = new StampedLock();
    t("sl", () -> { long w = sl.writeLockInterruptibly(); boolean ok = sl.tryUnlockWrite(); boolean again = sl.tryUnlockWrite(); long r = sl.readLockInterruptibly(); boolean ru = sl.tryUnlockRead(); boolean ru2 = sl.tryUnlockRead(); return (w != 0) + " " + ok + " " + again + " " + (r != 0) + " " + ru + " " + ru2 + " " + sl.isWriteLocked() + sl.getReadLockCount(); });
    t("slInterrupted", () -> { Thread.currentThread().interrupt(); try { sl.writeLockInterruptibly(); return "no"; } finally { Thread.interrupted(); } });
    t("slInterruptedR", () -> { Thread.currentThread().interrupt(); try { sl.readLockInterruptibly(); return "no"; } finally { Thread.interrupted(); } });
    ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    t("rw", () -> rw.hasQueuedThreads() + " " + rw.getQueueLength() + " " + rw.hasQueuedThread(Thread.currentThread()));
    t("rwq", () -> { rw.writeLock().lock(); Thread th = new Thread(() -> { rw.readLock().lock(); rw.readLock().unlock(); }); th.start(); while (rw.getQueueLength() == 0) Thread.sleep(5); String s = rw.hasQueuedThreads() + " " + rw.getQueueLength() + " " + rw.hasQueuedThread(th) + " " + rw.hasQueuedThread(Thread.currentThread()); rw.writeLock().unlock(); th.join(); return s; });
    t("rwNull", () -> rw.hasQueuedThread(null));
    t("rwWaiters", () -> { Condition c = rw.writeLock().newCondition(); rw.writeLock().lock(); try { return rw.hasWaiters(c) + " " + rw.getWaitQueueLength(c); } finally { rw.writeLock().unlock(); } });
    t("rwWaitersNoLock", () -> rw.hasWaiters(rw.writeLock().newCondition()));
    t("rwWaitersNull", () -> rw.hasWaiters(null));
    t("rwWaitersForeign", () -> rw.hasWaiters(new ReentrantLock().newCondition()));
    t("rwCond", () -> {
      ReentrantReadWriteLock l = new ReentrantReadWriteLock();
      Condition c = l.writeLock().newCondition();
      boolean[] done = new boolean[1];
      Thread th = new Thread(() -> { l.writeLock().lock(); try { while (!done[0]) c.awaitUninterruptibly(); } finally { l.writeLock().unlock(); } });
      th.start();
      while (true) { l.writeLock().lock(); try { if (l.hasWaiters(c)) break; } finally { l.writeLock().unlock(); } Thread.sleep(5); }
      l.writeLock().lock();
      String r = l.getWaitQueueLength(c) + " " + l.isWriteLockedByCurrentThread();
      done[0] = true; c.signalAll(); l.writeLock().unlock();
      th.join();
      l.writeLock().lock();
      try { return r + " " + l.hasWaiters(c) + " " + c.await(10, java.util.concurrent.TimeUnit.MILLISECONDS) + " " + l.getWriteHoldCount(); } finally { l.writeLock().unlock(); }
    });
    t("rwWaitersRead", () -> rw.getWaitQueueLength(rw.readLock().newCondition()));
  }
}
