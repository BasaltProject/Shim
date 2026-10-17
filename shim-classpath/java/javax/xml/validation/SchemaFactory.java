/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.validation;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceLoader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import shim.xml.FactoryFinder;

public abstract class SchemaFactory {
  private static final String kDefault = "shim.xml.validation.SchemaFactoryImpl";

  protected SchemaFactory() {
  }

  public static SchemaFactory newDefaultInstance() {
    return FactoryFinder.instantiate(SchemaFactory.class, kDefault, SchemaFactory.class.getClassLoader());
  }

  public static SchemaFactory newInstance(String schemaLanguage) {
    if (schemaLanguage == null) {
      throw new NullPointerException("The schema language parameter cannot be null.");
    }
    String name = System.getProperty(SchemaFactory.class.getName() + ":" + schemaLanguage);
    if (name != null) {
      SchemaFactory factory = FactoryFinder.instantiate(SchemaFactory.class, name, null);
      if (factory.isSchemaLanguageSupported(schemaLanguage)) {
        return factory;
      }
    }
    try {
      Iterator<SchemaFactory> it = ServiceLoader.load(SchemaFactory.class).iterator();
      while (it.hasNext()) {
        SchemaFactory candidate = it.next();
        if (candidate.isSchemaLanguageSupported(schemaLanguage)) {
          return candidate;
        }
      }
    } catch (Throwable ignored) {
    }
    SchemaFactory builtin = newDefaultInstance();
    if (builtin.isSchemaLanguageSupported(schemaLanguage)) {
      return builtin;
    }
    throw new IllegalArgumentException("No SchemaFactory that implements the schema language specified by: "
        + schemaLanguage + " could be loaded");
  }

  public static SchemaFactory newInstance(String schemaLanguage, String factoryClassName, ClassLoader classLoader) {
    if (schemaLanguage == null) {
      throw new NullPointerException("The schema language parameter cannot be null.");
    }
    if (factoryClassName == null) {
      throw new IllegalArgumentException("factoryClassName cannot be null.");
    }
    SchemaFactory factory = FactoryFinder.instantiate(SchemaFactory.class, factoryClassName, classLoader);
    if (factory.isSchemaLanguageSupported(schemaLanguage)) {
      return factory;
    }
    throw new IllegalArgumentException("No SchemaFactory that implements the schema language specified by: "
        + schemaLanguage + " could be loaded");
  }

  public abstract boolean isSchemaLanguageSupported(String schemaLanguage);

  public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException("the name parameter is null");
    }
    throw new SAXNotRecognizedException(name);
  }

  public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException("the name parameter is null");
    }
    throw new SAXNotRecognizedException(name);
  }

  public void setProperty(String name, Object object) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException("the name parameter is null");
    }
    throw new SAXNotRecognizedException(name);
  }

  public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException("the name parameter is null");
    }
    throw new SAXNotRecognizedException(name);
  }

  public abstract void setErrorHandler(ErrorHandler errorHandler);

  public abstract ErrorHandler getErrorHandler();

  public abstract void setResourceResolver(LSResourceResolver resourceResolver);

  public abstract LSResourceResolver getResourceResolver();

  public Schema newSchema(Source schema) throws SAXException {
    return newSchema(new Source[] {schema});
  }

  public Schema newSchema(File schema) throws SAXException {
    return newSchema(new StreamSource(schema));
  }

  public Schema newSchema(URL schema) throws SAXException {
    return newSchema(new StreamSource(schema.toExternalForm()));
  }

  public abstract Schema newSchema(Source[] schemas) throws SAXException;

  public abstract Schema newSchema() throws SAXException;
}
