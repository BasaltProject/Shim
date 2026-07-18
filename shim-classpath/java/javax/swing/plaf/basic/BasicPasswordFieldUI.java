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
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.View;

public class BasicPasswordFieldUI extends BasicTextFieldUI {
  public BasicPasswordFieldUI() { }

  public static ComponentUI createUI(JComponent c) { return new BasicPasswordFieldUI(); }

  protected String getPropertyPrefix() { return "PasswordField"; }

  protected void installDefaults() {
    super.installDefaults();
    String prefix = getPropertyPrefix();
    Character echoChar = (Character) javax.swing.UIManager.get(prefix + ".echoChar");
    if (echoChar != null) {
      javax.swing.LookAndFeel.installProperty(getComponent(), "echoChar", echoChar);
    }
  }

  public View create(Element elem) { return new PasswordView(elem); }
}
