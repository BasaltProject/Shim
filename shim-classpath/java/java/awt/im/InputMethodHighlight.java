/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im;

import java.awt.font.TextAttribute;
import java.util.Map;

public class InputMethodHighlight {
  public static final int RAW_TEXT = 0;
  public static final int CONVERTED_TEXT = 1;

  public static final InputMethodHighlight UNSELECTED_RAW_TEXT_HIGHLIGHT = new InputMethodHighlight(false, RAW_TEXT);
  public static final InputMethodHighlight SELECTED_RAW_TEXT_HIGHLIGHT = new InputMethodHighlight(true, RAW_TEXT);
  public static final InputMethodHighlight UNSELECTED_CONVERTED_TEXT_HIGHLIGHT = new InputMethodHighlight(false,
      CONVERTED_TEXT);
  public static final InputMethodHighlight SELECTED_CONVERTED_TEXT_HIGHLIGHT = new InputMethodHighlight(true,
      CONVERTED_TEXT);

  private boolean selected;
  private int state;
  private int variation;
  private Map<TextAttribute, ?> style;

  public InputMethodHighlight(boolean selected, int state) { this(selected, state, 0, null); }

  public InputMethodHighlight(boolean selected, int state, int variation) { this(selected, state, variation, null); }

  public InputMethodHighlight(boolean selected, int state, int variation, Map<TextAttribute, ?> style) {
    this.selected = selected;
    if (!(state == RAW_TEXT || state == CONVERTED_TEXT)) {
      throw new IllegalArgumentException("unknown input method highlight state");
    }
    this.state = state;
    this.variation = variation;
    this.style = style;
  }

  public boolean isSelected() { return selected; }

  public int getState() { return state; }

  public int getVariation() { return variation; }

  public Map<TextAttribute, ?> getStyle() { return style; }
}
