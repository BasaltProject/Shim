/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

final class StylesheetCompiler {
  private final TransformerFactoryImpl factory;

  StylesheetCompiler(TransformerFactoryImpl factory) {
    this.factory = factory;
  }

  Transformer compile(Source source) throws TransformerConfigurationException {
    throw new TransformerConfigurationException("XSLT stylesheets are not supported.");
  }

  Templates compileTemplates(Source source) throws TransformerConfigurationException {
    throw new TransformerConfigurationException("XSLT stylesheets are not supported.");
  }
}
