/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.Serializable;
import java.security.Permission;

public final class SocketPermission extends Permission implements Serializable {
  private static final long serialVersionUID = -7204263841984476862L;

  private static final int kConnect = 1;
  private static final int kListen = 2;
  private static final int kAccept = 4;
  private static final int kResolve = 8;

  private final int mask;
  private final String host;
  private final int portMin;
  private final int portMax;

  public SocketPermission(String host, String action) {
    super(host);
    this.mask = parseActions(action);
    String h = host;
    int lo = 0;
    int hi = 65535;
    int colon = h.lastIndexOf(':');
    if (colon > 0 && h.indexOf(':') == colon) {
      String ports = h.substring(colon + 1);
      h = h.substring(0, colon);
      try {
        int dash = ports.indexOf('-');
        if (dash < 0) {
          lo = hi = Integer.parseInt(ports);
        } else {
          lo = dash == 0 ? 0 : Integer.parseInt(ports.substring(0, dash));
          hi = dash == ports.length() - 1 ? 65535 : Integer.parseInt(ports.substring(dash + 1));
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("invalid port range: " + ports);
      }
    }
    if (h.isEmpty()) {
      h = "localhost";
    }
    this.host = h.toLowerCase();
    this.portMin = lo;
    this.portMax = hi;
  }

  private static int parseActions(String action) {
    if (action == null || action.isEmpty()) {
      throw new IllegalArgumentException("invalid permission: " + action);
    }
    int mask = 0;
    for (String a : action.toLowerCase().split(",")) {
      switch (a.trim()) {
        case "connect": mask |= kConnect | kResolve; break;
        case "listen": mask |= kListen | kResolve; break;
        case "accept": mask |= kAccept | kResolve; break;
        case "resolve": mask |= kResolve; break;
        default: throw new IllegalArgumentException("invalid permission: " + action);
      }
    }
    return mask;
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof SocketPermission)) {
      return false;
    }
    SocketPermission that = (SocketPermission) p;
    if ((that.mask & ~mask) != 0) {
      return false;
    }
    if (that.portMin < portMin || that.portMax > portMax) {
      return false;
    }
    if (host.equals("*")) {
      return true;
    }
    if (host.startsWith("*.")) {
      return that.host.endsWith(host.substring(1));
    }
    return host.equals(that.host);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SocketPermission)) {
      return false;
    }
    SocketPermission that = (SocketPermission) obj;
    return mask == that.mask && host.equals(that.host) && portMin == that.portMin && portMax == that.portMax;
  }

  @Override
  public int hashCode() {
    return host.hashCode() ^ mask;
  }

  @Override
  public String getActions() {
    StringBuilder sb = new StringBuilder();
    boolean comma = false;
    if ((mask & kConnect) == kConnect) {
      sb.append("connect");
      comma = true;
    }
    if ((mask & kListen) == kListen) {
      if (comma) {
        sb.append(',');
      }
      sb.append("listen");
      comma = true;
    }
    if ((mask & kAccept) == kAccept) {
      if (comma) {
        sb.append(',');
      }
      sb.append("accept");
      comma = true;
    }
    if ((mask & kResolve) == kResolve) {
      if (comma) {
        sb.append(',');
      }
      sb.append("resolve");
    }
    return sb.toString();
  }
}
