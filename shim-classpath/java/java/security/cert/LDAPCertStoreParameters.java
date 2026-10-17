/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class LDAPCertStoreParameters implements CertStoreParameters {
  private static final int LDAP_DEFAULT_PORT = 389;

  private final int port;
  private final String serverName;

  public LDAPCertStoreParameters(String serverName, int port) {
    if (serverName == null) {
      throw new NullPointerException();
    }
    this.serverName = serverName;
    this.port = port;
  }

  public LDAPCertStoreParameters(String serverName) {
    this(serverName, LDAP_DEFAULT_PORT);
  }

  public LDAPCertStoreParameters() {
    this("localhost", LDAP_DEFAULT_PORT);
  }

  public String getServerName() {
    return serverName;
  }

  public int getPort() {
    return port;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString(), e);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("LDAPCertStoreParameters: [\n");
    sb.append("  serverName: " + serverName + "\n");
    sb.append("  port: " + port + "\n");
    sb.append("]");
    return sb.toString();
  }
}
