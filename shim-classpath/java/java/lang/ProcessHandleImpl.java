/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

final class ProcessHandleImpl implements ProcessHandle {
  private final long pid;

  private static final ProcessHandle CURRENT = new ProcessHandleImpl(initCurrentPid());

  ProcessHandleImpl(long pid) {
    this.pid = pid;
  }

  static native long getCurrentPid0();

  static native boolean isAlive0(long pid);

  static native boolean destroy0(long pid, boolean forcibly);

  private static long initCurrentPid() {
    try {
      return getCurrentPid0();
    } catch (UnsatisfiedLinkError e) {
      File selfStat = new File("/proc/self/stat");
      if (selfStat.exists()) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(selfStat), StandardCharsets.UTF_8))) {
          String line = reader.readLine();
          if (line != null) {
            String[] tokens = line.split("\\s+");
            if (tokens.length > 0) {
              return Long.parseLong(tokens[0]);
            }
          }
        } catch (Throwable ignored) {
        }
      }
      return 1;
    }
  }

  static ProcessHandle current() {
    return CURRENT;
  }

  static Optional<ProcessHandle> of(long pid) {
    if (pid <= 0) {
      return Optional.empty();
    }
    ProcessHandle ph = new ProcessHandleImpl(pid);
    if (ph.isAlive()) {
      return Optional.of(ph);
    }
    return Optional.empty();
  }

  static Stream<ProcessHandle> allProcesses() {
    File proc = new File("/proc");
    if (!proc.isDirectory()) {
      return Stream.of(current());
    }
    String[] entries = proc.list();
    if (entries == null) {
      return Stream.of(current());
    }
    List<ProcessHandle> list = new ArrayList<ProcessHandle>();
    for (String entry : entries) {
      try {
        long p = Long.parseLong(entry);
        list.add(new ProcessHandleImpl(p));
      } catch (NumberFormatException ignored) {
      }
    }
    return list.stream();
  }

  @Override
  public long pid() {
    return pid;
  }

  @Override
  public Optional<ProcessHandle> parent() {
    File statFile = new File("/proc/" + pid + "/stat");
    if (!statFile.exists()) {
      return Optional.empty();
    }
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(statFile), StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      if (line != null) {
        int closeParen = line.lastIndexOf(')');
        if (closeParen != -1 && closeParen + 2 < line.length()) {
          String rest = line.substring(closeParen + 2);
          String[] tokens = rest.split("\\s+");
          if (tokens.length >= 2) {
            long ppid = Long.parseLong(tokens[1]);
            if (ppid > 0) {
              return Optional.of(new ProcessHandleImpl(ppid));
            }
          }
        }
      }
    } catch (Throwable ignored) {
    }
    return Optional.empty();
  }

  @Override
  public Stream<ProcessHandle> children() {
    long myPid = this.pid;
    return allProcesses().filter(ph -> {
      Optional<ProcessHandle> parent = ph.parent();
      return parent.isPresent() && parent.get().pid() == myPid;
    });
  }

  @Override
  public Stream<ProcessHandle> descendants() {
    List<ProcessHandle> result = new ArrayList<ProcessHandle>();
    collectDescendants(this, result);
    return result.stream();
  }

  private static void collectDescendants(ProcessHandle ph, List<ProcessHandle> result) {
    ph.children().forEach(child -> {
      result.add(child);
      collectDescendants(child, result);
    });
  }

  @Override
  public ProcessHandle.Info info() {
    return new InfoImpl(pid);
  }

  @Override
  public CompletableFuture<ProcessHandle> onExit() {
    return CompletableFuture.supplyAsync(() -> {
      while (isAlive()) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      return this;
    });
  }

  @Override
  public boolean supportsNormalTermination() {
    return true;
  }

  @Override
  public boolean destroy() {
    try {
      return destroy0(pid, false);
    } catch (UnsatisfiedLinkError e) {
      return false;
    }
  }

  @Override
  public boolean destroyForcibly() {
    try {
      return destroy0(pid, true);
    } catch (UnsatisfiedLinkError e) {
      return false;
    }
  }

  @Override
  public boolean isAlive() {
    try {
      return isAlive0(pid);
    } catch (UnsatisfiedLinkError e) {
      File proc = new File("/proc/" + pid);
      return proc.exists();
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other instanceof ProcessHandle) {
      return pid == ((ProcessHandle) other).pid();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(pid);
  }

  @Override
  public String toString() {
    return Long.toString(pid);
  }

  private static final class InfoImpl implements ProcessHandle.Info {
    private final long pid;

    InfoImpl(long pid) {
      this.pid = pid;
    }

    @Override
    public Optional<String> command() {
      File exe = new File("/proc/" + pid + "/exe");
      if (exe.exists()) {
        try {
          String canonical = exe.getCanonicalPath();
          if (canonical != null && !canonical.isEmpty()) {
            return Optional.of(canonical);
          }
        } catch (Throwable ignored) {
        }
      }
      File comm = new File("/proc/" + pid + "/comm");
      if (comm.exists()) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(comm), StandardCharsets.UTF_8))) {
          String s = reader.readLine();
          if (s != null && !s.isEmpty()) {
            return Optional.of(s.trim());
          }
        } catch (Throwable ignored) {
        }
      }
      return Optional.empty();
    }

    @Override
    public Optional<String> commandLine() {
      File cmdline = new File("/proc/" + pid + "/cmdline");
      if (cmdline.exists()) {
        try (FileInputStream in = new FileInputStream(cmdline)) {
          byte[] b = new byte[4096];
          int n = in.read(b);
          if (n > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
              if (b[i] == 0) {
                if (i + 1 < n && b[i + 1] != 0) {
                  sb.append(' ');
                }
              } else {
                sb.append((char) b[i]);
              }
            }
            String s = sb.toString().trim();
            if (!s.isEmpty()) {
              return Optional.of(s);
            }
          }
        } catch (Throwable ignored) {
        }
      }
      return Optional.empty();
    }

    @Override
    public Optional<String[]> arguments() {
      File cmdline = new File("/proc/" + pid + "/cmdline");
      if (cmdline.exists()) {
        try (FileInputStream in = new FileInputStream(cmdline)) {
          byte[] b = new byte[8192];
          int n = in.read(b);
          if (n > 0) {
            List<String> args = new ArrayList<String>();
            int start = -1;
            boolean first = true;
            for (int i = 0; i < n; i++) {
              if (b[i] == 0) {
                if (start != -1) {
                  if (first) {
                    first = false;
                  } else {
                    args.add(new String(b, start, i - start, StandardCharsets.UTF_8));
                  }
                  start = -1;
                }
              } else {
                if (start == -1) start = i;
              }
            }
            return Optional.of(args.toArray(new String[args.size()]));
          }
        } catch (Throwable ignored) {
        }
      }
      return Optional.empty();
    }

    @Override
    public Optional<Instant> startInstant() {
      return Optional.empty();
    }

    @Override
    public Optional<Duration> totalCpuDuration() {
      return Optional.empty();
    }

    @Override
    public Optional<String> user() {
      return Optional.empty();
    }
  }
}
