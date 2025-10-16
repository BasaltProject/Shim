/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.helpers;

import org.xml.sax.Parser;

@Deprecated
public class ParserFactory {
  private ParserFactory() {
  }

  public static Parser makeParser() throws ClassNotFoundException, IllegalAccessException, InstantiationException,
      NullPointerException, ClassCastException {
    String className = System.getProperty("org.xml.sax.parser");
    if (className == null) {
      throw new NullPointerException("No value for sax.parser property");
    }
    return makeParser(className);
  }

  public static Parser makeParser(String className) throws ClassNotFoundException, IllegalAccessException,
      InstantiationException, ClassCastException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Class<?> type = loader == null ? Class.forName(className) : Class.forName(className, true, loader);
    try {
      return (Parser) type.getDeclaredConstructor().newInstance();
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw new InstantiationException(String.valueOf(e.getCause()));
    } catch (NoSuchMethodException e) {
      throw new InstantiationException(className);
    }
  }
}
