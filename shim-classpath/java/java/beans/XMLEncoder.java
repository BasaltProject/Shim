/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import shim.beans.XmlWrite;

public class XMLEncoder extends Encoder implements AutoCloseable {
  private final OutputStream out;
  private final Charset charset;
  private final boolean declaration;
  private final List<Object> pending = new ArrayList<Object>();
  private boolean started;
  private Object owner;

  public XMLEncoder(OutputStream out) { this(out, "UTF-8", true, 0); }

  public XMLEncoder(OutputStream out, String charset, boolean declaration, int indentation) {
    if (out == null) {
      throw new IllegalArgumentException("the output stream cannot be null");
    }
    if (indentation < 0) {
      throw new IllegalArgumentException("the indentation must be >= 0");
    }
    this.out = out;
    this.charset = Charset.forName(charset);
    this.declaration = declaration;
  }

  public void setOwner(Object owner) { this.owner = owner; }

  public Object getOwner() { return owner; }

  public void writeObject(Object o) {
    if (o == this) {
      return;
    }
    synchronized (pending) {
      pending.add(o);
    }
  }

  public void writeStatement(Statement oldStm) { super.writeStatement(oldStm); }

  public void writeExpression(Expression oldExp) { super.writeExpression(oldExp); }

  public void flush() {
    StringBuilder sb = new StringBuilder();
    synchronized (pending) {
      if (!started) {
        started = true;
        if (declaration) {
          sb.append("<?xml version=\"1.0\" encoding=\"").append(charset.name()).append("\"?>\n");
        }
        sb.append("<java version=\"").append(System.getProperty("java.version")).append("\" class=\"java.beans.XMLDecoder\">\n");
      }
      for (Object o : pending) {
        sb.append(new XmlWrite(getExceptionListener()).render(o));
      }
      pending.clear();
    }
    try {
      out.write(sb.toString().getBytes(charset));
      out.flush();
    } catch (IOException e) {
      getExceptionListener().exceptionThrown(e);
    }
  }

  public void close() {
    flush();
    try {
      out.write("</java>\n".getBytes(charset));
      out.close();
    } catch (IOException e) {
      getExceptionListener().exceptionThrown(e);
    }
  }
}
