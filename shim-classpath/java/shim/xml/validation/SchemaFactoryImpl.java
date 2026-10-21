/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** The W3C XML Schema factory. */
public final class SchemaFactoryImpl extends SchemaFactory {
  private ErrorHandler errorHandler;
  private LSResourceResolver resolver;

  public boolean isSchemaLanguageSupported(String schemaLanguage) {
    if (schemaLanguage == null) {
      throw new NullPointerException("The schema language parameter cannot be null.");
    }
    return schemaLanguage.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setResourceResolver(LSResourceResolver resourceResolver) {
    this.resolver = resourceResolver;
  }

  public LSResourceResolver getResourceResolver() {
    return resolver;
  }

  public Schema newSchema(Source[] schemas) throws SAXException {
    if (schemas == null) {
      throw new NullPointerException("schemas array is null");
    }
    XsdSchema schema = new XsdSchema(new XsdSchema.Loader() {
      public Document load(String publicId, String systemId, String base, String namespace)
          throws SAXException, IOException {
        String id = systemId;
        if (base != null) {
          try {
            id = java.net.URI.create(base).resolve(systemId).toString();
          } catch (IllegalArgumentException e) {
            id = systemId;
          }
        }
        return XsdSchema.parse(new javax.xml.transform.stream.StreamSource(id), null);
      }
    });
    try {
      for (Source s : schemas) {
        if (s == null) {
          throw new NullPointerException("Source array contains a null item.");
        }
        schema.add(XsdSchema.parse(s, null), s.getSystemId());
      }
      schema.check(null);
    } catch (SAXParseException e) {
      if (errorHandler != null) {
        errorHandler.error(e);
      }
      throw e;
    } catch (IOException e) {
      throw new SAXException(e);
    }
    return new SchemaImpl(schema);
  }

  public Schema newSchema() throws SAXException {
    return new SchemaImpl(new XsdSchema(null));
  }
}
