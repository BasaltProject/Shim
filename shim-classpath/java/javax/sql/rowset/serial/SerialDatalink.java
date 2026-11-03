/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class SerialDatalink implements Serializable, Cloneable {
  private static final long serialVersionUID = 2826907821828733626L;

  private URL url;
  private int baseType;
  private String baseTypeName;

  public SerialDatalink(URL url) throws SerialException {
    if (url == null) {
      throw new SerialException("Cannot serialize empty URL instance");
    }
    this.url = url;
  }

  public URL getDatalink() throws SerialException {
    URL aURL = null;
    try {
      aURL = new URL((this.url).toString());
    } catch (MalformedURLException e) {
      throw new SerialException("MalformedURLException: " + e.getMessage());
    }
    return aURL;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SerialDatalink) {
      SerialDatalink sdl = (SerialDatalink) obj;
      return baseType == sdl.baseType && baseTypeName == null ? sdl.baseTypeName == null
          : baseTypeName.equals(sdl.baseTypeName) && url.equals(sdl.url);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * (31 * (31 + baseType) + (baseTypeName == null ? 0 : baseTypeName.hashCode())) + url.hashCode();
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
