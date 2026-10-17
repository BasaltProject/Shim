/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;

public class DomLsSerializer implements LSSerializer {
  private String newLine;
  private LSSerializerFilter filter;

  public DOMConfiguration getDomConfig() {
    return null;
  }

  public String getNewLine() {
    return newLine;
  }

  public void setNewLine(String newLine) {
    this.newLine = newLine;
  }

  public LSSerializerFilter getFilter() {
    return filter;
  }

  public void setFilter(LSSerializerFilter filter) {
    this.filter = filter;
  }

  public boolean write(Node nodeArg, LSOutput destination) throws LSException {
    throw new LSException(LSException.SERIALIZE_ERR, "not implemented");
  }

  public boolean writeToURI(Node nodeArg, String uri) throws LSException {
    throw new LSException(LSException.SERIALIZE_ERR, "not implemented");
  }

  public String writeToString(Node nodeArg) throws LSException {
    throw new LSException(LSException.SERIALIZE_ERR, "not implemented");
  }
}
