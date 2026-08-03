/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Component;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;

public class ObjectView extends ComponentView {
  public ObjectView(Element elem) {
    super(elem);
  }

  protected Component createComponent() { return null; }
}
