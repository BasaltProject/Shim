/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class ObjID implements Serializable {
  public static final int REGISTRY_ID = 0;
  public static final int ACTIVATOR_ID = 1;
  public static final int DGC_ID = 2;

  private static final long serialVersionUID = -6386392263968365220L;

  private static final AtomicLong nextObjNum = new AtomicLong(0);
  private static final UID mySpace = new UID();
  private static final SecureRandom secureRandom = new SecureRandom();

  private final long objNum;
  private final UID space;

  public ObjID() {
    if (Boolean.getBoolean("java.rmi.server.randomIDs")) {
      objNum = secureRandom.nextLong();
    } else {
      objNum = nextObjNum.getAndIncrement();
    }
    space = mySpace;
  }

  public ObjID(int objNum) {
    this.objNum = (long) objNum;
    this.space = new UID((short) 0);
  }

  private ObjID(long objNum, UID space) {
    this.objNum = objNum;
    this.space = space;
  }

  public void write(ObjectOutput out) throws IOException {
    out.writeLong(objNum);
    space.write(out);
  }

  public static ObjID read(ObjectInput in) throws IOException {
    long num = in.readLong();
    UID space = UID.read(in);
    return new ObjID(num, space);
  }

  public int hashCode() {
    return (int) objNum;
  }

  public boolean equals(Object obj) {
    if (obj instanceof ObjID) {
      ObjID id = (ObjID) obj;
      return objNum == id.objNum && space.equals(id.space);
    } else {
      return false;
    }
  }

  public String toString() {
    String str = "[";
    if (!space.equals(new UID((short) 0))) {
      str += space.toString() + ", ";
    }
    str += Long.toString(objNum) + "]";
    return str;
  }
}
