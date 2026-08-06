/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface MultiDoc {
  javax.print.MultiDoc next() throws java.io.IOException;
  javax.print.Doc getDoc() throws java.io.IOException;
}
