/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Locale;

public class JMXServiceURL implements Serializable {
  private static final long serialVersionUID = 8173364409860779292L;

  private String protocol;
  private String host;
  private int port;
  private String urlPath;
  private transient String toString;

  public JMXServiceURL(String serviceURL) throws MalformedURLException {
    final String serviceURLLowerCase = serviceURL.toLowerCase(Locale.ENGLISH);
    final String requiredPrefix = "service:jmx:";
    final int requiredPrefixLength = requiredPrefix.length();
    if (!serviceURLLowerCase.startsWith(requiredPrefix)) {
      throw new MalformedURLException("Service URL must start with " + requiredPrefix);
    }
    final int protoStart = requiredPrefixLength;
    final int protoEnd = indexOfFirstNotInSet(serviceURL, protocolBitSet, protoStart);
    this.protocol = serviceURL.substring(protoStart, protoEnd).toLowerCase(Locale.ENGLISH);
    if (!serviceURL.regionMatches(protoEnd, "://", 0, 3)) {
      throw new MalformedURLException("Missing \"://\" after protocol name");
    }
    final int hostStart = protoEnd + 3;
    final int hostEnd;
    if (hostStart < serviceURL.length() && serviceURL.charAt(hostStart) == '[') {
      hostEnd = serviceURL.indexOf(']', hostStart) + 1;
      if (hostEnd == 0) {
        throw new MalformedURLException("Bad host name: [ without ]");
      }
      this.host = serviceURL.substring(hostStart + 1, hostEnd - 1);
      if (!isNumericIPv6Address(this.host)) {
        throw new MalformedURLException("Address inside [...] must be numeric IPv6 address");
      }
      if (this.host.charAt(0) == '[') {
        throw new MalformedURLException("More than one [[...]]");
      }
    } else {
      hostEnd = indexOfFirstNotInSet(serviceURL, hostNameBitSet, hostStart);
      this.host = serviceURL.substring(hostStart, hostEnd);
    }
    final int portEnd;
    if (hostEnd < serviceURL.length() && serviceURL.charAt(hostEnd) == ':') {
      if (this.host.length() == 0) {
        throw new MalformedURLException("Cannot give port number without host name");
      }
      final int portStart = hostEnd + 1;
      portEnd = indexOfFirstNotInSet(serviceURL, rangeSet('0', '9'), portStart);
      final String portString = serviceURL.substring(portStart, portEnd);
      try {
        this.port = Integer.parseInt(portString);
      } catch (NumberFormatException e) {
        throw new MalformedURLException("Bad port number: \"" + portString + "\": " + e);
      }
    } else {
      portEnd = hostEnd;
      this.port = 0;
    }
    final int urlPathStart = portEnd;
    if (urlPathStart < serviceURL.length()) {
      this.urlPath = serviceURL.substring(urlPathStart);
    } else {
      this.urlPath = "";
    }
    validate();
  }

  public JMXServiceURL(String protocol, String host, int port) throws MalformedURLException {
    this(protocol, host, port, null);
  }

  public JMXServiceURL(String protocol, String host, int port, String urlPath) throws MalformedURLException {
    if (protocol == null) {
      protocol = "jmxmp";
    }
    if (host == null) {
      InetAddress local;
      try {
        local = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        throw new MalformedURLException("Local host name unknown: " + e);
      }
      host = local.getHostName();
      if (isNumericIPv6Address(host)) {
        host = "[" + host + "]";
      }
    }
    if (host.startsWith("[")) {
      if (!host.endsWith("]")) {
        throw new MalformedURLException("Bad host name: [ without ]: " + host);
      }
      host = host.substring(1, host.length() - 1);
      if (!isNumericIPv6Address(host)) {
        throw new MalformedURLException("Bad host name: [...] with a non-numeric IPv6 address: " + host);
      }
    }
    if (port < 0) {
      throw new MalformedURLException("Bad port: " + port);
    }
    if (urlPath == null) {
      urlPath = "";
    }
    this.protocol = protocol.toLowerCase(Locale.ENGLISH);
    this.host = host;
    this.port = port;
    this.urlPath = urlPath;
    validate();
  }

  private static boolean isNumericIPv6Address(String s) {
    return s.indexOf(':') >= 0;
  }

