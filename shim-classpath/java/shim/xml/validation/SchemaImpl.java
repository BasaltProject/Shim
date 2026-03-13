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
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import shim.xml.SaxReader;
import shim.xml.ser.DomToSax;

/** A compiled schema; validators created from it share the immutable component model. */
final class SchemaImpl extends Schema {
  private final XsdSchema schema;

  SchemaImpl(XsdSchema schema) {
    this.schema = schema;
  }

  @Override
  public Validator newValidator() {
    return new ValidatorImpl(schema);
  }

  @Override
  public ValidatorHandler newValidatorHandler() {
    return new HandlerImpl(schema);
  }

  private static final class ValidatorImpl extends Validator {
    private final XsdSchema schema;
    private ErrorHandler errorHandler;
    private LSResourceResolver resolver;

    ValidatorImpl(XsdSchema schema) {
      this.schema = schema;
    }

    @Override
    public void reset() {
      errorHandler = null;
      resolver = null;
    }

    @Override
    public void validate(Source source, Result result) throws SAXException, IOException {
      if (source == null) {
        throw new NullPointerException("Source parameter cannot be null.");
      }
      XsdValidator v = new XsdValidator(schema);
      v.setErrorHandler(errorHandler);
      if (result != null) {
        try {
          TransformerHandler th = ((SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance())
              .newTransformerHandler();
          th.setResult(result);
          v.setForward(th);
        } catch (javax.xml.transform.TransformerConfigurationException e) {
          throw new SAXException(e);
        }
      }
      if (source instanceof DOMSource) {
        Node node = ((DOMSource) source).getNode();
        if (node == null) {
          throw new IllegalArgumentException("DOMSource contains no node.");
        }
        v.startDocument();
        new DomToSax(v, null, true).parse(node);
        v.endDocument();
      } else if (source instanceof SAXSource) {
        SAXSource ss = (SAXSource) source;
        XMLReader reader = ss.getXMLReader();
        if (reader == null) {
          reader = new SaxReader(true);
        }
        reader.setContentHandler(v);
        reader.parse(ss.getInputSource());
      } else if (source instanceof StreamSource) {
        InputSource is = SAXSource.sourceToInputSource(source);
        SaxReader reader = new SaxReader(true);
        reader.setContentHandler(v);
        reader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
        reader.parse(is);
      } else {
        throw new IllegalArgumentException("Source parameter must be a StreamSource, SAXSource or DOMSource.");
      }
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
      return errorHandler;
    }

    @Override
    public void setResourceResolver(LSResourceResolver resourceResolver) {
      this.resolver = resourceResolver;
    }

    @Override
    public LSResourceResolver getResourceResolver() {
      return resolver;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
      if (name == null) {
        throw new NullPointerException("the name parameter is null");
      }
      if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
        return true;
      }
      throw new SAXNotRecognizedException("Feature: " + name + " is not recognized.");
    }
  }

  private static final class HandlerImpl extends ValidatorHandler {
    private final XsdValidator validator;
    private LSResourceResolver resolver;

    HandlerImpl(XsdSchema schema) {
      this.validator = new XsdValidator(schema);
    }

    @Override
    public void setContentHandler(ContentHandler receiver) {
      validator.setForward(receiver);
    }

    @Override
    public ContentHandler getContentHandler() {
      return validator.forward();
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
      validator.setErrorHandler(errorHandler);
    }

    @Override
    public ErrorHandler getErrorHandler() {
      return validator.errorHandler();
    }

    @Override
    public void setResourceResolver(LSResourceResolver resourceResolver) {
      this.resolver = resourceResolver;
    }

    @Override
    public LSResourceResolver getResourceResolver() {
      return resolver;
    }

    @Override
    public TypeInfoProvider getTypeInfoProvider() {
      return null;
    }

    public void setDocumentLocator(org.xml.sax.Locator locator) {
      validator.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
      validator.startDocument();
    }

    public void endDocument() throws SAXException {
      validator.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
      validator.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
      validator.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts)
        throws SAXException {
      validator.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
      validator.endElement(uri, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
      validator.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
      validator.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
      validator.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
      validator.skippedEntity(name);
    }
  }
}
