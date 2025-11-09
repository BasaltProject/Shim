/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Sun extends Provider {
  private static final long serialVersionUID = 6440182097568097204L;

  private static final String kInfo = "SUN (DSA key/parameter generation; DSA signing; SHA-1, MD5 digests; "
      + "SecureRandom; X.509 certificates; PKCS12, JKS & DKS keystores; PKIX CertPathValidator; "
      + "PKIX CertPathBuilder; LDAP, Collection CertStores, JavaPolicy Policy; "
      + "JavaLoginConfig Configuration)";

  public Sun() {
    super("SUN", "25", kInfo);
    addRandom("NativePRNG", "sun.security.provider.NativePRNG", false);
    addRandom("DRBG", "sun.security.provider.DRBG", true);
    addRandom("SHA1PRNG", "sun.security.provider.SecureRandom", true);
    addRandom("NativePRNGBlocking", "sun.security.provider.NativePRNG$Blocking", false);
    addRandom("NativePRNGNonBlocking", "sun.security.provider.NativePRNG$NonBlocking", false);
    addDigest("SHA-1", "sun.security.provider.SHA", "OID.1.3.14.3.2.26", "1.3.14.3.2.26", "SHA", "SHA1");
    addDigest("MD2", "sun.security.provider.MD2", "OID.1.2.840.113549.2.2", "1.2.840.113549.2.2");
    addDigest("MD5", "sun.security.provider.MD5", "OID.1.2.840.113549.2.5", "1.2.840.113549.2.5");
    addDigest("SHA-256", "sun.security.provider.SHA2$SHA256", "OID.2.16.840.1.101.3.4.2.1", "2.16.840.1.101.3.4.2.1",
        "SHA256");
    addDigest("SHA-384", "sun.security.provider.SHA5$SHA384", "OID.2.16.840.1.101.3.4.2.2", "2.16.840.1.101.3.4.2.2",
        "SHA384");
    addDigest("SHA-512", "sun.security.provider.SHA5$SHA512", "OID.2.16.840.1.101.3.4.2.3", "2.16.840.1.101.3.4.2.3",
        "SHA512");
    addDigest("SHA-224", "sun.security.provider.SHA2$SHA224", "OID.2.16.840.1.101.3.4.2.4", "2.16.840.1.101.3.4.2.4",
        "SHA224");
    addDigest("SHA-512/224", "sun.security.provider.SHA5$SHA512_224", "OID.2.16.840.1.101.3.4.2.5",
        "2.16.840.1.101.3.4.2.5", "SHA512/224");
    addDigest("SHA-512/256", "sun.security.provider.SHA5$SHA512_256", "OID.2.16.840.1.101.3.4.2.6",
        "2.16.840.1.101.3.4.2.6", "SHA512/256");
    addDigest("SHA3-224", "sun.security.provider.SHA3$SHA224", "OID.2.16.840.1.101.3.4.2.7", "2.16.840.1.101.3.4.2.7");
    addDigest("SHA3-256", "sun.security.provider.SHA3$SHA256", "OID.2.16.840.1.101.3.4.2.8", "2.16.840.1.101.3.4.2.8");
    addDigest("SHA3-384", "sun.security.provider.SHA3$SHA384", "OID.2.16.840.1.101.3.4.2.9", "2.16.840.1.101.3.4.2.9");
    addDigest("SHA3-512", "sun.security.provider.SHA3$SHA512", "OID.2.16.840.1.101.3.4.2.10",
        "2.16.840.1.101.3.4.2.10");
    addDigest("SHAKE128-256", "sun.security.provider.SHA3$SHAKE128Hash", "OID.2.16.840.1.101.3.4.2.11",
        "2.16.840.1.101.3.4.2.11", "SHAKE128");
    addDigest("SHAKE256-512", "sun.security.provider.SHA3$SHAKE256Hash", "OID.2.16.840.1.101.3.4.2.12",
        "2.16.840.1.101.3.4.2.12", "SHAKE256");
    addSoftware("CertificateFactory", "X.509", "sun.security.provider.X509Factory", "X509");
    addSoftware("KeyStore", "JKS", "sun.security.provider.JavaKeyStore$DualFormatJKS");
    putService(new Provider.Service(this, "KeyStore", "PKCS12", "sun.security.pkcs12.PKCS12KeyStore$DualFormatPKCS12",
        Collections.<String>emptyList(), new HashMap<String, String>()));
    addSoftware("KeyStore", "CaseExactJKS", "sun.security.provider.JavaKeyStore$CaseExactJKS");
    addSoftware("CertStore", "Collection", "sun.security.provider.certpath.CollectionCertStore");
    Map<String, String> pkix = new HashMap<String, String>();
    pkix.put("ImplementedIn", "Software");
    pkix.put("ValidationAlgorithm", "RFC5280");
    putService(new Provider.Service(this, "CertPathBuilder", "PKIX",
        "sun.security.provider.certpath.SunCertPathBuilder",
        Collections.<String>emptyList(), pkix));
    putService(new Provider.Service(this, "CertPathValidator", "PKIX",
        "sun.security.provider.certpath.PKIXCertPathValidator", Collections.<String>emptyList(), pkix));
  }

  private void addSoftware(String type, String algorithm, String className, String... aliases) {
    Map<String, String> attrs = new HashMap<String, String>();
    attrs.put("ImplementedIn", "Software");
    putService(new Provider.Service(this, type, algorithm, className, Arrays.asList(aliases), attrs));
  }

  private void addDigest(String algorithm, String className, String... aliases) {
    Map<String, String> attrs = new HashMap<String, String>();
    attrs.put("ImplementedIn", "Software");
    putService(new Provider.Service(this, "MessageDigest", algorithm, className, Arrays.asList(aliases), attrs));
  }

  private void addRandom(String algorithm, String className, boolean software) {
    Map<String, String> attrs = new HashMap<String, String>();
    if (software) {
      attrs.put("ImplementedIn", "Software");
    }
    attrs.put("ThreadSafe", "true");
    putService(new Provider.Service(this, "SecureRandom", algorithm, className, Collections.<String>emptyList(),
        attrs));
  }
}
