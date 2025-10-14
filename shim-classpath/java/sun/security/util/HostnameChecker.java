/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import sun.security.x509.X500Name;

public final class HostnameChecker {
  public static final byte kTypeTls = 1;
  public static final byte kTypeLdap = 2;

  private static final HostnameChecker kTlsChecker = new HostnameChecker(kTypeTls);
  private static final HostnameChecker kLdapChecker = new HostnameChecker(kTypeLdap);

  private final byte checkType;

  private HostnameChecker(byte checkType) {
    this.checkType = checkType;
  }

  public static HostnameChecker getInstance(byte checkType) {
    return checkType == kTypeTls ? kTlsChecker : kLdapChecker;
  }

  public void match(String expectedName, X509Certificate cert, boolean chainsToPublicCA) throws CertificateException {
    if (isIpAddress(expectedName)) {
      matchIP(expectedName, cert);
    } else {
      matchDNS(expectedName, cert, chainsToPublicCA);
    }
  }

  public static boolean isIpAddress(String name) {
    if (name == null) {
      return false;
    }
    if (name.indexOf(':') >= 0) {
      return name.indexOf('.') < 0 || name.matches("[0-9a-fA-F:]*:[0-9.]+");
    }
    String[] parts = name.split("\\.", -1);
    if (parts.length != 4) {
      return false;
    }
    for (String p : parts) {
      if (p.isEmpty() || p.length() > 3) {
        return false;
      }
      for (int i = 0; i < p.length(); i++) {
        if (!Character.isDigit(p.charAt(i))) {
          return false;
        }
      }
      if (Integer.parseInt(p) > 255) {
        return false;
      }
    }
    return true;
  }

  private static byte[] ipBytes(String ip) {
    try {
      return InetAddress.getByName(ip).getAddress();
    } catch (UnknownHostException e) {
      return null;
    }
  }

  private static void matchIP(String expectedIP, X509Certificate cert) throws CertificateException {
    Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
    if (subjAltNames == null) {
      throw new CertificateException("No subject alternative names present");
    }
    byte[] expected = ipBytes(expectedIP);
    for (List<?> next : subjAltNames) {
      if (((Integer) next.get(0)).intValue() == 7) {
        String ipAddress = (String) next.get(1);
        if (expectedIP.equalsIgnoreCase(ipAddress)) {
          return;
        }
        byte[] actual = ipBytes(ipAddress);
        if (expected != null && actual != null && java.util.Arrays.equals(expected, actual)) {
          return;
        }
      }
    }
    throw new CertificateException("No subject alternative names matching IP address " + expectedIP + " found");
  }

  private void matchDNS(String expectedName, X509Certificate cert, boolean chainsToPublicCA)
      throws CertificateException {
    Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
    if (subjAltNames != null) {
      boolean foundDNS = false;
      for (List<?> next : subjAltNames) {
        if (((Integer) next.get(0)).intValue() == 2) {
          foundDNS = true;
          String dnsName = (String) next.get(1);
          if (isMatched(expectedName, dnsName, chainsToPublicCA)) {
            return;
          }
        }
      }
      if (foundDNS) {
        throw new CertificateException("No subject alternative DNS name matching " + expectedName + " found.");
      }
    }
    X500Name subjectName = X500Name.decode(cert.getSubjectX500Principal().getEncoded());
    String cn = subjectName.getMostSpecificCommonName();
    if (cn != null && isMatched(expectedName, cn, chainsToPublicCA)) {
      return;
    }
    String msg = "No name matching " + expectedName + " found";
    throw new CertificateException(msg);
  }

  public static boolean isMatched(String name, String template, boolean chainsToPublicCA) {
    if (hasIllegalWildcard(template, chainsToPublicCA)) {
      return false;
    }
    try {
      String n = IDN.toUnicode(IDN.toASCII(name, IDN.ALLOW_UNASSIGNED));
      String t = IDN.toUnicode(IDN.toASCII(template, IDN.ALLOW_UNASSIGNED));
      if (n.endsWith(".")) {
        n = n.substring(0, n.length() - 1);
      }
      if (t.endsWith(".")) {
        t = t.substring(0, t.length() - 1);
      }
      return matchLeftmostWildcard(n.toLowerCase(Locale.ENGLISH), t.toLowerCase(Locale.ENGLISH));
    } catch (RuntimeException re) {
      return false;
    }
  }

  private static boolean hasIllegalWildcard(String template, boolean chainsToPublicCA) {
    int lastWildcardIndex = template.lastIndexOf('*');
    if (lastWildcardIndex == -1) {
      return false;
    }
    int firstDot = template.indexOf('.');
    if (firstDot < 0 || lastWildcardIndex > firstDot) {
      return true;
    }
    if (template.indexOf('*') != lastWildcardIndex) {
      return true;
    }
    if (chainsToPublicCA) {
      String rest = template.substring(firstDot + 1);
      return rest.indexOf('.') < 0;
    }
    return false;
  }

  private static boolean matchLeftmostWildcard(String name, String template) {
    int templateIdx = template.indexOf('.');
    int nameIdx = name.indexOf('.');
    if (templateIdx < 0 || nameIdx < 0) {
      return name.equals(template);
    }
    String templateLabel = template.substring(0, templateIdx);
    int starIdx = templateLabel.indexOf('*');
    if (starIdx == -1) {
      return name.equals(template);
    }
    String nameRest = name.substring(nameIdx);
    String templateRest = template.substring(templateIdx);
    if (!nameRest.equals(templateRest)) {
      return false;
    }
    String nameLabel = name.substring(0, nameIdx);
    String prefix = templateLabel.substring(0, starIdx);
    String suffix = templateLabel.substring(starIdx + 1);
    return nameLabel.length() >= prefix.length() + suffix.length() && nameLabel.startsWith(prefix)
        && nameLabel.endsWith(suffix);
  }
}
