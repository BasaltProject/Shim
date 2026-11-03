/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.rmi.server.RMIClassLoader;
import java.util.Arrays;

public final class MarshalledObject<T> implements Serializable {
  private byte[] objBytes = null;
  private byte[] locBytes = null;
  private int hash;

  private static final long serialVersionUID = 8988374069173025854L;

  public MarshalledObject(T obj) throws IOException {
    if (obj == null) {
      hash = 13;
      return;
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ByteArrayOutputStream lout = new ByteArrayOutputStream();
    MarshalledObjectOutputStream out = new MarshalledObjectOutputStream(bout, lout);
    out.writeObject(obj);
    out.flush();
    objBytes = bout.toByteArray();
    locBytes = out.hadAnnotations() ? lout.toByteArray() : null;
    int h = 0;
    for (int i = 0; i < objBytes.length; i++) {
      h = 31 * h + objBytes[i];
    }
    hash = h;
  }

  public T get() throws IOException, ClassNotFoundException {
    if (objBytes == null) {
      return null;
    }
    ByteArrayInputStream bin = new ByteArrayInputStream(objBytes);
    ByteArrayInputStream lin = (locBytes == null) ? null : new ByteArrayInputStream(locBytes);
    MarshalledObjectInputStream in = new MarshalledObjectInputStream(bin, lin);
    @SuppressWarnings("unchecked")
    T obj = (T) in.readObject();
    in.close();
    return obj;
  }

  public int hashCode() {
    return hash;
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj instanceof MarshalledObject) {
      MarshalledObject<?> other = (MarshalledObject<?>) obj;
      if (this.hash != other.hash) {
        return false;
      }
      return Arrays.equals(this.objBytes, other.objBytes);
    } else {
      return false;
    }
  }

  private static class MarshalledObjectOutputStream extends ObjectOutputStream {
    private ObjectOutputStream locOut;
    private boolean hadAnnotations;

    MarshalledObjectOutputStream(java.io.OutputStream objOut, java.io.OutputStream locOut) throws IOException {
      super(objOut);
      this.useProtocolVersion(java.io.ObjectStreamConstants.PROTOCOL_VERSION_2);
      this.locOut = new ObjectOutputStream(locOut);
      hadAnnotations = false;
    }

    boolean hadAnnotations() {
      return hadAnnotations;
    }

    protected void annotateClass(Class<?> cl) throws IOException {
      Object annotation = RMIClassLoader.getClassAnnotation(cl);
      if (annotation != null) {
        hadAnnotations = true;
      }
      locOut.writeObject(annotation);
    }

    protected void annotateProxyClass(Class<?> cl) throws IOException {
      annotateClass(cl);
    }

    public void flush() throws IOException {
      super.flush();
      locOut.flush();
    }
  }

  private static class MarshalledObjectInputStream extends ObjectInputStream {
    private ObjectInputStream locIn;

    MarshalledObjectInputStream(java.io.InputStream objIn, java.io.InputStream locIn) throws IOException {
      super(objIn);
      this.locIn = (locIn == null ? null : new ObjectInputStream(locIn));
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      String annotation = (locIn == null ? null : (String) locIn.readObject());
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return RMIClassLoader.loadClass(annotation, desc.getName(), loader);
    }

    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
      String annotation = (locIn == null ? null : (String) locIn.readObject());
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return RMIClassLoader.loadProxyClass(annotation, interfaces, loader);
    }
  }
}
