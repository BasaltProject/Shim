/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSParserFilter;

public class DomLsParser implements LSParser {
  private LSParserFilter filter;

  public DOMConfiguration getDomConfig() {
    throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
  }

  public LSParserFilter getFilter() {
    return filter;
  }

  public void setFilter(LSParserFilter filter) {
    this.filter = filter;
  }

  public boolean getAsync() {
    return false;
  }

  public boolean getBusy() {
    return false;
  }

  public Document parse(LSInput input) throws DOMException, LSException {
    throw new LSException(LSException.PARSE_ERR, "not implemented");
  }

  public Document parseURI(String uri) throws DOMException, LSException {
    throw new LSException(LSException.PARSE_ERR, "not implemented");
  }

  public Node parseWithContext(LSInput input, Node contextArg, short action) throws DOMException, LSException {
    throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
  }

  public void abort() {
  }
}
