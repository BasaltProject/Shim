/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

import java.io.InputStream;
import javax.xml.transform.Source;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

public interface CatalogResolver extends org.xml.sax.EntityResolver, javax.xml.stream.XMLResolver,
    javax.xml.transform.URIResolver, org.w3c.dom.ls.LSResourceResolver {
  InputSource resolveEntity(String publicId, String systemId);

  LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI);

  Source resolve(String href, String base);

  InputStream resolveEntity(String publicId, String systemId, String baseURI, String namespace);

  public enum NotFoundAction {
    CONTINUE("continue"), IGNORE("ignore"), STRICT("strict");

    private final String literal;

    NotFoundAction(String literal) {
      this.literal = literal;
    }

    public static NotFoundAction getType(String type) {
      for (NotFoundAction a : values()) {
        if (a.literal.equals(type)) {
          return a;
        }
      }
      throw new IllegalArgumentException(CatalogImpl.badArg(type, "RESOLVE"));
    }
  }
}
