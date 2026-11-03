/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;

public class UnicastRef2 extends UnicastRef {
  private static final long serialVersionUID = 1829537514995881838L;

  public UnicastRef2() {
  }

  public UnicastRef2(String host, int port, ObjID objID, RMIClientSocketFactory csf) {
    super(host, port, objID, csf);
  }

  @Override
  public String getRefClass(ObjectOutput out) {
    return "UnicastRef2";
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeByte(csf == null ? 0 : 1);
    out.writeUTF(host);
    out.writeInt(port);
    if (csf != null) {
      out.writeObject(csf);
    }
    objID.write(out);
    out.writeBoolean(false);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    byte format = in.readByte();
    switch (format) {
      case 0:
        host = in.readUTF();
        port = in.readInt();
        break;
      case 1:
        host = in.readUTF();
        port = in.readInt();
        csf = (RMIClientSocketFactory) in.readObject();
        break;
      default:
        throw new IOException("invalid endpoint format");
    }
    objID = ObjID.read(in);
    if (in.readBoolean()) {
      UnicastRef.RefReadState.mark();
    }
  }
}
