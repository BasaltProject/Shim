/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
import java.util.*;
import javax.net.ssl.*;
public class TlsLegacyProtocolsModern {
  public static void main(String[] a) throws Exception {
    for (String p : new String[] {"SSLv3", "TLSv1", "TLSv1.1", "SSLv2Hello", "bogus"}) {
      try {
        SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null);
        SSLEngine e = c.createSSLEngine();
        e.setEnabledProtocols(new String[] {p});
        System.out.println(p + " -> " + Arrays.toString(e.getEnabledProtocols()));
        e.setUseClientMode(true);
        java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(20000);
        e.wrap(java.nio.ByteBuffer.allocate(0), out);
        System.out.println(p + " wrap ok " + out.position());
      } catch (Throwable t) { System.out.println(p + " !! " + t); }
    }
    for (String n : new String[] {"TLS", "Default", "TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3", "DTLS", "DTLSv1.2", "DTLSv1.0"}) {
      try {
        SSLContext c = SSLContext.getInstance(n); c.init(null, null, null);
        System.out.println(n + " sup=" + Arrays.toString(c.getSupportedSSLParameters().getProtocols()) + " def=" + Arrays.toString(c.getDefaultSSLParameters().getProtocols()));
        SSLEngine e = c.createSSLEngine();
        System.out.println("  engine sup=" + Arrays.toString(e.getSupportedProtocols()) + " en=" + Arrays.toString(e.getEnabledProtocols()));
      } catch (Throwable t) { System.out.println(n + " !! " + t); }
    }
    SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null);
    System.out.println(c.createSSLEngine().getUseClientMode());
    System.out.println(Arrays.toString(c.getSupportedSSLParameters().getProtocols()) + " " + Arrays.toString(c.getDefaultSSLParameters().getProtocols()));
    SSLEngine e = c.createSSLEngine(); e.setUseClientMode(true);
    System.out.println(Arrays.toString(e.getEnabledProtocols()));
  }
}

