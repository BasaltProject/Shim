/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import shim.beans.MiniXml;
import shim.beans.XmlEval;

public class XMLDecoder implements AutoCloseable {
  private final InputStream in;
  private Object owner;
  private ExceptionListener exceptionListener;
  private final ClassLoader cl;
  private List<Object> objects;
  private int index;

  public XMLDecoder(InputStream in) { this(in, null, null, null); }

  public XMLDecoder(InputStream in, Object owner) { this(in, owner, null, null); }

  public XMLDecoder(InputStream in, Object owner, ExceptionListener exceptionListener) {
    this(in, owner, exceptionListener, null);
  }

  public XMLDecoder(InputStream in, Object owner, ExceptionListener exceptionListener, ClassLoader cl) {
    this.in = in;
    this.owner = owner;
    this.exceptionListener = exceptionListener;
    this.cl = cl;
  }

  public XMLDecoder(InputSource is) {
    this(is.getByteStream(), null, null, null);
  }

  public void close() {
    if (in != null) {
      try {
        in.close();
      } catch (IOException e) {
        getExceptionListener().exceptionThrown(e);
      }
    }
  }

  private void load() {
    if (objects != null) {
      return;
    }
    try {
      MiniXml.Node root = MiniXml.parse(in);
      objects = new XmlEval(owner, getExceptionListener(), cl).run(root);
    } catch (IOException e) {
      getExceptionListener().exceptionThrown(e);
      objects = new java.util.ArrayList<Object>();
    }
  }

  public void setExceptionListener(ExceptionListener exceptionListener) {
    if (exceptionListener == null) {
      exceptionListener = Statement.defaultExceptionListener;
    }
    this.exceptionListener = exceptionListener;
  }

  public ExceptionListener getExceptionListener() {
    return (exceptionListener != null) ? exceptionListener : Statement.defaultExceptionListener;
  }

  public Object readObject() {
    load();
    if (index >= objects.size()) {
      throw new ArrayIndexOutOfBoundsException("no more objects to read");
    }
    return objects.get(index++);
  }

  public void setOwner(Object owner) { this.owner = owner; }

  public Object getOwner() { return owner; }

  public static DefaultHandler createHandler(Object owner, ExceptionListener el, ClassLoader cl) {
    return new DefaultHandler();
  }
}
