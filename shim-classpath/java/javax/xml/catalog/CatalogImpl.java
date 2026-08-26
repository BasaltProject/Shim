/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class CatalogImpl implements Catalog {
  static final String NS = "urn:oasis:names:tc:entity:xmlns:xml:catalog";

  private static final class Entry {
    final String kind;
    final String key;
    final String value;
    final boolean preferPublic;

    Entry(String kind, String key, String value, boolean preferPublic) {
      this.kind = kind;
      this.key = key;
      this.value = value;
      this.preferPublic = preferPublic;
    }
  }

  private final CatalogFeatures features;
  private final List<Entry> entries = new ArrayList<Entry>();
  private final List<URI> nextCatalogs = new ArrayList<URI>();
  private final List<CatalogImpl> loadedNext = new ArrayList<CatalogImpl>();
  private final boolean preferPublicDefault;
  private final boolean defer;
  private boolean nextLoaded;

  CatalogImpl(CatalogFeatures features, URI... uris) {
    this.features = features;
    this.preferPublicDefault = "public".equals(features.get(CatalogFeatures.Feature.PREFER));
    this.defer = "true".equals(features.get(CatalogFeatures.Feature.DEFER));
    List<URI> files = new ArrayList<URI>();
    if (uris != null && uris.length > 0) {
      for (URI u : uris) {
        if (u == null) {
          throw new NullPointerException();
        }
        files.add(check(u));
      }
    } else {
      String f = features.get(CatalogFeatures.Feature.FILES);
      if (f != null) {
        for (String s : f.split(";")) {
          s = s.trim();
          if (s.length() > 0) {
            files.add(check(URI.create(s)));
          }
        }
      }
    }
    for (URI u : files) {
      load(u);
    }
    nextCatalogs.addAll(0, java.util.Collections.<URI>emptyList());
  }

  private CatalogImpl(CatalogFeatures features, URI single, boolean ignored) {
    this(features, new URI[] {single});
  }

  static String badArg(String value, String name) {
    return "JAXP09020005: The specified argument '" + value + "' (case sensitive) for '" + name + "' is not valid.";
  }

  private static URI check(URI u) {
    if (!u.isAbsolute()) {
      throw new IllegalArgumentException("JAXP09020003: The specified URI '" + u + "' is not absolute.");
    }
    return u;
  }

  private void load(final URI file) {
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      try {
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      } catch (Exception e) {
      }
      final List<String> bases = new ArrayList<String>();
      final List<Boolean> prefers = new ArrayList<Boolean>();
      bases.add(file.toString());
      prefers.add(Boolean.valueOf(preferPublicDefault));
      DefaultHandler h = new DefaultHandler() {
        public InputSource resolveEntity(String publicId, String systemId) {
          return new InputSource(new java.io.StringReader(""));
        }

        public void startElement(String uri, String local, String qName, Attributes a) throws SAXException {
          boolean root = bases.size() == 1 && entries.isEmpty() && !seenRoot;
          if (!NS.equals(uri)) {
            bases.add(bases.get(bases.size() - 1));
            prefers.add(prefers.get(prefers.size() - 1));
            return;
          }
          seenRoot = true;
          String base = bases.get(bases.size() - 1);
          String xmlBase = a.getValue("http://www.w3.org/XML/1998/namespace", "base");
          if (xmlBase != null) {
            base = resolve(base, xmlBase);
          }
          boolean pp = prefers.get(prefers.size() - 1).booleanValue();
          String prefer = a.getValue("prefer");
          if (prefer != null && (local.equals("catalog") || local.equals("group"))) {
            pp = prefer.equals("public");
          }
          bases.add(base);
          prefers.add(Boolean.valueOf(pp));
          if (local.equals("public")) {
            add("public", norm(a.getValue("publicId")), resolve(base, a.getValue("uri")), pp);
          } else if (local.equals("system")) {
            add("system", normSys(a.getValue("systemId")), resolve(base, a.getValue("uri")), pp);
          } else if (local.equals("uri")) {
            add("uri", normSys(a.getValue("name")), resolve(base, a.getValue("uri")), pp);
          } else if (local.equals("rewriteSystem")) {
            add("rewriteSystem", normSys(a.getValue("systemIdStartString")), resolve(base, a.getValue("rewritePrefix")), pp);
          } else if (local.equals("rewriteURI")) {
            add("rewriteURI", normSys(a.getValue("uriStartString")), resolve(base, a.getValue("rewritePrefix")), pp);
          } else if (local.equals("systemSuffix")) {
            add("systemSuffix", normSys(a.getValue("systemIdSuffix")), resolve(base, a.getValue("uri")), pp);
          } else if (local.equals("uriSuffix")) {
            add("uriSuffix", normSys(a.getValue("uriSuffix")), resolve(base, a.getValue("uri")), pp);
          } else if (local.equals("delegatePublic")) {
            add("delegatePublic", norm(a.getValue("publicIdStartString")), resolve(base, a.getValue("catalog")), pp);
          } else if (local.equals("delegateSystem")) {
            add("delegateSystem", normSys(a.getValue("systemIdStartString")), resolve(base, a.getValue("catalog")), pp);
          } else if (local.equals("delegateURI")) {
            add("delegateURI", normSys(a.getValue("uriStartString")), resolve(base, a.getValue("catalog")), pp);
          } else if (local.equals("nextCatalog")) {
            nextCatalogs.add(URI.create(resolve(base, a.getValue("catalog"))));
          }
        }

        boolean seenRoot;

        public void endElement(String uri, String local, String qName) {
          bases.remove(bases.size() - 1);
          prefers.remove(prefers.size() - 1);
        }
      };
      InputSource in = new InputSource(file.toString());
      spf.newSAXParser().parse(in, h);
    } catch (IOException e) {
    } catch (SAXException e) {
      throw new CatalogException("Fail to parse the catalog file '" + file + "'", e);
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new CatalogException(e.getMessage(), e);
    }
  }

  private void add(String kind, String key, String value, boolean pp) {
    if (key == null || value == null) {
      return;
    }
    entries.add(new Entry(kind, key, value, pp));
  }

  static String resolve(String base, String uri) {
    if (uri == null) {
      return null;
    }
    try {
      String esc = uri.replace(" ", "%20");
      URI u = new URI(esc);
      if (u.isAbsolute() || base == null) {
        return u.toString();
      }
      return new java.net.URL(new java.net.URL(base), esc).toString();
    } catch (URISyntaxException e) {
      return uri;
    } catch (java.net.MalformedURLException e) {
      return uri;
    }
  }

  static String norm(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    StringBuilder b = new StringBuilder();
    boolean sp = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isWhitespace(c)) {
        sp = true;
      } else {
        if (sp && b.length() > 0) {
          b.append(' ');
        }
        sp = false;
        b.append(c);
      }
    }
    String r = b.toString();
    if (r.startsWith("urn:publicid:")) {
      r = decodeUrn(r.substring("urn:publicid:".length()));
    }
    return r;
  }

  static String normSys(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim().replace(" ", "%20");
    return s;
  }

  static String decodeUrn(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '+') {
        b.append(' ');
      } else if (c == ':') {
        b.append("//");
      } else if (c == ';') {
        b.append("::");
      } else if (c == '%' && i + 2 < s.length() + 0 && i + 2 <= s.length() - 1 + 0) {
        String hex = s.substring(i + 1, i + 3);
        String r = null;
        if (hex.equalsIgnoreCase("2B")) {
          r = "+";
        } else if (hex.equalsIgnoreCase("3A")) {
          r = ":";
        } else if (hex.equalsIgnoreCase("2F")) {
          r = "/";
        } else if (hex.equalsIgnoreCase("3B")) {
          r = ";";
        } else if (hex.equalsIgnoreCase("27")) {
          r = "'";
        } else if (hex.equalsIgnoreCase("3F")) {
          r = "?";
        } else if (hex.equalsIgnoreCase("23")) {
          r = "#";
        } else if (hex.equalsIgnoreCase("25")) {
          r = "%";
        }
        if (r != null) {
          b.append(r);
          i += 2;
        } else {
          b.append(c);
        }
      } else {
        b.append(c);
      }
    }
    return b.toString();
  }

  private synchronized List<CatalogImpl> next() {
    if (!nextLoaded) {
      nextLoaded = true;
      for (URI u : nextCatalogs) {
        loadedNext.add(new CatalogImpl(features, u, true));
      }
    }
    return loadedNext;
  }

  public Stream<Catalog> catalogs() {
    List<Catalog> l = new ArrayList<Catalog>(next());
    return l.stream();
  }

  public String matchPublic(String publicId) {
    return matchPublic(publicId, false);
  }

  String matchPublic(String publicId, boolean withSystem) {
    if (publicId == null) {
      return null;
    }
    for (Entry e : entries) {
      if (e.kind.equals("public") && e.key.equals(publicId) && (!withSystem || e.preferPublic)) {
        return e.value;
      }
    }
    return delegate("delegatePublic", publicId, "public");
  }

  public String matchSystem(String systemId) {
    if (systemId == null) {
      return null;
    }
    for (Entry e : entries) {
      if (e.kind.equals("system") && e.key.equals(systemId)) {
        return e.value;
      }
    }
    String r = rewrite("rewriteSystem", systemId);
    if (r != null) {
      return r;
    }
    r = suffix("systemSuffix", systemId);
    if (r != null) {
      return r;
    }
    r = delegate("delegateSystem", systemId, "system");
    if (r != null) {
      return r;
    }
    return null;
  }

  public String matchURI(String uri) {
    if (uri == null) {
      return null;
    }
    for (Entry e : entries) {
      if (e.kind.equals("uri") && e.key.equals(uri)) {
        return e.value;
      }
    }
    String r = rewrite("rewriteURI", uri);
    if (r != null) {
      return r;
    }
    r = suffix("uriSuffix", uri);
    if (r != null) {
      return r;
    }
    r = delegate("delegateURI", uri, "uri");
    if (r != null) {
      return r;
    }
    return null;
  }

  private String rewrite(String kind, String id) {
    Entry best = null;
    for (Entry e : entries) {
      if (e.kind.equals(kind) && id.startsWith(e.key) && (best == null || e.key.length() > best.key.length())) {
        best = e;
      }
    }
    return best == null ? null : best.value + id.substring(best.key.length());
  }

  private String suffix(String kind, String id) {
    Entry best = null;
    for (Entry e : entries) {
      if (e.kind.equals(kind) && id.endsWith(e.key) && (best == null || e.key.length() > best.key.length())) {
        best = e;
      }
    }
    return best == null ? null : best.value;
  }

  private String delegate(String kind, String id, String what) {
    List<Entry> matches = new ArrayList<Entry>();
    for (Entry e : entries) {
      if (e.kind.equals(kind) && id.startsWith(e.key)) {
        matches.add(e);
      }
    }
    if (matches.isEmpty()) {
      return null;
    }
    java.util.Collections.sort(matches, new java.util.Comparator<Entry>() {
      public int compare(Entry a, Entry b) {
        return b.key.length() - a.key.length();
      }
    });
    for (Entry e : matches) {
      CatalogImpl c = new CatalogImpl(features, URI.create(e.value), true);
      String r = what.equals("public") ? c.matchPublic(id, false) : what.equals("system") ? c.matchSystem(id) : c.matchURI(id);
      if (r != null) {
        return r;
      }
    }
    return null;
  }
}
