/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class BarriersAndStampedLockModern {

  public static void main(String[] args) throws Exception {
    testCyclicBarrier();
    testExchanger();
    testPhaser();
    testStampedLock();
    System.out.println("BarriersAndStampedLockModern passed successfully");
  }

  private static void testCyclicBarrier() throws Exception {
    AtomicInteger actionCount = new AtomicInteger(0);
    CyclicBarrier barrier = new CyclicBarrier(3, actionCount::incrementAndGet);

    System.out.println("parties: " + barrier.getParties());
    System.out.println("initial waiting: " + barrier.getNumberWaiting());
    System.out.println("isBroken initial: " + barrier.isBroken());

    AtomicInteger arrivals = new AtomicInteger(0);
    Thread t1 = new Thread(() -> {
      try {
        arrivals.incrementAndGet();
        barrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread t2 = new Thread(() -> {
      try {
        arrivals.incrementAndGet();
        barrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    t1.start();
    t2.start();

    // Main thread is the 3rd party
    barrier.await();
    t1.join();
    t2.join();

    System.out.println("arrivals: " + arrivals.get());
    System.out.println("barrier action ran: " + (actionCount.get() == 1));

    // Test barrier reset
    barrier.reset();
    System.out.println("after reset isBroken: " + barrier.isBroken());
  }

  private static void testExchanger() throws Exception {
    Exchanger<String> exchanger = new Exchanger<>();
    String[] results = new String[2];

    Thread t1 = new Thread(() -> {
      try {
        results[0] = exchanger.exchange("alpha");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread t2 = new Thread(() -> {
      try {
        results[1] = exchanger.exchange("beta");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    t1.start();
    t2.start();
    t1.join();
    t2.join();

    System.out.println("t1 received: " + results[0]);
    System.out.println("t2 received: " + results[1]);
  }

  private static void testPhaser() {
    Phaser phaser = new Phaser(2);
    System.out.println("initial phase: " + phaser.getPhase());
    System.out.println("registered: " + phaser.getRegisteredParties());
    System.out.println("unarrived: " + phaser.getUnarrivedParties());

    phaser.register();
    System.out.println("after register: " + phaser.getRegisteredParties());

    phaser.arrive();
    System.out.println("arrived count: " + phaser.getArrivedParties());
    phaser.arrive();
    phaser.arrive(); // all 3 arrived -> phase advances to 1

    System.out.println("phase after advance: " + phaser.getPhase());

    phaser.arriveAndDeregister();
    System.out.println("registered after deregister: " + phaser.getRegisteredParties());

    phaser.forceTermination();
    System.out.println("isTerminated: " + phaser.isTerminated());
  }

  private static void testStampedLock() {
    StampedLock sl = new StampedLock();
    System.out.println("initial isWriteLocked: " + sl.isWriteLocked());
    System.out.println("initial isReadLocked: " + sl.isReadLocked());

    // Optimistic reading
    long optStamp = sl.tryOptimisticRead();
    System.out.println("optStamp != 0: " + (optStamp != 0L));
    System.out.println("optStamp isOptimisticReadStamp: " + StampedLock.isOptimisticReadStamp(optStamp));
    System.out.println("optStamp isLockStamp: " + StampedLock.isLockStamp(optStamp));
    System.out.println("validate initial: " + sl.validate(optStamp));

    // Write lock
    long writeStamp = sl.writeLock();
    System.out.println("isWriteLocked after writeLock: " + sl.isWriteLocked());
    System.out.println("writeStamp isWriteLockStamp: " + StampedLock.isWriteLockStamp(writeStamp));
    System.out.println("validate during write: " + sl.validate(optStamp));
    sl.unlockWrite(writeStamp);
    System.out.println("isWriteLocked after unlock: " + sl.isWriteLocked());

    // Read lock
    long readStamp = sl.readLock();
    System.out.println("isReadLocked after readLock: " + sl.isReadLocked());
    System.out.println("readLockCount: " + sl.getReadLockCount());
    System.out.println("readStamp isReadLockStamp: " + StampedLock.isReadLockStamp(readStamp));

    // Try convert read lock to write lock
    long convertedWrite = sl.tryConvertToWriteLock(readStamp);
    System.out.println("converted to write: " + (convertedWrite != 0L));
    System.out.println("isWriteLocked after conversion: " + sl.isWriteLocked());
    sl.unlockWrite(convertedWrite);

    // Lock views
    Lock rLock = sl.asReadLock();
    rLock.lock();
    System.out.println("view isReadLocked: " + sl.isReadLocked());
    rLock.unlock();
    System.out.println("view after read unlock: " + sl.isReadLocked());

    Lock wLock = sl.asWriteLock();
    wLock.lock();
    System.out.println("view isWriteLocked: " + sl.isWriteLocked());
    wLock.unlock();
    System.out.println("view after write unlock: " + sl.isWriteLocked());
  }
}
