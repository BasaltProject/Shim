/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleExtendedText {
  int LINE = 4;
  int ATTRIBUTE_RUN = 5;
  java.lang.String getTextRange(int p0, int p1);
  javax.accessibility.AccessibleTextSequence getTextSequenceAt(int p0, int p1);
  javax.accessibility.AccessibleTextSequence getTextSequenceAfter(int p0, int p1);
  javax.accessibility.AccessibleTextSequence getTextSequenceBefore(int p0, int p1);
  java.awt.Rectangle getTextBounds(int p0, int p1);
}
