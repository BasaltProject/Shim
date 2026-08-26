/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.spec;

import javax.xml.crypto.XMLStructure;

public final class XSLTTransformParameterSpec implements TransformParameterSpec {
  private XMLStructure stylesheet;

  public XSLTTransformParameterSpec(XMLStructure stylesheet) {
    if (stylesheet == null) {
      throw new NullPointerException();
    }
    this.stylesheet = stylesheet;
  }

  public XMLStructure getStylesheet() {
    return stylesheet;
  }
}
