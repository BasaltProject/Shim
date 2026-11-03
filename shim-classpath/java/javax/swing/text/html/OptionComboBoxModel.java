/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.io.Serializable;
import javax.swing.DefaultComboBoxModel;

class OptionComboBoxModel<E> extends DefaultComboBoxModel<E> implements Serializable {
  private Option selectedOption = null;

  public void setInitialSelection(Option option) { selectedOption = option; }

  public Option getInitialSelection() { return selectedOption; }
}
