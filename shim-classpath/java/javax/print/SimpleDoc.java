/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;

public final class SimpleDoc implements Doc {
  private final DocFlavor flavor;
  private DocAttributeSet attributes;
  private final Object printData;
  private Reader reader;
  private InputStream inStream;

  public SimpleDoc(Object printData, DocFlavor flavor, DocAttributeSet attributes) {
    if (flavor == null || printData == null) {
      throw new IllegalArgumentException("null argument(s)");
    }
    Class<?> repClass = null;
    try {
      String className = flavor.getRepresentationClassName();
      repClass = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
    } catch (Throwable e) {
      throw new IllegalArgumentException("unknown representation class");
    }
    if (!repClass.isInstance(printData)) {
      throw new IllegalArgumentException("data is not of declared type");
    }
    this.flavor = flavor;
    if (attributes != null) {
      this.attributes = AttributeSetUtilities.unmodifiableView(attributes);
    }
    this.printData = printData;
  }

  public DocFlavor getDocFlavor() { return flavor; }

  public DocAttributeSet getAttributes() { return attributes; }

  public Object getPrintData() throws IOException { return printData; }

  public Reader getReaderForText() throws IOException {
    if (printData instanceof Reader) {
      return (Reader) printData;
    }
    if (printData instanceof char[]) {
      if (reader == null) {
        reader = new CharArrayReader((char[]) printData);
      }
      return reader;
    }
    if (printData instanceof String) {
      if (reader == null) {
        reader = new StringReader((String) printData);
      }
      return reader;
    }
    return null;
  }

  public InputStream getStreamForBytes() throws IOException {
    if (printData instanceof InputStream) {
      return (InputStream) printData;
    }
    if (printData instanceof byte[]) {
      if (inStream == null) {
        inStream = new ByteArrayInputStream((byte[]) printData);
      }
      return inStream;
    }
    return null;
  }
}
