/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.parsers;

import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  private final Map<String, Boolean> features = new HashMap<String, Boolean>();
  private boolean xInclude;

  public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    return new DocumentBuilderImpl(this, attributes, features);
  }

  public void setAttribute(String name, Object value) throws IllegalArgumentException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      attributes.remove(name);
      return;
    }
    if (name.startsWith("http://javax.xml.XMLConstants/property/") || name.startsWith("jdk.xml.")
        || name.startsWith("http://java.sun.com/xml/jaxp/properties/")
        || name.startsWith("http://apache.org/xml/properties/")) {
      attributes.put(name, value);
      return;
    }
    throw new IllegalArgumentException("Property '" + name + "' is not recognized.");
  }

  public Object getAttribute(String name) throws IllegalArgumentException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (attributes.containsKey(name)) {
      return attributes.get(name);
    }
    if (name.startsWith("http://javax.xml.XMLConstants/property/") || name.startsWith("jdk.xml.")
        || name.startsWith("http://java.sun.com/xml/jaxp/properties/")
        || name.startsWith("http://apache.org/xml/properties/")) {
      return null;
    }
    throw new IllegalArgumentException("Property '" + name + "' is not recognized.");
  }

  public void setFeature(String name, boolean value) throws ParserConfigurationException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)
        || name.equals("http://apache.org/xml/features/disallow-doctype-decl")
        || name.equals("http://xml.org/sax/features/external-general-entities")
        || name.equals("http://xml.org/sax/features/external-parameter-entities")
        || name.equals("http://apache.org/xml/features/nonvalidating/load-external-dtd")
        || name.equals("http://apache.org/xml/features/dom/create-entity-ref-nodes")
        || name.equals("http://apache.org/xml/features/nonvalidating/load-dtd-grammar")
        || name.equals("http://apache.org/xml/features/continue-after-fatal-error")) {
      features.put(name, value);
      return;
    }
    throw new ParserConfigurationException("Feature '" + name + "' is not recognized.");
  }

  public boolean getFeature(String name) throws ParserConfigurationException {
    if (name == null) {
      throw new NullPointerException();
    }
    Boolean set = features.get(name);
    if (set != null) {
      return set;
    }
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)
        || name.equals("http://xml.org/sax/features/external-general-entities")
        || name.equals("http://xml.org/sax/features/external-parameter-entities")
        || name.equals("http://apache.org/xml/features/nonvalidating/load-external-dtd")
        || name.equals("http://apache.org/xml/features/nonvalidating/load-dtd-grammar")) {
      return true;
    }
    if (name.equals("http://apache.org/xml/features/disallow-doctype-decl")
        || name.equals("http://apache.org/xml/features/dom/create-entity-ref-nodes")
        || name.equals("http://apache.org/xml/features/continue-after-fatal-error")) {
      return false;
    }
    throw new ParserConfigurationException("Feature '" + name + "' is not recognized.");
  }

  public void setXIncludeAware(boolean state) {
    xInclude = state;
  }

  public boolean isXIncludeAware() {
    return xInclude;
  }
}
