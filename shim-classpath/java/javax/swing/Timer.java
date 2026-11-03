/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.event.*;
import java.util.*;

public class Timer implements java.io.Serializable {
  private final java.util.concurrent.CopyOnWriteArrayList<ActionListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private int initialDelay;
  private int delay;
  private boolean repeats = true;
  private boolean coalesce = true;
  private String actionCommand;
  private java.util.Timer scheduler;
  private java.util.TimerTask task;
  private boolean running;
  private static boolean logTimers;
  private final Runnable doPostEvent = new Runnable() {
    public void run() {
      fireActionPerformed(new ActionEvent(Timer.this, 0, getActionCommand(), System.currentTimeMillis(), 0));
      if (!repeats) {
        stop();
      }
    }
  };
  protected javax.swing.event.EventListenerList listenerList;
  public Timer(int p0, java.awt.event.ActionListener p1) {
    if (p0 < 0) {
      throw new IllegalArgumentException("Invalid delay: " + p0);
    }
    this.delay = p0;
    this.initialDelay = p0;
    if (p1 != null) {
      addActionListener(p1);
    }
  }
  public void start() {
    synchronized (this) {
      if (running) {
        return;
      }
      running = true;
      scheduler = new java.util.Timer("Swing-Timer", true);
      task = new java.util.TimerTask() {
        public void run() {
          java.awt.EventQueue.invokeLater(doPostEvent);
        }
      };
      if (repeats) {
        scheduler.schedule(task, initialDelay, Math.max(1, delay));
      } else {
        scheduler.schedule(task, initialDelay);
      }
    }
  }
  public void stop() {
    synchronized (this) {
      running = false;
      if (task != null) {
        task.cancel();
        task = null;
      }
      if (scheduler != null) {
        scheduler.cancel();
        scheduler = null;
      }
    }
  }
  public void setRepeats(boolean p0) {
    repeats = p0;
  }
  public int getInitialDelay() {
    return initialDelay;
  }
  public static void setLogTimers(boolean p0) {
    logTimers = p0;
  }
  public static boolean getLogTimers() {
    return logTimers;
  }
  public boolean isRepeats() {
    return repeats;
  }
  public boolean isCoalesce() {
    return coalesce;
  }
  public void restart() {
    stop();
    start();
  }
  public void setCoalesce(boolean p0) {
    coalesce = p0;
  }
  public void setInitialDelay(int p0) {
    if (p0 < 0) {
      throw new IllegalArgumentException("Invalid initial delay: " + p0);
    }
    initialDelay = p0;
  }
  public boolean isRunning() {
    return running;
  }
  protected void fireActionPerformed(java.awt.event.ActionEvent p0) {
    for (ActionListener l : listeners.toArray(new ActionListener[0])) {
      l.actionPerformed(p0);
    }
  }
  public int getDelay() {
    return delay;
  }
  public void setDelay(int p0) {
    if (p0 < 0) {
      throw new IllegalArgumentException("Invalid delay: " + p0);
    }
    delay = p0;
  }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) {
    ArrayList<EventListener> l = new ArrayList<>();
    if (p0 == ActionListener.class) {
      l.addAll(listeners);
    }
    T[] result = (T[]) java.lang.reflect.Array.newInstance(p0, l.size());
    for (int i = 0; i < result.length; i++) {
      result[i] = (T) l.get(i);
    }
    return result;
  }
  public java.lang.String getActionCommand() {
    return actionCommand;
  }
  public void addActionListener(java.awt.event.ActionListener p0) {
    if (p0 != null) {
      listeners.add(p0);
    }
  }
  public void setActionCommand(java.lang.String p0) {
    actionCommand = p0;
  }
  public void removeActionListener(java.awt.event.ActionListener p0) {
    listeners.remove(p0);
  }
  public java.awt.event.ActionListener[] getActionListeners() {
    return listeners.toArray(new ActionListener[0]);
  }
}
