/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class FileHandler extends StreamHandler {
  private MeteredStream meter;
  private boolean append;
  private long limit;
  private int count;
  private String pattern;
  private String lockFileName;
  private File[] files;
  private static final int kMaxLocks = 100;
  private int maxLocks = kMaxLocks;
  private static final Set<String> kLocks = new HashSet<String>();

  private static final class MeteredStream extends OutputStream {
    final OutputStream out;
    long written;

    MeteredStream(OutputStream out, long written) {
      this.out = out;
      this.written = written;
    }

    @Override
    public void write(int b) throws IOException {
      out.write(b);
      written++;
    }

    @Override
    public void write(byte[] buff) throws IOException {
      out.write(buff);
      written += buff.length;
    }

    @Override
    public void write(byte[] buff, int off, int len) throws IOException {
      out.write(buff, off, len);
      written += len;
    }

    @Override
    public void flush() throws IOException {
      out.flush();
    }

    @Override
    public void close() throws IOException {
      out.close();
    }
  }

  private void open(File fname, boolean append) throws IOException {
    long len = 0;
    if (append) {
      len = fname.length();
    }
    FileOutputStream fout = new FileOutputStream(fname.toString(), append);
    BufferedOutputStream bout = new BufferedOutputStream(fout);
    meter = new MeteredStream(bout, len);
    setOutputStream(meter);
  }

  private void configure() {
    LogManager manager = LogManager.getLogManager();
    String cname = getClass().getName();
    pattern = manager.getStringProperty(cname + ".pattern", "%h/java%u.log");
    limit = manager.getLongProperty(cname + ".limit", 0);
    if (limit < 0) {
      limit = 0;
    }
    count = manager.getIntProperty(cname + ".count", 1);
    if (count <= 0) {
      count = 1;
    }
    append = manager.getBooleanProperty(cname + ".append", false);
    setLevel(manager.getLevelProperty(cname + ".level", Level.ALL));
    setFilter(manager.getFilterProperty(cname + ".filter", null));
    setFormatter(manager.getFormatterProperty(cname + ".formatter", new XMLFormatter()));
    try {
      setEncoding(manager.getStringProperty(cname + ".encoding", null));
    } catch (Exception ex) {
      try {
        setEncoding(null);
      } catch (Exception ex2) {
        // doing a setEncoding with null should always work
      }
    }
    String ml = manager.getProperty(cname + ".maxLocks");
    if (ml != null) {
      try {
        maxLocks = Integer.parseInt(ml);
      } catch (NumberFormatException ex) {
        maxLocks = kMaxLocks;
      }
      if (maxLocks <= 0) {
        maxLocks = kMaxLocks;
      }
    }
  }

  public FileHandler() throws IOException, SecurityException {
    configure();
    openFiles();
  }

  public FileHandler(String pattern) throws IOException, SecurityException {
    if (pattern.length() < 1) {
      throw new IllegalArgumentException();
    }
    configure();
    this.pattern = pattern;
    this.limit = 0;
    this.count = 1;
    openFiles();
  }

  public FileHandler(String pattern, boolean append) throws IOException, SecurityException {
    if (pattern.length() < 1) {
      throw new IllegalArgumentException();
    }
    configure();
    this.pattern = pattern;
    this.limit = 0;
    this.count = 1;
    this.append = append;
    openFiles();
  }

  public FileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
    if (limit < 0 || count < 1 || pattern.length() < 1) {
      throw new IllegalArgumentException();
    }
    configure();
    this.pattern = pattern;
    this.limit = limit;
    this.count = count;
    openFiles();
  }

  public FileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
    this(pattern, (long) limit, count, append);
  }

  public FileHandler(String pattern, long limit, int count, boolean append) throws IOException, SecurityException {
    if (limit < 0 || count < 1 || pattern.length() < 1) {
      throw new IllegalArgumentException();
    }
    configure();
    this.pattern = pattern;
    this.limit = limit;
    this.count = count;
    this.append = append;
    openFiles();
  }

  private void openFiles() throws IOException {
    LogManager manager = LogManager.getLogManager();
    manager.checkPermission();
    if (count < 1) {
      throw new IllegalArgumentException("file count = " + count);
    }
    if (limit < 0) {
      limit = 0;
    }
    InitializationErrorManager em = new InitializationErrorManager();
    setErrorManager(em);
    int unique = -1;
    for (;;) {
      unique++;
      if (unique > maxLocks) {
        throw new IOException("Couldn't get lock for " + pattern + ", maxLocks: " + maxLocks);
      }
      lockFileName = generate(pattern, 0, unique).toString() + ".lck";
      synchronized (kLocks) {
        if (kLocks.contains(lockFileName)) {
          continue;
        }
        Path lockFilePath = Paths.get(lockFileName);
        try {
          Files.createFile(lockFilePath);
        } catch (FileAlreadyExistsException ix) {
          continue;
        } catch (IOException ix) {
          throw ix;
        }
        kLocks.add(lockFileName);
        break;
      }
    }
    files = new File[count];
    for (int i = 0; i < count; i++) {
      files[i] = generate(pattern, i, unique);
    }
    if (append) {
      open(files[0], true);
    } else {
      rotate();
    }
    Exception ex = em.lastException;
    if (ex != null) {
      if (ex instanceof IOException) {
        throw (IOException) ex;
      } else if (ex instanceof SecurityException) {
        throw (SecurityException) ex;
      } else {
        throw new IOException("Exception: " + ex);
      }
    }
    setErrorManager(new ErrorManager());
  }

  private File generate(String pattern, int generation, int unique) throws IOException {
    File file = null;
    String word = "";
    int ix = 0;
    boolean sawg = false;
    boolean sawu = false;
    while (ix < pattern.length()) {
      char ch = pattern.charAt(ix);
      ix++;
      char ch2 = 0;
      if (ix < pattern.length()) {
        ch2 = Character.toLowerCase(pattern.charAt(ix));
      }
      if (ch == '/') {
        if (file == null) {
          file = new File(word);
        } else {
          file = new File(file, word);
        }
        word = "";
        continue;
      } else if (ch == '%') {
        if (ch2 == 't') {
          String tmpDir = System.getProperty("java.io.tmpdir");
          if (tmpDir == null) {
            tmpDir = System.getProperty("user.home");
          }
          file = new File(tmpDir);
          ix++;
          word = "";
          continue;
        } else if (ch2 == 'h') {
          file = new File(System.getProperty("user.home"));
          ix++;
          word = "";
          continue;
        } else if (ch2 == 'g') {
          word = word + generation;
          sawg = true;
          ix++;
          continue;
        } else if (ch2 == 'u') {
          word = word + unique;
          sawu = true;
          ix++;
          continue;
        } else if (ch2 == '%') {
          word = word + "%";
          ix++;
          continue;
        }
      }
      word = word + ch;
    }
    if (count > 1 && !sawg) {
      word = word + "." + generation;
    }
    if (unique > 0 && !sawu) {
      word = word + "." + unique;
    }
    if (!word.isEmpty()) {
      if (file == null) {
        file = new File(word);
      } else {
        file = new File(file, word);
      }
    }
    return file;
  }

  private synchronized void rotate() {
    Level oldLevel = getLevel();
    setLevel(Level.OFF);
    super.close();
    for (int i = count - 2; i >= 0; i--) {
      File f1 = files[i];
      File f2 = files[i + 1];
      if (f1.exists()) {
        if (f2.exists()) {
          f2.delete();
        }
        f1.renameTo(f2);
      }
    }
    try {
      open(files[0], false);
    } catch (IOException ix) {
      reportError(null, ix, ErrorManager.OPEN_FAILURE);
    }
    setLevel(oldLevel);
  }

  @Override
  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    super.publish(record);
    flush();
    if (limit > 0 && (meter.written >= limit || meter.written < 0)) {
      rotate();
    }
  }

  @Override
  public synchronized void close() throws SecurityException {
    super.close();
    if (lockFileName == null) {
      return;
    }
    try {
      Files.deleteIfExists(Paths.get(lockFileName));
    } catch (IOException ex) {
      // the lock file may already be gone
    }
    synchronized (kLocks) {
      kLocks.remove(lockFileName);
    }
    lockFileName = null;
  }

  private static final class InitializationErrorManager extends ErrorManager {
    Exception lastException;

    @Override
    public void error(String msg, Exception ex, int code) {
      lastException = ex;
    }
  }
}
