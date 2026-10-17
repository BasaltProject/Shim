/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.util.ResourceBundle;

public interface ResourceBundleControlProvider {
  ResourceBundle.Control getControl(String baseName);
}
