/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

public interface Templates {
  Transformer newTransformer() throws TransformerConfigurationException;

  java.util.Properties getOutputProperties();
}
