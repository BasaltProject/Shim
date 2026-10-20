/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import javax.lang.model.SourceVersion;

public interface Tool {
  default String name() {
    return "";
  }

  int run(InputStream in, OutputStream out, OutputStream err, String... arguments);

  Set<SourceVersion> getSourceVersions();
}
