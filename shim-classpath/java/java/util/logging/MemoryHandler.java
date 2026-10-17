/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

public class MemoryHandler extends Handler {
  private static final int kDefaultSize = 1000;
  private volatile Level pushLevel;
  private int size;
  private Handler target;
  private LogRecord[] buffer;
  int start;
  int count;

  public MemoryHandler() {
    super(Level.ALL, new SimpleFormatter(), null);
    LogManager manager = LogManager.getLogManager();
    String cname = getClass().getName();
    pushLevel = manager.getLevelProperty(cname + ".push", Level.SEVERE);
    size = manager.getIntProperty(cname + ".size", kDefaultSize);
    if (size <= 0) {
      size = kDefaultSize;
    }
    String targetName = manager.getProperty(cname + ".target");
    if (targetName == null) {
      throw new RuntimeException("The handler " + cname + " does not specify a target");
    }
    Class<?> clz;
    try {
      clz = ClassLoader.getSystemClassLoader().loadClass(targetName);
      @SuppressWarnings("deprecation")
      Object o = clz.newInstance();
      target = (Handler) o;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("MemoryHandler can't load handler target \"" + targetName + "\"", e);
    }
    init();
  }

  private void init() {
    buffer = new LogRecord[size];
    start = 0;
    count = 0;
  }

  public MemoryHandler(Handler target, int size, Level pushLevel) {
    super(Level.ALL, new SimpleFormatter(), null);
    if (target == null || pushLevel == null) {
      throw new NullPointerException();
    }
    if (size <= 0) {
      throw new IllegalArgumentException();
    }
    this.target = target;
    this.pushLevel = pushLevel;
    this.size = size;
    init();
  }

  @Override
  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    int ix = (start + count) % buffer.length;
    buffer[ix] = record;
    if (count < buffer.length) {
      count++;
    } else {
      start++;
      start %= buffer.length;
    }
    if (record.getLevel().intValue() >= pushLevel.intValue()) {
      push();
    }
  }

  public synchronized void push() {
    for (int i = 0; i < count; i++) {
      int ix = (start + i) % buffer.length;
      LogRecord record = buffer[ix];
      target.publish(record);
    }
    start = 0;
    count = 0;
  }

  @Override
  public void flush() {
    target.flush();
  }

  @Override
  public void close() throws SecurityException {
    target.close();
    setLevel(Level.OFF);
  }

  public synchronized void setPushLevel(Level newLevel) throws SecurityException {
    if (newLevel == null) {
      throw new NullPointerException();
    }
    checkPermission();
    pushLevel = newLevel;
  }

  public Level getPushLevel() {
    return pushLevel;
  }

  @Override
  public boolean isLoggable(LogRecord record) {
    return super.isLoggable(record);
  }
}
