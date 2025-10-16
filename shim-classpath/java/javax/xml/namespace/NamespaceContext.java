/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.namespace;

public interface NamespaceContext {
  String getNamespaceURI(String p0);

  String getPrefix(String p0);

  java.util.Iterator<String> getPrefixes(String p0);
}
