/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform.sax;

public interface TemplatesHandler extends org.xml.sax.ContentHandler {
  javax.xml.transform.Templates getTemplates();

  void setSystemId(String p0);

  String getSystemId();
}
