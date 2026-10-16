/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

import java.util.Map;

public interface MatchResult {
  int start();

  int start(int group);

  default int start(String name) {
    return start(Matcher.groupNumber(this, name));
  }

  int end();

  int end(int group);

  default int end(String name) {
    return end(Matcher.groupNumber(this, name));
  }

  String group();

  String group(int group);

  default String group(String name) {
    return group(Matcher.groupNumber(this, name));
  }

  int groupCount();

  default Map<String, Integer> namedGroups() {
    throw new UnsupportedOperationException("namedGroups()");
  }

  default boolean hasMatch() {
    throw new UnsupportedOperationException("hasMatch()");
  }
}
