/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.security.Provider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SunEC extends Provider {
  private static final long serialVersionUID = -2279741672933606418L;

  public SunEC() {
    super("SunEC", "25", "Sun Elliptic Curve provider");
//SERVICES
    add("KeyPairGenerator", "EC", "sun.security.ec.ECKeyPairGenerator", new String[] {"OID.1.2.840.10045.2.1", "1.2.840.10045.2.1", "EllipticCurve"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("KeyFactory", "EC", "sun.security.ec.ECKeyFactory", new String[] {"OID.1.2.840.10045.2.1", "1.2.840.10045.2.1", "EllipticCurve"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("AlgorithmParameters", "EC", "sun.security.ec.ECParameters", new String[] {"OID.1.2.840.10045.2.1", "1.2.840.10045.2.1", "EllipticCurve"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedCurves", "[secp256r1,NIST P-256,X9.62 prime256v1,1.2.840.10045.3.1.7]|[secp384r1,NIST P-384,1.3.132.0.34]|[secp521r1,NIST P-521,1.3.132.0.35]", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA256withECDSA", "sun.security.ec.ECDSASignature$SHA256withECDSA", new String[] {"OID.1.2.840.10045.4.3.2", "1.2.840.10045.4.3.2"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA3-512withECDSA", "sun.security.ec.ECDSASignature$SHA3_512withECDSA", new String[] {"OID.2.16.840.1.101.3.4.3.12", "2.16.840.1.101.3.4.3.12"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA224withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA224withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA256withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA256withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA3-224withECDSA", "sun.security.ec.ECDSASignature$SHA3_224withECDSA", new String[] {"OID.2.16.840.1.101.3.4.3.9", "2.16.840.1.101.3.4.3.9"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA3-256withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_256withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA3-384withECDSA", "sun.security.ec.ECDSASignature$SHA3_384withECDSA", new String[] {"OID.2.16.840.1.101.3.4.3.11", "2.16.840.1.101.3.4.3.11"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA224withECDSA", "sun.security.ec.ECDSASignature$SHA224withECDSA", new String[] {"OID.1.2.840.10045.4.3.1", "1.2.840.10045.4.3.1"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA384withECDSA", "sun.security.ec.ECDSASignature$SHA384withECDSA", new String[] {"OID.1.2.840.10045.4.3.3", "1.2.840.10045.4.3.3"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA512withECDSA", "sun.security.ec.ECDSASignature$SHA512withECDSA", new String[] {"OID.1.2.840.10045.4.3.4", "1.2.840.10045.4.3.4"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA3-256withECDSA", "sun.security.ec.ECDSASignature$SHA3_256withECDSA", new String[] {"OID.2.16.840.1.101.3.4.3.10", "2.16.840.1.101.3.4.3.10"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA3-384withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_384withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "NONEwithECDSA", "sun.security.ec.ECDSASignature$RawECDSA", new String[] {}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "NONEwithECDSAinP1363Format", "sun.security.ec.ECDSASignature$RawECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA3-224withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_224withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA3-512withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA3_512withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA384withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA384withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA512withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA512withECDSAinP1363Format", new String[] {}, new String[] {});
    add("Signature", "SHA1withECDSA", "sun.security.ec.ECDSASignature$SHA1withECDSA", new String[] {"OID.1.2.840.10045.4.1", "1.2.840.10045.4.1"}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("KeyAgreement", "ECDH", "sun.security.ec.ECDHKeyAgreement", new String[] {}, new String[] {"ImplementedIn", "Software", "KeySize", "256", "SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey"});
    add("Signature", "SHA1withECDSAinP1363Format", "sun.security.ec.ECDSASignature$SHA1withECDSAinP1363Format", new String[] {}, new String[] {});
  }

  private void add(String type, String algorithm, String className, String[] aliases, String[] attrs) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < attrs.length; i += 2) {
      map.put(attrs[i], attrs[i + 1]);
    }
    putService(new Provider.Service(this, type, algorithm, className, Arrays.asList(aliases), map));
  }
}
