/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

import java.io.IOException;

public class JMXServerErrorException extends IOException {
  private static final long serialVersionUID = 3996732239558744666L;

  private final Error cause;

  public JMXServerErrorException(String s, Error err) {
    super(s);
    cause = err;
  }

  @Override
  public Throwable getCause() {
    return cause;
  }
}
