/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class Timer {
  private static int serialNumber;

  private final PriorityQueue<TimerTask> queue = new PriorityQueue<TimerTask>(11, new Comparator<TimerTask>() {
    public int compare(TimerTask a, TimerTask b) {
      return Long.compare(a.nextExecutionTime, b.nextExecutionTime);
    }
  });

  private boolean newTasksMayBeScheduled = true;

  private final Thread thread;

  private static synchronized int nextSerialNumber() {
    return serialNumber++;
  }

  public Timer() {
    this("Timer-" + nextSerialNumber());
  }

  public Timer(boolean isDaemon) {
    this("Timer-" + nextSerialNumber(), isDaemon);
  }

  public Timer(String name) {
    this(name, false);
  }

  public Timer(String name, boolean isDaemon) {
    thread = new Thread(new Runnable() {
      public void run() {
        try {
          mainLoop();
        } finally {
          synchronized (queue) {
            newTasksMayBeScheduled = false;
            queue.clear();
          }
        }
      }
    }, name);
    thread.setDaemon(isDaemon);
    thread.start();
  }

  private void mainLoop() {
    while (true) {
      try {
        TimerTask task;
        boolean taskFired;
        synchronized (queue) {
          while (queue.isEmpty() && newTasksMayBeScheduled) {
            queue.wait();
          }
          if (queue.isEmpty()) {
            break;
          }

          long currentTime;
          long executionTime;
          task = queue.peek();
          synchronized (task.lock) {
            if (task.state == TimerTask.CANCELLED) {
              queue.poll();
              continue;
            }
            currentTime = System.currentTimeMillis();
            executionTime = task.nextExecutionTime;
            taskFired = executionTime <= currentTime;
            if (taskFired) {
              queue.poll();
              if (task.period == 0) {
                task.state = TimerTask.EXECUTED;
              } else {
                task.nextExecutionTime = task.period < 0 ? currentTime - task.period : executionTime + task.period;
                queue.add(task);
              }
            }
          }
          if (!taskFired) {
            queue.wait(executionTime - currentTime);
          }
        }
        if (taskFired) {
          task.run();
        }
      } catch (InterruptedException e) {
      }
    }
  }

  public void schedule(TimerTask task, long delay) {
    if (delay < 0) {
      throw new IllegalArgumentException("Negative delay.");
    }
    sched(task, System.currentTimeMillis() + delay, 0);
  }

  public void schedule(TimerTask task, Date time) {
    sched(task, time.getTime(), 0);
  }

  public void schedule(TimerTask task, long delay, long period) {
    if (delay < 0) {
      throw new IllegalArgumentException("Negative delay.");
    }
    if (period <= 0) {
      throw new IllegalArgumentException("Non-positive period.");
    }
    sched(task, System.currentTimeMillis() + delay, -period);
  }

  public void schedule(TimerTask task, Date firstTime, long period) {
    if (period <= 0) {
      throw new IllegalArgumentException("Non-positive period.");
    }
    sched(task, firstTime.getTime(), -period);
  }

  public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
    if (delay < 0) {
      throw new IllegalArgumentException("Negative delay.");
    }
    if (period <= 0) {
      throw new IllegalArgumentException("Non-positive period.");
    }
    sched(task, System.currentTimeMillis() + delay, period);
  }

  public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
    if (period <= 0) {
      throw new IllegalArgumentException("Non-positive period.");
    }
    sched(task, firstTime.getTime(), period);
  }

  private void sched(TimerTask task, long time, long period) {
    if (time < 0) {
      throw new IllegalArgumentException("Illegal execution time.");
    }
    if (Math.abs(period) > (Long.MAX_VALUE >> 1)) {
      period >>= 1;
    }
    synchronized (queue) {
      if (!newTasksMayBeScheduled) {
        throw new IllegalStateException("Timer already cancelled.");
      }
      synchronized (task.lock) {
        if (task.state != TimerTask.VIRGIN) {
          throw new IllegalStateException("Task already scheduled or cancelled");
        }
        task.nextExecutionTime = time;
        task.period = period;
        task.state = TimerTask.SCHEDULED;
      }
      queue.add(task);
      queue.notify();
    }
  }

  public void cancel() {
    synchronized (queue) {
      newTasksMayBeScheduled = false;
      queue.clear();
      queue.notify();
    }
  }

  public int purge() {
    int result = 0;
    synchronized (queue) {
      Iterator<TimerTask> iterator = queue.iterator();
      while (iterator.hasNext()) {
        TimerTask task = iterator.next();
        if (task.state == TimerTask.CANCELLED) {
          iterator.remove();
          ++result;
        }
      }
    }
    return result;
  }
}
