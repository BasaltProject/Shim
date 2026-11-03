/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im;

public interface InputMethodRequests {
  java.awt.Rectangle getTextLocation(java.awt.font.TextHitInfo p0);
  java.awt.font.TextHitInfo getLocationOffset(int p0, int p1);
  int getInsertPositionOffset();
  java.text.AttributedCharacterIterator getCommittedText(int p0, int p1, java.text.AttributedCharacterIterator.Attribute[] p2);
  int getCommittedTextLength();
  java.text.AttributedCharacterIterator cancelLatestCommittedText(java.text.AttributedCharacterIterator.Attribute[] p0);
  java.text.AttributedCharacterIterator getSelectedText(java.text.AttributedCharacterIterator.Attribute[] p0);
}
