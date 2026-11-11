/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.util.concurrent.*; import java.util.concurrent.locks.*; import java.util.concurrent.atomic.*;
public class ConditionSemanticsModern { public static void main(String[] a) throws Exception {
 ReentrantLock l=new ReentrantLock(); Condition c=l.newCondition(); AtomicInteger woke=new AtomicInteger(); ExecutorService ex=Executors.newFixedThreadPool(4);
 for (int i=0;i<3;i++) ex.submit(()->{ l.lock(); try{ c.await(); woke.incrementAndGet(); } catch(InterruptedException e){} finally{ l.unlock(); } });
 Thread.sleep(200); l.lock(); System.out.println(l.getWaitQueueLength(c)+" "+l.hasWaiters(c)); c.signal(); l.unlock(); Thread.sleep(200); System.out.println("after one signal woke="+woke.get()); l.lock(); System.out.println(l.getWaitQueueLength(c)); c.signalAll(); l.unlock(); Thread.sleep(200); System.out.println("woke="+woke.get());
 // timed wait signalled
 Future<Boolean> f=ex.submit(()->{ l.lock(); try{ return c.await(2,TimeUnit.SECONDS);} finally{l.unlock();} }); Thread.sleep(100); l.lock(); c.signal(); l.unlock(); System.out.println("signalled "+f.get());
 Future<Boolean> g=ex.submit(()->{ l.lock(); try{ return c.await(100,TimeUnit.MILLISECONDS);} finally{l.unlock();} }); System.out.println("timedout "+g.get());
 // interrupt
 Thread[] t=new Thread[1]; Future<String> h=ex.submit(()->{ t[0]=Thread.currentThread(); l.lock(); try{ c.await(); return "no"; } catch(InterruptedException e){ return "interrupted held="+l.isHeldByCurrentThread(); } finally{ l.unlock(); } }); Thread.sleep(100); t[0].interrupt(); System.out.println(h.get());
 Future<String> u=ex.submit(()->{ t[0]=Thread.currentThread(); l.lock(); try{ c.awaitUninterruptibly(); return "woken intr="+Thread.currentThread().isInterrupted(); } finally{ l.unlock(); } }); Thread.sleep(100); t[0].interrupt(); Thread.sleep(50); l.lock(); c.signal(); l.unlock(); System.out.println(u.get());
 try { c.await(); } catch(IllegalMonitorStateException e){ System.out.println("imse"); }
 try { c.signal(); } catch(IllegalMonitorStateException e){ System.out.println("imse2"); }
 ex.shutdown(); System.out.println(ex.awaitTermination(2,TimeUnit.SECONDS)); }}