  private void validate() throws MalformedURLException {
    final int protoEnd = indexOfFirstNotInSet(protocol, protocolBitSet, 0);
    final boolean validProto = (protoEnd == protocol.length() && protoEnd > 0);
    if (!validProto) {
      throw new MalformedURLException("Missing or invalid protocol name: \"" + protocol + "\"");
    }
    validateHost();
    if (port < 0) {
      throw new MalformedURLException("Bad port: " + port);
    }
    if (urlPath.length() > 0) {
      if (!urlPath.startsWith("/") && !urlPath.startsWith(";")) {
        throw new MalformedURLException("Bad URL path: " + urlPath);
      }
    }
  }

  private void validateHost() throws MalformedURLException {
    if (host.length() == 0) {
      return;
    }
    if (isNumericIPv6Address(host)) {
      try {
        InetAddress.getByName(host);
      } catch (Exception e) {
        throw new MalformedURLException("Bad IPv6 address: " + host);
      }
    } else {
      final int hostEnd = indexOfFirstNotInSet(host, hostNameBitSet, 0);
      if (hostEnd != host.length()) {
        throw new MalformedURLException("Bad character in host name: " + host);
      }
    }
  }

  private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField gf = inputStream.readFields();
    String h = (String) gf.get("host", null);
    int p = gf.get("port", -1);
    String proto = (String) gf.get("protocol", null);
    String path = (String) gf.get("urlPath", null);
    if (proto == null || path == null || h == null) {
      throw new java.io.InvalidObjectException("Missing field");
    }
    if (p < 0) {
      throw new java.io.InvalidObjectException("Invalid port");
    }
    this.host = h;
    this.port = p;
    this.protocol = proto;
    this.urlPath = path;
    try {
      validate();
    } catch (MalformedURLException e) {
      throw new java.io.InvalidObjectException(e.getMessage());
    }
  }

  public String getProtocol() {
    return protocol;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getURLPath() {
    return urlPath;
  }

  public String toString() {
    if (toString != null) {
      return toString;
    }
    StringBuilder buf = new StringBuilder("service:jmx:");
    buf.append(getProtocol()).append("://");
    final String getHost = getHost();
    if (getHost.length() > 0) {
      if (isNumericIPv6Address(getHost)) {
        buf.append('[').append(getHost).append(']');
      } else {
        buf.append(getHost);
      }
    }
    if (getPort() != 0) {
      buf.append(":").append(getPort());
    }
    buf.append(getURLPath());
    toString = buf.toString();
    return toString;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof JMXServiceURL)) {
      return false;
    }
    JMXServiceURL u = (JMXServiceURL) obj;
    return (u.getProtocol().equalsIgnoreCase(getProtocol()) && u.getHost().equalsIgnoreCase(getHost())
        && u.getPort() == getPort() && u.getURLPath().equals(getURLPath()));
  }

  public int hashCode() {
    return toString().hashCode();
  }

  private static final BitSet protocolBitSet = new BitSet(128);
  private static final BitSet hostNameBitSet = new BitSet(128);

  static {
    protocolBitSet.or(rangeSet('0', '9'));
    protocolBitSet.or(rangeSet('a', 'z'));
    protocolBitSet.or(rangeSet('A', 'Z'));
    protocolBitSet.set('+');
    protocolBitSet.set('-');
    hostNameBitSet.or(rangeSet('0', '9'));
    hostNameBitSet.or(rangeSet('a', 'z'));
    hostNameBitSet.or(rangeSet('A', 'Z'));
    hostNameBitSet.set('-');
    hostNameBitSet.set('.');
  }

  private static BitSet rangeSet(char lo, char hi) {
    BitSet b = new BitSet(128);
    for (char c = lo; c <= hi; c++) {
      b.set(c);
    }
    return b;
  }

  private static int indexOfFirstNotInSet(String s, BitSet set, int start) {
    final int slen = s.length();
    int i = start;
    while (i < slen) {
      char c = s.charAt(i);
      if (c >= 128 || !set.get(c)) {
        break;
      }
      i++;
    }
    return i;
  }

  private static int indexOf(String s, char c, int start) throws MalformedURLException {
    int i = s.indexOf(c, start);
    if (i < 0) {
      throw new MalformedURLException("Missing '" + c + "' character");
    }
    return i;
  }
}
