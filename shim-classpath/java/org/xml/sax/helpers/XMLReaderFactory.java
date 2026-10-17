/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.helpers;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class XMLReaderFactory {
  private static final String kProperty = "org.xml.sax.driver";
  private static final String kDefaultReader = "shim.xml.SaxReader";

  private XMLReaderFactory() {
  }

  public static XMLReader createXMLReader() throws SAXException {
    String className = System.getProperty(kProperty);
    if (className != null) {
      return loadClass(className);
    }
    try {
      Iterator<XMLReader> it = ServiceLoader.load(XMLReader.class).iterator();
      if (it.hasNext()) {
        return it.next();
      }
    } catch (Throwable t) {
    }
    return loadClass(kDefaultReader);
  }

  public static XMLReader createXMLReader(String className) throws SAXException {
    return loadClass(className);
  }

  private static XMLReader loadClass(String className) throws SAXException {
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> type = loader == null ? Class.forName(className) : Class.forName(className, true, loader);
      return (XMLReader) type.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException e1) {
      throw new SAXException("SAX2 driver class " + className + " not found", e1);
    } catch (IllegalAccessException e2) {
      throw new SAXException("SAX2 driver class " + className + " found but cannot be loaded", e2);
    } catch (ReflectiveOperationException e3) {
      throw new SAXException("SAX2 driver class " + className + " loaded but cannot be instantiated", e3);
    } catch (ClassCastException e4) {
      throw new SAXException("SAX2 driver class " + className + " does not implement XMLReader", e4);
    }
  }
}
