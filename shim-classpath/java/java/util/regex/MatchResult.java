/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

public interface MatchResult {
  int start();

  int start(int group);

  int end();

  int end(int group);

  String group();

  String group(int group);

  int groupCount();
}
