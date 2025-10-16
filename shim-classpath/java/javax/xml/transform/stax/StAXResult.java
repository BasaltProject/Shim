/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform.stax;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

public class StAXResult implements Result {
  public static final String FEATURE = "http://javax.xml.transform.stax.StAXResult/feature";

  private XMLEventWriter xmlEventWriter = null;
  private XMLStreamWriter xmlStreamWriter = null;

  public StAXResult(XMLEventWriter xmlEventWriter) {
    if (xmlEventWriter == null) {
      throw new IllegalArgumentException("StAXResult(XMLEventWriter) with XMLEventWriter == null");
    }
    this.xmlEventWriter = xmlEventWriter;
  }

  public StAXResult(XMLStreamWriter xmlStreamWriter) {
    if (xmlStreamWriter == null) {
      throw new IllegalArgumentException("StAXResult(XMLStreamWriter) with XMLStreamWriter == null");
    }
    this.xmlStreamWriter = xmlStreamWriter;
  }

  public XMLEventWriter getXMLEventWriter() {
    return xmlEventWriter;
  }

  public XMLStreamWriter getXMLStreamWriter() {
    return xmlStreamWriter;
  }

  public void setSystemId(String systemId) {
    throw new UnsupportedOperationException("StAXResult#setSystemId(systemId) cannot set the system identifier for a StAXResult");
  }

  public String getSystemId() {
    return null;
  }
}
