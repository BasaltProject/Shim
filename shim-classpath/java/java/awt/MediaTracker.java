/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

public class MediaTracker implements java.io.Serializable {
  public static final int LOADING = 1;
  public static final int ABORTED = 2;
  public static final int ERRORED = 4;
  public static final int COMPLETE = 8;

  private static final class Entry {
    final Image image;
    final int id;
    final int width;
    final int height;

    Entry(Image image, int id, int width, int height) {
      this.image = image;
      this.id = id;
      this.width = width;
      this.height = height;
    }
  }

  Component target;
  private final List<Entry> entries = new ArrayList<Entry>();

  public MediaTracker(Component comp) { target = comp; }

  public void addImage(Image image, int id) { addImage(image, id, -1, -1); }

  public synchronized void addImage(Image image, int id, int w, int h) {
    entries.add(new Entry(image, id, w, h));
  }

  private int statusOf(Entry e, boolean load) {
    if (load) {
      Toolkit.getDefaultToolkit().prepareImage(e.image, e.width, e.height, null);
    }
    int f = Toolkit.getDefaultToolkit().checkImage(e.image, e.width, e.height, null);
    if ((f & ImageObserver.ALLBITS) != 0 || (f & ImageObserver.FRAMEBITS) != 0) {
      return COMPLETE;
    } else if ((f & ImageObserver.ERROR) != 0) {
      return ERRORED;
    } else if ((f & ImageObserver.ABORT) != 0) {
      return ABORTED;
    }
    return LOADING;
  }

  public boolean checkAll() { return checkAll(false); }

  public boolean checkAll(boolean load) { return (statusAll(load) & LOADING) == 0; }

  public synchronized boolean isErrorAny() { return (statusAll(false) & (ERRORED | ABORTED)) != 0; }

  public synchronized Object[] getErrorsAny() {
    List<Object> out = new ArrayList<Object>();
    for (Entry e : entries) {
      if ((statusOf(e, false) & (ERRORED | ABORTED)) != 0) {
        out.add(e.image);
      }
    }
    return out.isEmpty() ? null : out.toArray();
  }

  public void waitForAll() throws InterruptedException {
    while (!checkAll(true)) {
      Thread.sleep(10);
    }
  }

  public synchronized boolean waitForAll(long ms) throws InterruptedException {
    long end = System.currentTimeMillis() + ms;
    while (!checkAll(true)) {
      if (System.currentTimeMillis() >= end) {
        return false;
      }
      Thread.sleep(10);
    }
    return true;
  }

  public int statusAll(boolean load) {
    int status = 0;
    synchronized (this) {
      for (Entry e : entries) {
        status |= statusOf(e, load);
      }
    }
    return status;
  }

  public boolean checkID(int id) { return checkID(id, false); }

  public boolean checkID(int id, boolean load) { return (statusID(id, load) & LOADING) == 0; }

  public synchronized boolean isErrorID(int id) { return (statusID(id, false) & (ERRORED | ABORTED)) != 0; }

  public synchronized Object[] getErrorsID(int id) {
    List<Object> out = new ArrayList<Object>();
    for (Entry e : entries) {
      if (e.id == id && (statusOf(e, false) & (ERRORED | ABORTED)) != 0) {
        out.add(e.image);
      }
    }
    return out.isEmpty() ? null : out.toArray();
  }

  public void waitForID(int id) throws InterruptedException {
    while (!checkID(id, true)) {
      Thread.sleep(10);
    }
  }

  public synchronized boolean waitForID(int id, long ms) throws InterruptedException {
    long end = System.currentTimeMillis() + ms;
    while (!checkID(id, true)) {
      if (System.currentTimeMillis() >= end) {
        return false;
      }
      Thread.sleep(10);
    }
    return true;
  }

  public int statusID(int id, boolean load) {
    int status = 0;
    synchronized (this) {
      for (Entry e : entries) {
        if (e.id == id) {
          status |= statusOf(e, load);
        }
      }
    }
    return status;
  }

  public synchronized void removeImage(Image image) {
    for (int i = entries.size() - 1; i >= 0; i--) {
      if (entries.get(i).image == image) {
        entries.remove(i);
      }
    }
  }

  public synchronized void removeImage(Image image, int id) {
    for (int i = entries.size() - 1; i >= 0; i--) {
      Entry e = entries.get(i);
      if (e.image == image && e.id == id) {
        entries.remove(i);
      }
    }
  }

  public synchronized void removeImage(Image image, int id, int width, int height) {
    for (int i = entries.size() - 1; i >= 0; i--) {
      Entry e = entries.get(i);
      if (e.image == image && e.id == id && e.width == width && e.height == height) {
        entries.remove(i);
      }
    }
  }
}
