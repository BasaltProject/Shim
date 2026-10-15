/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Runtime {
  private static final Runtime instance = new Runtime();

  private static Version version;

  private Runtime() { }

  public static Runtime getRuntime() {
    return instance;
  }

  public static Version version() {
    if (version == null) {
      String prop = System.getProperty("java.runtime.version");
      if (prop == null || prop.isEmpty()) {
        prop = System.getProperty("java.version");
      }
      if (prop == null || prop.isEmpty()) {
        prop = "25";
      }
      version = Version.parse(prop);
    }
    return version;
  }

  public static final class Version implements Comparable<Version> {
    private static final Pattern VSTR_PATTERN = Pattern.compile(
        "([1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)(?:-([a-zA-Z0-9]+))?(?:(\\+)(0|[1-9][0-9]*)?)?(?:-([-a-zA-Z0-9.]+))?");

    private final List<Integer> version;
    private final Optional<String> pre;
    private final Optional<Integer> build;
    private final Optional<String> optional;

    private Version(List<Integer> version, Optional<String> pre, Optional<Integer> build, Optional<String> optional) {
      this.version = version;
      this.pre = pre;
      this.build = build;
      this.optional = optional;
    }

    public static Version parse(String s) {
      if (s == null) {
        throw new NullPointerException();
      }
      if (isSimpleNumber(s)) {
        int n = Integer.parseInt(s);
        return new Version(Collections.singletonList(n), Optional.empty(), Optional.empty(), Optional.empty());
      }
      Matcher m = VSTR_PATTERN.matcher(s);
      if (!m.matches()) {
        throw new IllegalArgumentException("Invalid version string: '" + s + "'");
      }
      String vnumGroup = m.group(1);
      String[] parts = vnumGroup.split("\\.");
      List<Integer> list = new ArrayList<>(parts.length);
      for (String part : parts) {
        list.add(Integer.parseInt(part));
      }
      Optional<String> pre = Optional.ofNullable(m.group(2));
      String plus = m.group(3);
      String buildStr = m.group(4);
      Optional<Integer> build = buildStr != null ? Optional.of(Integer.parseInt(buildStr)) : Optional.empty();
      Optional<String> optional = Optional.ofNullable(m.group(5));

      if (build.isEmpty()) {
        if (plus != null) {
          if (optional.isPresent()) {
            if (pre.isPresent()) {
              throw new IllegalArgumentException("'+' found with pre-release and optional components:'" + s + "'");
            }
          } else {
            throw new IllegalArgumentException("'+' found with neither build or optional components: '" + s + "'");
          }
        } else {
          if (optional.isPresent() && pre.isEmpty()) {
            throw new IllegalArgumentException("optional component must be preceded by a pre-release component or '+': '" + s + "'");
          }
        }
      }

      return new Version(Collections.unmodifiableList(list), pre, build, optional);
    }

    private static boolean isSimpleNumber(String s) {
      int len = s.length();
      if (len == 0) return false;
      char first = s.charAt(0);
      if (first < '1' || first > '9') return false;
      for (int i = 1; i < len; i++) {
        char c = s.charAt(i);
        if (c < '0' || c > '9') return false;
      }
      return true;
    }

    public int feature() {
      return version.get(0);
    }

    public int interim() {
      return version.size() > 1 ? version.get(1) : 0;
    }

    public int update() {
      return version.size() > 2 ? version.get(2) : 0;
    }

    public int patch() {
      return version.size() > 3 ? version.get(3) : 0;
    }

    public int major() {
      return feature();
    }

    public int minor() {
      return interim();
    }

    public int security() {
      return update();
    }

    public List<Integer> version() {
      return version;
    }

    public Optional<String> pre() {
      return pre;
    }

    public Optional<Integer> build() {
      return build;
    }

    public Optional<String> optional() {
      return optional;
    }

    @Override
    public int compareTo(Version obj) {
      return compare(obj, false);
    }

    public int compareToIgnoreOptional(Version obj) {
      return compare(obj, true);
    }

    private int compare(Version other, boolean ignoreOptional) {
      Objects.requireNonNull(other);
      int cmp = compareVersion(other);
      if (cmp != 0) return cmp;
      cmp = comparePre(other);
      if (cmp != 0) return cmp;
      cmp = compareBuild(other);
      if (cmp != 0) return cmp;
      if (!ignoreOptional) {
        return compareOptional(other);
      }
      return 0;
    }

    private int compareVersion(Version other) {
      int s1 = version.size();
      int s2 = other.version.size();
      int min = Math.min(s1, s2);
      for (int i = 0; i < min; i++) {
        int v1 = version.get(i);
        int v2 = other.version.get(i);
        if (v1 != v2) {
          return v1 - v2;
        }
      }
      return s1 - s2;
    }

    private int comparePre(Version other) {
      Optional<String> oPre = other.pre;
      if (pre.isEmpty()) {
        return oPre.isPresent() ? 1 : 0;
      }
      if (oPre.isEmpty()) {
        return -1;
      }
      String p1 = pre.get();
      String p2 = oPre.get();
      boolean d1 = isDigits(p1);
      boolean d2 = isDigits(p2);
      if (d1) {
        if (d2) {
          return compareNumericStrings(p1, p2);
        }
        return -1;
      }
      if (d2) {
        return 1;
      }
      return p1.compareTo(p2);
    }

    private static boolean isDigits(String s) {
      if (s.isEmpty()) return false;
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c < '0' || c > '9') return false;
      }
      return true;
    }

    private static int compareNumericStrings(String s1, String s2) {
      int i1 = 0;
      while (i1 < s1.length() - 1 && s1.charAt(i1) == '0') i1++;
      int i2 = 0;
      while (i2 < s2.length() - 1 && s2.charAt(i2) == '0') i2++;
      int len1 = s1.length() - i1;
      int len2 = s2.length() - i2;
      if (len1 != len2) {
        return len1 - len2;
      }
      while (i1 < s1.length()) {
        char c1 = s1.charAt(i1++);
        char c2 = s2.charAt(i2++);
        if (c1 != c2) {
          return c1 - c2;
        }
      }
      return 0;
    }

    private int compareBuild(Version other) {
      Optional<Integer> oBuild = other.build;
      if (build.isPresent()) {
        if (oBuild.isPresent()) {
          return build.get().compareTo(oBuild.get());
        }
        return 1;
      }
      return oBuild.isPresent() ? -1 : 0;
    }

    private int compareOptional(Version other) {
      Optional<String> oOpt = other.optional;
      if (optional.isEmpty()) {
        return oOpt.isPresent() ? -1 : 0;
      }
      if (oOpt.isEmpty()) {
        return 1;
      }
      return optional.get().compareTo(oOpt.get());
    }

    @Override
    public boolean equals(Object obj) {
      if (!equalsIgnoreOptional(obj)) {
        return false;
      }
      Version other = (Version) obj;
      return optional.equals(other.optional);
    }

    public boolean equalsIgnoreOptional(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Version)) {
        return false;
      }
      Version other = (Version) obj;
      return version.equals(other.version)
          && pre.equals(other.pre)
          && build.equals(other.build);
    }

    @Override
    public int hashCode() {
      int h = 1;
      int prime = 17;
      h = prime * h + version.hashCode();
      h = prime * h + pre.hashCode();
      h = prime * h + build.hashCode();
      h = prime * h + optional.hashCode();
      return h;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < version.size(); i++) {
        if (i > 0) sb.append('.');
        sb.append(version.get(i));
      }
      if (pre.isPresent()) {
        sb.append('-').append(pre.get());
      }
      if (build.isPresent()) {
        sb.append('+').append(build.get());
        if (optional.isPresent()) {
          sb.append('-').append(optional.get());
        }
      } else if (optional.isPresent()) {
        if (pre.isPresent()) {
          sb.append('-');
        } else {
          sb.append("+-");
        }
        sb.append(optional.get());
      }
      return sb.toString();
    }
  }

  public void load(String path) {
    if (path != null) {
      ClassLoader.load(path, ClassLoader.getCaller(), false);
    } else {
      throw new NullPointerException();
    }
  }

  public void loadLibrary(String path) {
    if (path != null) {
      ClassLoader.load(path, ClassLoader.getCaller(), true);
    } else {
      throw new NullPointerException();
    }
  }

  public Process exec(String command) throws IOException {
    StringTokenizer t = new StringTokenizer(command);
    String[] cmd = new String[t.countTokens()];
    for (int i = 0; i < cmd.length; i++)
      cmd[i] = t.nextToken();
    
    return exec(cmd);
  }

  public Process exec(final String[] command) throws IOException {
    final MyProcess[] process = new MyProcess[1];
    final Throwable[] exception = new Throwable[1];

    synchronized (process) {
      Thread t = new Thread() {
          public void run() {
            synchronized (process) {
              try {
                long[] info = new long[5];
                exec(command, info);
                process[0] = new MyProcess
                  (info[0], info[1], (int) info[2], (int) info[3],
                   (int) info[4]);
              } catch (Throwable e) {
                exception[0] = e;
              } finally {          
                process.notifyAll();
              }
            }

            MyProcess p = process[0];
            if (p != null) {
              synchronized (p) {
                try {
                  if (p.pid != 0) {
                    p.exitCode = Runtime.waitFor(p.pid, p.tid);
                    p.pid = 0;
                    p.tid = 0;
                  }
                } finally {
                  p.notifyAll();
                }
              }
            }
          }
        };
      t.setDaemon(true);
      t.start();

      while (process[0] == null && exception[0] == null) {
        try {
          process.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    if (exception[0] != null) {
      if (exception[0] instanceof IOException) {
        String message = "Failed to run \"" + command[0] + "\": " + exception[0].getMessage();
        throw new IOException(message);
      } else {
        throw new RuntimeException(exception[0]);
      }
    }

    return process[0];
  }

  public native void addShutdownHook(Thread t);

  private static native void exec(String[] command, long[] process)
    throws IOException;

  private static native int waitFor(long pid, long tid);

  private static native void kill(long pid);

  public native void gc();

  public native void exit(int code);

  public native long freeMemory();

  public native long totalMemory();
  
  public native long maxMemory();

  public int availableProcessors() {
    return 4;
  }

  private static class MyProcess extends Process {
    private long pid;
    private long tid;
    private final int in;
    private final int out;
    private final int err;
    private int exitCode;

    public MyProcess(long pid, long tid, int in, int out, int err) {
      this.pid = pid;
      this.tid = tid;
      this.in = in;
      this.out = out;
      this.err = err;
    }

    public void destroy() {
			if (pid != 0) {
      	kill(pid);
			}
    }

    @Override
    public long pid() {
      return pid;
    }

    @Override
    public ProcessHandle toHandle() {
      return ProcessHandle.of(pid).orElseGet(() -> new ProcessHandleImpl(pid));
    }

    @Override
    public boolean isAlive() {
      return pid != 0;
    }

    @Override
    public Process destroyForcibly() {
      destroy();
      return this;
    }

    @Override
    public boolean supportsNormalTermination() {
      return true;
    }

    public InputStream getInputStream() {
      return new FileInputStream(new FileDescriptor(in));
    }

    public OutputStream getOutputStream() {
      return new FileOutputStream(new FileDescriptor(out));
    }

    public InputStream getErrorStream() {
      return new FileInputStream(new FileDescriptor(err));
    }

    public synchronized int exitValue() {
      if (pid != 0) {
        throw new IllegalThreadStateException();
      }

      return exitCode;
    }

    public synchronized int waitFor() throws InterruptedException {
      while (pid != 0) {
        wait();
      }

      return exitCode;
    }
  }
}
