/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

public final class XPathFactoryImpl extends XPathFactory {
  private XPathVariableResolver variables;
  private XPathFunctionResolver functions;
  private boolean secure;

  public boolean isObjectModelSupported(String objectModel) {
    if (objectModel == null) {
      throw new NullPointerException("com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl#isObjectModelSupported( String objectModel ) cannot be called with objectModel == null");
    }
    if (objectModel.length() == 0) {
      throw new IllegalArgumentException("com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl#isObjectModelSupported( String objectModel ) cannot be called with objectModel == \"\"");
    }
    return objectModel.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
  }

  public void setFeature(String name, boolean value) throws XPathFactoryConfigurationException {
    if (name == null) {
      throw new NullPointerException("Trying to set a feature with a null name: XPathFactoryImpl#setFeature( null, " + value + ")");
    }
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      secure = value;
      return;
    }
    throw new XPathFactoryConfigurationException("Trying to set the unknown feature \"" + name
        + "\":XPathFactoryImpl#setFeature(" + name + "," + value + ")");
  }

  public boolean getFeature(String name) throws XPathFactoryConfigurationException {
    if (name == null) {
      throw new NullPointerException("Trying to get a feature with a null name: XPathFactoryImpl#getFeature(null)");
    }
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      return secure;
    }
    throw new XPathFactoryConfigurationException("Trying to get the unknown feature \"" + name
        + "\":XPathFactoryImpl#getFeature(" + name + ")");
  }

  public void setXPathVariableResolver(XPathVariableResolver resolver) {
    if (resolver == null) {
      throw new NullPointerException("Attempting to set a null XPathVariableResolver:XPathFactoryImpl#setXPathVariableResolver(null)");
    }
    variables = resolver;
  }

  public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
    if (resolver == null) {
      throw new NullPointerException("Attempting to set a null XPathFunctionResolver:XPathFactoryImpl#setXPathFunctionResolver(null)");
    }
    functions = resolver;
  }

  public XPath newXPath() {
    return new XPathImpl(variables, functions);
  }
}
