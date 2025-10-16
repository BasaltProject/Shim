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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import shim.xml.SaxReader;

public class SAXParserFactoryImpl extends SAXParserFactory {
  private final Map<String, Boolean> features = new HashMap<String, Boolean>();

  public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
    SAXParserImpl parser;
    try {
      parser = new SAXParserImpl(this, features);
    } catch (SAXException e) {
      throw new ParserConfigurationException(e.getMessage());
    }
    return parser;
  }

  public void setFeature(String name, boolean value) throws ParserConfigurationException,
      SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING)) {
      features.put(name, value);
      return;
    }
    SaxReader probe = new SaxReader(isNamespaceAware());
    probe.setFeature(name, value);
    features.put(name, value);
  }

  public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException,
      SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals("http://xml.org/sax/features/namespaces")) {
      return isNamespaceAware();
    }
    if (name.equals("http://xml.org/sax/features/validation")) {
      return isValidating();
    }
    Boolean set = features.get(name);
    if (set != null) {
      return set;
    }
    return new SaxReader(isNamespaceAware()).getFeature(name);
  }
}
