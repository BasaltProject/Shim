/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.util.concurrent.CountDownLatch;

public class ThreadEnumerationLifecycle {
  static int checks;
  static void expect(boolean condition) {if(!condition)throw new AssertionError("Thread enumeration check "+checks);++checks;}
  static void check(Thread expected) {
    Thread[] threads=new Thread[Thread.activeCount()+16];int count=Thread.enumerate(threads);
    boolean found=false,liveOnly=true;
    for(int i=0;i<count;++i){liveOnly &= threads[i]!=null && threads[i].isAlive();found |= threads[i]==expected;}
    expect(liveOnly);expect(found);
    expect(Thread.getAllStackTraces().containsKey(expected));
    count=Thread.currentThread().getThreadGroup().enumerate(threads);
    found=false;liveOnly=true;
    for(int i=0;i<count;++i){liveOnly &= threads[i]!=null && threads[i].isAlive();found |= threads[i]==expected;}
    expect(liveOnly);expect(found);
  }
  public static void main(String[] args) throws Exception {
    CountDownLatch ready=new CountDownLatch(1),release=new CountDownLatch(1);
    Thread holder=new Thread(() -> {
      ready.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}
    },"live-enumeration-target");holder.start();ready.await();
    Thread[] ended=new Thread[4];
    try {
      for(int i=0;i<ended.length;++i){ended[i]=new Thread(() -> {},"finished-enumeration-"+i);ended[i].start();ended[i].join();}
      Thread.sleep(30);check(holder);
      for(int size:new int[]{0,1,2}) {Thread[] small=new Thread[size];int n=Thread.enumerate(small);expect(n>=0 && n<=size);boolean valid=true;for(int i=0;i<n;++i)valid &= small[i]!=null && small[i].isAlive();expect(valid);}
    } finally {release.countDown();holder.join();}
    Thread.sleep(30);
    java.util.Map<Thread,StackTraceElement[]> snapshot=Thread.getAllStackTraces();
    expect(!snapshot.containsKey(holder));for(Thread thread:ended)expect(!snapshot.containsKey(thread));
    System.out.println("Thread enumeration checks="+checks);
  }
}
