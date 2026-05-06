/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RemoteStub;
import sun.rmi.transport.ObjectTable;

public class MarshalOutputStream extends ObjectOutputStream {
  public MarshalOutputStream(OutputStream out) throws IOException {
    super(out);
    useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2);
    enableReplaceObject(true);
  }

  @Override
  protected void annotateClass(Class<?> cl) throws IOException {
    writeObject(RMIClassLoader.getClassAnnotation(cl));
  }

  @Override
  protected void annotateProxyClass(Class<?> cl) throws IOException {
    writeObject(RMIClassLoader.getClassAnnotation(cl));
  }

  @Override
  protected Object replaceObject(Object obj) throws IOException {
    if ((obj instanceof Remote) && !(obj instanceof RemoteStub)) {
      Remote stub = ObjectTable.stubFor((Remote) obj);
      if (stub != null) {
        return stub;
      }
    }
    return obj;
  }
}
