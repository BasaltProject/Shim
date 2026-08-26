/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

final class CatalogResolverImpl implements CatalogResolver {
  private final Catalog catalog;
  private final NotFoundAction action;

  CatalogResolverImpl(Catalog catalog, NotFoundAction action) {
    this.catalog = catalog;
    this.action = action;
  }

  private String find(Catalog c, String pub, String sys) {
    String r = null;
    if (sys != null) {
      r = c.matchSystem(sys);
    }
    if (r == null && pub != null) {
      r = c instanceof CatalogImpl ? ((CatalogImpl) c).matchPublic(pub, sys != null) : c.matchPublic(pub);
    }
    if (r != null) {
      return r;
    }
    java.util.Iterator<Catalog> it = c.catalogs().iterator();
    while (it.hasNext()) {
      r = find(it.next(), pub, sys);
      if (r != null) {
        return r;
      }
    }
    return null;
  }

  private String findUri(Catalog c, String uri) {
    String r = c.matchURI(uri);
    if (r != null) {
      return r;
    }
    java.util.Iterator<Catalog> it = c.catalogs().iterator();
    while (it.hasNext()) {
      r = findUri(it.next(), uri);
      if (r != null) {
        return r;
      }
    }
    return null;
  }

  private String findEntity(String publicId, String systemId) {
    String pub = publicId;
    String sys = systemId;
    if (pub != null && pub.startsWith("urn:publicid:")) {
      pub = CatalogImpl.norm(pub);
    }
    if (sys != null && sys.startsWith("urn:publicid:")) {
      if (pub == null) {
        pub = CatalogImpl.norm(sys);
      }
      sys = null;
    }
    return find(catalog, pub, sys);
  }

  private void notFound(String publicId, String systemId) {
    throw new CatalogException("JAXP09040001: No match found for publicId '" + publicId + "' and systemId '"
        + systemId + "'.");
  }

  private static void requireSystemId(String systemId) {
    if (systemId == null) {
      throw new NullPointerException("JAXP09020006: The argument 'systemId' can not be null.");
    }
  }

  public InputSource resolveEntity(String publicId, String systemId) {
    requireSystemId(systemId);
    String r = findEntity(publicId, systemId);
    if (r != null) {
      return new InputSource(r);
    }
    return missing(publicId, systemId);
  }

  private InputSource missing(String publicId, String systemId) {
    if (action == NotFoundAction.STRICT) {
      notFound(publicId, systemId);
    }
    if (action == NotFoundAction.IGNORE) {
      return new InputSource(new java.io.StringReader(""));
    }
    return null;
  }

  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
      String baseURI) {
    String r = null;
    if (systemId != null || publicId != null) {
      r = find(catalog, publicId, systemId);
    }
    if (r == null && namespaceURI != null) {
      r = findUri(catalog, namespaceURI);
    }
    if (r == null && type != null && systemId != null) {
      r = findUri(catalog, systemId);
    }
    if (r != null) {
      return new SimpleLSInput(publicId, systemId, baseURI, r);
    }
    if (action == NotFoundAction.STRICT) {
      throw new CatalogException("JAXP09040001: No match found for publicId '" + publicId + "' and systemId '"
          + systemId + "'.");
    }
    if (action == NotFoundAction.IGNORE) {
      return new SimpleLSInput(publicId, systemId, baseURI, null);
    }
    return null;
  }

  public Source resolve(String href, String base) {
    if (href == null) {
      throw new NullPointerException();
    }
    String r = findUri(catalog, href);
    if (r == null && base != null) {
      String abs = CatalogImpl.resolve(base, href);
      if (abs != null && !abs.equals(href)) {
        r = findUri(catalog, abs);
      }
    }
    if (r != null) {
      return new SAXSource(new InputSource(r));
    }
    if (action == NotFoundAction.STRICT) {
      throw new CatalogException("JAXP09040002: No match found for href '" + href + "' and base '" + base + "'.");
    }
    if (action == NotFoundAction.IGNORE) {
      return new SAXSource(new InputSource(new java.io.StringReader("")));
    }
    return new SAXSource(new InputSource(href));
  }

  public InputStream resolveEntity(String publicId, String systemId, String baseURI, String namespace) {
    requireSystemId(systemId);
    String r = findEntity(publicId, systemId);
    if (r == null) {
      r = findUri(catalog, systemId);
    }
    if (r != null) {
      try {
        return new URL(r).openStream();
      } catch (IOException e) {
      }
    }
    if (action == NotFoundAction.STRICT) {
      notFound(publicId, systemId);
    }
    if (action == NotFoundAction.IGNORE) {
      return new java.io.ByteArrayInputStream(new byte[0]);
    }
    return null;
  }

  private static final class SimpleLSInput implements LSInput {
    private String publicId;
    private String systemId;
    private String baseURI;
    private String encoding;
    private boolean certifiedText;
    private java.io.Reader characterStream;
    private InputStream byteStream;
    private String stringData;

    SimpleLSInput(String publicId, String systemId, String baseURI, String resolved) {
      this.publicId = publicId;
      this.systemId = resolved;
      this.baseURI = baseURI;
      if (resolved == null) {
        this.stringData = "";
      }
    }

    public java.io.Reader getCharacterStream() {
      return characterStream;
    }

    public void setCharacterStream(java.io.Reader r) {
      characterStream = r;
    }

    public InputStream getByteStream() {
      return byteStream;
    }

    public void setByteStream(InputStream s) {
      byteStream = s;
    }

    public String getStringData() {
      return stringData;
    }

    public void setStringData(String s) {
      stringData = s;
    }

    public String getSystemId() {
      return systemId;
    }

    public void setSystemId(String s) {
      systemId = s;
    }

    public String getPublicId() {
      return publicId;
    }

    public void setPublicId(String s) {
      publicId = s;
    }

    public String getBaseURI() {
      return baseURI;
    }

    public void setBaseURI(String s) {
      baseURI = s;
    }

    public String getEncoding() {
      return encoding;
    }

    public void setEncoding(String s) {
      encoding = s;
    }

    public boolean getCertifiedText() {
      return certifiedText;
    }

    public void setCertifiedText(boolean b) {
      certifiedText = b;
    }
  }
}
