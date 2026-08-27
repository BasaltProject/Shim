/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLDocument extends org.w3c.dom.Document {
  void close();
  org.w3c.dom.html.HTMLCollection getAnchors();
  org.w3c.dom.html.HTMLCollection getApplets();
  org.w3c.dom.html.HTMLElement getBody();
  java.lang.String getCookie();
  java.lang.String getDomain();
  org.w3c.dom.NodeList getElementsByName(java.lang.String string);
  org.w3c.dom.html.HTMLCollection getForms();
  org.w3c.dom.html.HTMLCollection getImages();
  org.w3c.dom.html.HTMLCollection getLinks();
  java.lang.String getReferrer();
  java.lang.String getTitle();
  java.lang.String getURL();
  void open();
  void setBody(org.w3c.dom.html.HTMLElement hTMLElement);
  void setCookie(java.lang.String string);
  void setTitle(java.lang.String string);
  void write(java.lang.String string);
  void writeln(java.lang.String string);
}
