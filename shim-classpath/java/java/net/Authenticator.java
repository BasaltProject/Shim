/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public abstract class Authenticator {
  public static enum RequestorType {
    PROXY, SERVER
  }

  private static volatile Authenticator theAuthenticator;

  private String requestingHost;
  private InetAddress requestingSite;
  private int requestingPort;
  private String requestingProtocol;
  private String requestingPrompt;
  private String requestingScheme;
  private URL requestingURL;
  private RequestorType requestingAuthType;

  public Authenticator() {
  }

  private void reset() {
    requestingHost = null;
    requestingSite = null;
    requestingPort = -1;
    requestingProtocol = null;
    requestingPrompt = null;
    requestingScheme = null;
    requestingURL = null;
    requestingAuthType = RequestorType.SERVER;
  }

  public static synchronized void setDefault(Authenticator a) {
    theAuthenticator = a;
  }

  public static Authenticator getDefault() {
    return theAuthenticator;
  }

  public static PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol,
      String prompt, String scheme) {
    return requestPasswordAuthentication(null, addr, port, protocol, prompt, scheme);
  }

  public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port,
      String protocol, String prompt, String scheme) {
    Authenticator a = theAuthenticator;
    if (a == null) {
      return null;
    }
    return a.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme, null,
        RequestorType.SERVER);
  }

  public static PasswordAuthentication requestPasswordAuthentication(String host, InetAddress addr, int port,
      String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
    Authenticator a = theAuthenticator;
    if (a == null) {
      return null;
    }
    return a.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme, url, reqType);
  }

  public static PasswordAuthentication requestPasswordAuthentication(Authenticator authenticator, String host,
      InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
    Authenticator a = authenticator == null ? theAuthenticator : authenticator;
    if (a == null) {
      return null;
    }
    return a.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme, url, reqType);
  }

  public PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr, int port,
      String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
    synchronized (this) {
      this.reset();
      this.requestingHost = host;
      this.requestingSite = addr;
      this.requestingPort = port;
      this.requestingProtocol = protocol;
      this.requestingPrompt = prompt;
      this.requestingScheme = scheme;
      this.requestingURL = url;
      this.requestingAuthType = reqType;
      return this.getPasswordAuthentication();
    }
  }

  protected final String getRequestingHost() {
    return requestingHost;
  }

  protected final InetAddress getRequestingSite() {
    return requestingSite;
  }

  protected final int getRequestingPort() {
    return requestingPort;
  }

  protected final String getRequestingProtocol() {
    return requestingProtocol;
  }

  protected final String getRequestingPrompt() {
    return requestingPrompt;
  }

  protected final String getRequestingScheme() {
    return requestingScheme;
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return null;
  }

  protected URL getRequestingURL() {
    return requestingURL;
  }

  protected RequestorType getRequestorType() {
    return requestingAuthType;
  }
}
