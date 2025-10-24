/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.InputSource;
import shim.xml.XmlException;
import shim.xml.XmlInput;
import shim.xml.XmlPullParser;

final class StaxResolver implements XmlPullParser.Resolver {
  static final String kPlaceholder = "<!-- Exception scanning External DTD Subset.  True contents of DTD cannot be "
      + "determined.  Processing will continue as XMLInputFactory.IS_VALIDATING == false. -->";

  boolean externalDtdFailed;
  private final XMLResolver user;
  private final boolean ignoreExternalDtd;

  StaxResolver(XMLResolver user, boolean ignoreExternalDtd) {
    this.user = user;
    this.ignoreExternalDtd = ignoreExternalDtd;
  }

  public XmlInput resolve(String name, String publicId, String systemId, String baseUri)
      throws IOException, XmlException {
    boolean dtd = "[dtd]".equals(name);
    if (dtd && ignoreExternalDtd) {
      return null;
    }
    String expanded = XmlInput.expand(systemId, baseUri);
    try {
      if (user != null) {
        Object o = user.resolveEntity(publicId, systemId, baseUri, null);
        if (o instanceof InputStream) {
          InputSource src = new InputSource((InputStream) o);
          src.setSystemId(expanded);
          return XmlInput.open(src, baseUri);
        }
        if (o instanceof Reader) {
          InputSource src = new InputSource((Reader) o);
          src.setSystemId(expanded);
          return XmlInput.open(src, baseUri);
        }
      }
    } catch (XMLStreamException e) {
      throw new IOException(e.getMessage());
    }
    InputSource src = new InputSource(expanded);
    src.setPublicId(publicId);
    try {
      return XmlInput.open(src, null);
    } catch (IOException e) {
      if (dtd) {
        externalDtdFailed = true;
      }
      return null;
    }
  }
}
