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
    addSoftware("KeyPairGenerator", "DSA", "sun.security.provider.DSAKeyPairGenerator", "OID.1.2.840.10040.4.1",
        "1.2.840.10040.4.1", "1.3.14.3.2.12");
    addSoftware("KeyFactory", "DSA", "sun.security.provider.DSAKeyFactory", "OID.1.2.840.10040.4.1",
        "1.2.840.10040.4.1", "1.3.14.3.2.12");
    addSoftware("AlgorithmParameters", "DSA", "sun.security.provider.DSAParameters", "OID.1.2.840.10040.4.1",
        "1.2.840.10040.4.1", "1.3.14.3.2.12");
    addSoftware("AlgorithmParameterGenerator", "DSA", "sun.security.provider.DSAParameterGenerator");
    addSoftware("Signature", "SHA1withDSA", "sun.security.provider.DSA$SHA1withDSA", "DSA", "DSS", "SHA/DSA",
        "SHA-1/DSA", "SHA1/DSA", "SHAwithDSA", "DSAWithSHA1", "OID.1.2.840.10040.4.3", "1.2.840.10040.4.3",
        "1.3.14.3.2.13", "1.3.14.3.2.27");
    addSoftware("Signature", "SHA224withDSA", "sun.security.provider.DSA$SHA224withDSA", "OID.2.16.840.1.101.3.4.3.1",
        "2.16.840.1.101.3.4.3.1");
    addSoftware("Signature", "SHA256withDSA", "sun.security.provider.DSA$SHA256withDSA", "OID.2.16.840.1.101.3.4.3.2",
        "2.16.840.1.101.3.4.3.2");
    addSoftware("Signature", "SHA384withDSA", "sun.security.provider.DSA$SHA384withDSA", "OID.2.16.840.1.101.3.4.3.3",
        "2.16.840.1.101.3.4.3.3");
    addSoftware("Signature", "SHA512withDSA", "sun.security.provider.DSA$SHA512withDSA", "OID.2.16.840.1.101.3.4.3.4",
        "2.16.840.1.101.3.4.3.4");
    addSoftware("Signature", "NONEwithDSA", "sun.security.provider.DSA$RawDSA", "RawDSA");
    addSoftware("Signature", "SHA1withDSAinP1363Format", "sun.security.provider.DSA$SHA1withDSAinP1363Format");
    addSoftware("Signature", "SHA224withDSAinP1363Format", "sun.security.provider.DSA$SHA224withDSAinP1363Format");
    addSoftware("Signature", "SHA256withDSAinP1363Format", "sun.security.provider.DSA$SHA256withDSAinP1363Format");
    addSoftware("Signature", "SHA384withDSAinP1363Format", "sun.security.provider.DSA$SHA384withDSAinP1363Format");
    addSoftware("Signature", "SHA512withDSAinP1363Format", "sun.security.provider.DSA$SHA512withDSAinP1363Format");
    addSoftware("Signature", "SHA3-224withDSA", "sun.security.provider.DSA$SHA3_224withDSA", "OID.2.16.840.1.101.3.4.3.5", "2.16.840.1.101.3.4.3.5");
    addSoftware("Signature", "SHA3-224withDSAinP1363Format", "sun.security.provider.DSA$SHA3_224withDSAinP1363Format");
    addSoftware("Signature", "SHA3-256withDSA", "sun.security.provider.DSA$SHA3_256withDSA", "OID.2.16.840.1.101.3.4.3.6", "2.16.840.1.101.3.4.3.6");
    addSoftware("Signature", "SHA3-256withDSAinP1363Format", "sun.security.provider.DSA$SHA3_256withDSAinP1363Format");
    addSoftware("Signature", "SHA3-384withDSA", "sun.security.provider.DSA$SHA3_384withDSA", "OID.2.16.840.1.101.3.4.3.7", "2.16.840.1.101.3.4.3.7");
    addSoftware("Signature", "SHA3-384withDSAinP1363Format", "sun.security.provider.DSA$SHA3_384withDSAinP1363Format");
    addSoftware("Signature", "SHA3-512withDSA", "sun.security.provider.DSA$SHA3_512withDSA", "OID.2.16.840.1.101.3.4.3.8", "2.16.840.1.101.3.4.3.8");
    addSoftware("Signature", "SHA3-512withDSAinP1363Format", "sun.security.provider.DSA$SHA3_512withDSAinP1363Format");
    addSoftware("Signature", "NONEwithDSAinP1363Format", "sun.security.provider.DSA$RawDSAinP1363Format");
    addSoftware("KeyPairGenerator", "ML-DSA", "sun.security.provider.MLDSAImpl$KPGAny");
    addSoftware("KeyFactory", "ML-DSA", "sun.security.provider.MLDSAImpl$KFAny");
    addSoftware("Signature", "ML-DSA", "sun.security.provider.MLDSAImpl$SigAny");
    addSoftware("KeyPairGenerator", "ML-DSA-44", "sun.security.provider.MLDSAImpl$KPG44", "OID.2.16.840.1.101.3.4.3.17", "2.16.840.1.101.3.4.3.17");
    addSoftware("KeyFactory", "ML-DSA-44", "sun.security.provider.MLDSAImpl$KF44", "OID.2.16.840.1.101.3.4.3.17", "2.16.840.1.101.3.4.3.17");
    addSoftware("Signature", "ML-DSA-44", "sun.security.provider.MLDSAImpl$Sig44", "OID.2.16.840.1.101.3.4.3.17", "2.16.840.1.101.3.4.3.17");
    addSoftware("KeyPairGenerator", "ML-DSA-65", "sun.security.provider.MLDSAImpl$KPG65", "OID.2.16.840.1.101.3.4.3.18", "2.16.840.1.101.3.4.3.18");
    addSoftware("KeyFactory", "ML-DSA-65", "sun.security.provider.MLDSAImpl$KF65", "OID.2.16.840.1.101.3.4.3.18", "2.16.840.1.101.3.4.3.18");
    addSoftware("Signature", "ML-DSA-65", "sun.security.provider.MLDSAImpl$Sig65", "OID.2.16.840.1.101.3.4.3.18", "2.16.840.1.101.3.4.3.18");
    addSoftware("KeyPairGenerator", "ML-DSA-87", "sun.security.provider.MLDSAImpl$KPG87", "OID.2.16.840.1.101.3.4.3.19", "2.16.840.1.101.3.4.3.19");
    addSoftware("KeyFactory", "ML-DSA-87", "sun.security.provider.MLDSAImpl$KF87", "OID.2.16.840.1.101.3.4.3.19", "2.16.840.1.101.3.4.3.19");
    addSoftware("Signature", "ML-DSA-87", "sun.security.provider.MLDSAImpl$Sig87", "OID.2.16.840.1.101.3.4.3.19", "2.16.840.1.101.3.4.3.19");
    addSoftware("KeyFactory", "HSS/LMS", "sun.security.provider.HSS$KeyFactoryImpl", "OID.1.2.840.113549.1.9.16.3.17", "1.2.840.113549.1.9.16.3.17");
    addSoftware("Signature", "HSS/LMS", "sun.security.provider.HSS$SignatureImpl", "OID.1.2.840.113549.1.9.16.3.17", "1.2.840.113549.1.9.16.3.17");
    addSoftware("CertificateFactory", "X.509", "sun.security.provider.X509Factory", "X509");
    addSoftware("KeyStore", "JKS", "sun.security.provider.JavaKeyStore$DualFormatJKS");
    putService(new Provider.Service(this, "KeyStore", "PKCS12", "sun.security.pkcs12.PKCS12KeyStore$DualFormatPKCS12",
        Collections.<String>emptyList(), new HashMap<String, String>()));
    addSoftware("KeyStore", "DKS", "sun.security.provider.DomainKeyStore$DKS");
    addSoftware("KeyStore", "CaseExactJKS", "sun.security.provider.JavaKeyStore$CaseExactJKS");
    addSoftware("Configuration", "JavaLoginConfig", "sun.security.provider.ConfigFile$Spi");
    addSoftware("CertStore", "Collection", "sun.security.provider.certpath.CollectionCertStore");
    addSoftware("CertStore", "com.sun.security.IndexedCollection", "sun.security.provider.certpath.IndexedCollectionCertStore");
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
