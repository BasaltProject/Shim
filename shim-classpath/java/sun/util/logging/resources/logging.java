/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.util.logging.resources;

import java.util.ListResourceBundle;

public final class logging extends ListResourceBundle {
  @Override
  protected Object[][] getContents() {
    return new Object[][] {
      {"ALL", "All"},
      {"CONFIG", "Config"},
      {"FINE", "Fine"},
      {"FINER", "Finer"},
      {"FINEST", "Finest"},
      {"INFO", "Info"},
      {"OFF", "Off"},
      {"SEVERE", "Severe"},
      {"WARNING", "Warning"},
    };
  }
}
