/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

class SourceCodeAnalysisImpl {
  public static final class ClassIndex {
    public final long timestamp;
    public final Path forPath;
    public final Set<String> packages;
    public final Map<String, Set<String>> classSimpleName2FQN;

    public ClassIndex(long timestamp, Path forPath, Set<String> packages, Map<String, Set<String>> classSimpleName2FQN) {
      this.timestamp = timestamp;
      this.forPath = forPath;
      this.packages = packages;
      this.classSimpleName2FQN = classSimpleName2FQN;
    }
  }

  SourceCodeAnalysisImpl() {}
}
