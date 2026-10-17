/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.XMLFilter;

public class TransformerFactoryImpl extends SAXTransformerFactory {
  private URIResolver resolver;
  private ErrorListener errorListener = new TransformErrorListener();
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  private boolean secureProcessing = true;

  public Transformer newTransformer(Source source) throws TransformerConfigurationException {
    if (source == null) {
      throw new NullPointerException();
    }
    return new StylesheetCompiler(this).compile(source);
  }

  public Transformer newTransformer() throws TransformerConfigurationException {
    IdentityTransformer t = new IdentityTransformer(this);
    return t;
  }

  public Templates newTemplates(Source source) throws TransformerConfigurationException {
    if (source == null) {
      throw new NullPointerException();
    }
    return new StylesheetCompiler(this).compileTemplates(source);
  }

  public Source getAssociatedStylesheet(Source source, String media, String title, String charset)
      throws TransformerConfigurationException {
    throw new TransformerConfigurationException("Associated stylesheets are not supported.");
  }

  public void setURIResolver(URIResolver resolver) {
    this.resolver = resolver;
  }

  public URIResolver getURIResolver() {
    return resolver;
  }

  public void setFeature(String name, boolean value) throws TransformerConfigurationException {
    if (name == null) {
      throw new NullPointerException("Feature name is null.");
    }
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      secureProcessing = value;
      return;
    }
    if (supportedFeature(name)) {
      if (!value) {
        throw new TransformerConfigurationException("Feature " + name + " cannot be set to false.");
      }
      return;
    }
    throw new TransformerConfigurationException("Cannot set the feature '" + name + "' on this TransformerFactory.");
  }

  private static boolean supportedFeature(String name) {
    return name.equals(DOMSource.FEATURE) || name.equals(DOMResult.FEATURE) || name.equals(SAXSource.FEATURE)
        || name.equals(SAXResult.FEATURE) || name.equals(StreamSource.FEATURE) || name.equals(StreamResult.FEATURE)
        || name.equals(StAXSource.FEATURE) || name.equals(StAXResult.FEATURE)
        || name.equals(SAXTransformerFactory.FEATURE) || name.equals(SAXTransformerFactory.FEATURE_XMLFILTER);
  }

  public boolean getFeature(String name) {
    if (name == null) {
      throw new NullPointerException("Feature name is null.");
    }
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      return secureProcessing;
    }
    return supportedFeature(name);
  }

  public void setAttribute(String name, Object value) {
    if (name.equals("indent-number")) {
      if (value instanceof Integer || value instanceof String) {
        attributes.put(name, value);
        return;
      }
      throw new IllegalArgumentException("TransformerFactory does not recognise attribute '" + name + "'.");
    }
    if (name.equals("enable-inlining") || name.equals("translet-name") || name.equals("destination-directory")
        || name.equals("package-name") || name.equals("jar-name") || name.equals("generate-translet")
        || name.equals("auto-translet") || name.equals("use-classpath") || name.equals("debug")
        || name.equals("disable-xsl-element") || name.equals("http://javax.xml.XMLConstants/property/accessExternalDTD")
        || name.equals("http://javax.xml.XMLConstants/property/accessExternalStylesheet")) {
      attributes.put(name, value);
      return;
    }
    throw new IllegalArgumentException("TransformerFactory does not recognise attribute '" + name + "'.");
  }

  public Object getAttribute(String name) {
    if (attributes.containsKey(name)) {
      return attributes.get(name);
    }
    if (name.equals("translet-name")) {
      return "GregorSamsa";
    }
    if (name.equals("indent-number") || name.equals("enable-inlining") || name.equals("generate-translet")
        || name.equals("auto-translet") || name.equals("use-classpath") || name.equals("debug")
        || name.equals("destination-directory") || name.equals("package-name") || name.equals("jar-name")
        || name.equals("disable-xsl-element")
        || name.equals("http://javax.xml.XMLConstants/property/accessExternalDTD")
        || name.equals("http://javax.xml.XMLConstants/property/accessExternalStylesheet")) {
      if (name.equals("enable-inlining") || name.equals("generate-translet") || name.equals("auto-translet")
          || name.equals("use-classpath") || name.equals("debug")) {
        return Boolean.FALSE;
      }
      if (name.equals("indent-number")) {
        throw new IllegalArgumentException("TransformerFactory does not recognise attribute 'indent-number'.");
      }
      return null;
    }
    throw new IllegalArgumentException("TransformerFactory does not recognise attribute '" + name + "'.");
  }

  Object attribute(String name) {
    return attributes.get(name);
  }

  public void setErrorListener(ErrorListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Attempting to set ErrorListener for 'TransformerFactory' to null");
    }
    this.errorListener = listener;
  }

  public ErrorListener getErrorListener() {
    return errorListener;
  }

  public TransformerHandler newTransformerHandler(Source src) throws TransformerConfigurationException {
    Transformer t = newTransformer(src);
    return new TransformerHandlerImpl((IdentityTransformer) t);
  }

  public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
    Transformer t = templates.newTransformer();
    return new TransformerHandlerImpl((IdentityTransformer) t);
  }

  public TransformerHandler newTransformerHandler() throws TransformerConfigurationException {
    return new TransformerHandlerImpl((IdentityTransformer) newTransformer());
  }

  public TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException {
    throw new TransformerConfigurationException("TemplatesHandler is not supported.");
  }

  public XMLFilter newXMLFilter(Source src) throws TransformerConfigurationException {
    throw new TransformerConfigurationException("XMLFilter is not supported.");
  }

  public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
    throw new TransformerConfigurationException("XMLFilter is not supported.");
  }
}
