/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.util.XMLEventAllocator;

final class StaxConfig {
  boolean namespaceAware = true;
  boolean validating;
  boolean coalescing;
  boolean replacingEntities = true;
  boolean externalEntities = true;
  boolean supportDtd = true;
  boolean reportCdata;
  boolean ignoreExternalDtd;
  XMLReporter reporter;
  XMLResolver resolver;
  XMLEventAllocator allocator;

  StaxConfig copy() {
    StaxConfig c = new StaxConfig();
    c.namespaceAware = namespaceAware;
    c.validating = validating;
    c.coalescing = coalescing;
    c.replacingEntities = replacingEntities;
    c.externalEntities = externalEntities;
    c.supportDtd = supportDtd;
    c.reportCdata = reportCdata;
    c.ignoreExternalDtd = ignoreExternalDtd;
    c.reporter = reporter;
    c.resolver = resolver;
    c.allocator = allocator;
    return c;
  }
}
