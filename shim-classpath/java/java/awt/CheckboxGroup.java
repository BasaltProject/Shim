/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.io.Serializable;

public class CheckboxGroup implements Serializable {
  Checkbox selectedCheckbox = null;

  public CheckboxGroup() { }

  public Checkbox getSelectedCheckbox() { return getCurrent(); }

  public Checkbox getCurrent() { return selectedCheckbox; }

  public void setSelectedCheckbox(Checkbox box) { setCurrent(box); }

  public void setCurrent(Checkbox box) { selectedCheckbox = box; }

  public String toString() { return getClass().getName() + "[selectedCheckbox=" + selectedCheckbox + "]"; }
}
