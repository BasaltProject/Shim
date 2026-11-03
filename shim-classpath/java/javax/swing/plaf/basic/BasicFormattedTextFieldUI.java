/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class BasicFormattedTextFieldUI extends BasicTextFieldUI {
  public BasicFormattedTextFieldUI() { }

  public static ComponentUI createUI(JComponent c) { return new BasicFormattedTextFieldUI(); }

  protected String getPropertyPrefix() { return "FormattedTextField"; }
}
