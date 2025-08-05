/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface ProcessHandle extends Comparable<ProcessHandle> {
  long pid();

  Optional<ProcessHandle> parent();

  Stream<ProcessHandle> children();

  Stream<ProcessHandle> descendants();

  ProcessHandle.Info info();

  CompletableFuture<ProcessHandle> onExit();

  boolean supportsNormalTermination();

  boolean destroy();

  boolean destroyForcibly();

  boolean isAlive();

  @Override
  default int compareTo(ProcessHandle other) {
    return Long.compare(pid(), other.pid());
  }

  static Optional<ProcessHandle> of(long pid) {
    return ProcessHandleImpl.of(pid);
  }

  static ProcessHandle current() {
    return ProcessHandleImpl.current();
  }

  static Stream<ProcessHandle> allProcesses() {
    return ProcessHandleImpl.allProcesses();
  }

  interface Info {
    Optional<String> command();

    Optional<String> commandLine();

    Optional<String[]> arguments();

    Optional<Instant> startInstant();

    Optional<Duration> totalCpuDuration();

    Optional<String> user();
  }
}
