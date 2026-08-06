/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface URIException {
  int URIInaccessible = 1;
  int URISchemeNotSupported = 2;
  int URIOtherProblem = -1;
  int getReason();
  java.net.URI getUnsupportedURI();
}
