/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

public interface Result {
  String PI_DISABLE_OUTPUT_ESCAPING = "javax.xml.transform.disable-output-escaping";
  String PI_ENABLE_OUTPUT_ESCAPING = "javax.xml.transform.enable-output-escaping";

  void setSystemId(String p0);

  String getSystemId();
}
