/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;

final class SslConfig {
  boolean clientMode = true;
  String[] protocols;
  String[] cipherSuites;
  boolean needClientAuth;
  boolean wantClientAuth;
  String endpointIdentification;
  List<SNIServerName> serverNames;
  Collection<SNIMatcher> sniMatchers;
  String[] applicationProtocols = new String[0];
  String[] signatureSchemes;
  String[] namedGroups;
  boolean enableSessionCreation = true;
  boolean useCipherSuitesOrder;
  BiFunction<?, List<String>, String> alpnSelector;

  SslConfig copy() {
    SslConfig c = new SslConfig();
    c.clientMode = clientMode;
    c.protocols = protocols == null ? null : protocols.clone();
    c.cipherSuites = cipherSuites == null ? null : cipherSuites.clone();
    c.needClientAuth = needClientAuth;
    c.wantClientAuth = wantClientAuth;
    c.endpointIdentification = endpointIdentification;
    c.serverNames = serverNames;
    c.sniMatchers = sniMatchers;
    c.applicationProtocols = applicationProtocols.clone();
    c.signatureSchemes = signatureSchemes == null ? null : signatureSchemes.clone();
    c.namedGroups = namedGroups == null ? null : namedGroups.clone();
    c.enableSessionCreation = enableSessionCreation;
    c.useCipherSuitesOrder = useCipherSuitesOrder;
    c.alpnSelector = alpnSelector;
    return c;
  }
}
