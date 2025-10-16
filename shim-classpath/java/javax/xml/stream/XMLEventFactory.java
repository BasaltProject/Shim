/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import shim.xml.FactoryFinder;

public abstract class XMLEventFactory {
  private static final String kDefault = "shim.xml.stream.XMLEventFactoryImpl";

  protected XMLEventFactory() {
  }

  public static XMLEventFactory newDefaultFactory() {
    return FactoryFinder.instantiate(XMLEventFactory.class, kDefault, XMLEventFactory.class.getClassLoader());
  }

  public static XMLEventFactory newInstance() throws FactoryConfigurationError {
    return newFactory();
  }

  public static XMLEventFactory newFactory() throws FactoryConfigurationError {
    try {
      return FactoryFinder.find(XMLEventFactory.class, kDefault, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e.getMessage(), (Exception) e.getCause());
    }
  }

  public static XMLEventFactory newInstance(String factoryId, ClassLoader classLoader)
      throws FactoryConfigurationError {
    return newFactory(factoryId, classLoader);
  }

  public static XMLEventFactory newFactory(String factoryId, ClassLoader classLoader)
      throws FactoryConfigurationError {
    if (factoryId == null) {
      throw new FactoryConfigurationError("factoryId cannot be null");
    }
    try {
      return FactoryFinder.instantiate(XMLEventFactory.class, factoryId, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e.getMessage(), (Exception) e.getCause());
    }
  }

  public abstract void setLocation(Location location);

  public abstract Attribute createAttribute(String prefix, String namespaceURI, String localName, String value);

  public abstract Attribute createAttribute(String localName, String value);

  public abstract Attribute createAttribute(QName name, String value);

  public abstract Namespace createNamespace(String namespaceURI);

  public abstract Namespace createNamespace(String prefix, String namespaceUri);

  public abstract StartElement createStartElement(QName name, Iterator<? extends Attribute> attributes,
                                                  Iterator<? extends Namespace> namespaces);

  public abstract StartElement createStartElement(String prefix, String namespaceUri, String localName);

  public abstract StartElement createStartElement(String prefix, String namespaceUri, String localName,
                                                  Iterator<? extends Attribute> attributes,
                                                  Iterator<? extends Namespace> namespaces);

  public abstract StartElement createStartElement(String prefix, String namespaceUri, String localName,
                                                  Iterator<? extends Attribute> attributes,
                                                  Iterator<? extends Namespace> namespaces,
                                                  javax.xml.namespace.NamespaceContext context);

  public abstract EndElement createEndElement(QName name, Iterator<? extends Namespace> namespaces);

  public abstract EndElement createEndElement(String prefix, String namespaceUri, String localName);

  public abstract EndElement createEndElement(String prefix, String namespaceUri, String localName,
                                              Iterator<? extends Namespace> namespaces);

  public abstract Characters createCharacters(String content);

  public abstract Characters createCData(String content);

  public abstract Characters createSpace(String content);

  public abstract Characters createIgnorableSpace(String content);

  public abstract StartDocument createStartDocument();

  public abstract StartDocument createStartDocument(String encoding, String version, boolean standalone);

  public abstract StartDocument createStartDocument(String encoding, String version);

  public abstract StartDocument createStartDocument(String encoding);

  public abstract EndDocument createEndDocument();

  public abstract EntityReference createEntityReference(String name, EntityDeclaration declaration);

  public abstract Comment createComment(String text);

  public abstract ProcessingInstruction createProcessingInstruction(String target, String data);

  public abstract DTD createDTD(String dtd);
}
