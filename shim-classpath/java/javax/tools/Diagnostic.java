/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import java.util.Locale;

public interface Diagnostic<S> {
  enum Kind { ERROR, WARNING, MANDATORY_WARNING, NOTE, OTHER }

  long NOPOS = -1;

  Kind getKind();

  S getSource();

  long getPosition();

  long getStartPosition();

  long getEndPosition();

  long getLineNumber();

  long getColumnNumber();

  String getCode();

  String getMessage(Locale locale);
}
