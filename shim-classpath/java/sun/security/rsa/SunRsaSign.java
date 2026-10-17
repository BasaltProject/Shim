/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.security.Provider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SunRsaSign extends Provider {
  private static final long serialVersionUID = 866040293550393045L;

  public SunRsaSign() {
    super("SunRsaSign", "25", "Sun RSA signature provider");
//SERVICES
    add("KeyFactory", "RSASSA-PSS", "sun.security.rsa.RSAKeyFactory$PSS", new String[] {"OID.1.2.840.113549.1.1.10", "1.2.840.113549.1.1.10", "PSS"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("KeyPairGenerator", "RSA", "sun.security.rsa.RSAKeyPairGenerator$Legacy", new String[] {"OID.1.2.840.113549.1.1", "1.2.840.113549.1.1", "1.2.840.113549.1.1.1"}, new String[] {});
    add("KeyPairGenerator", "RSASSA-PSS", "sun.security.rsa.RSAKeyPairGenerator$PSS", new String[] {"OID.1.2.840.113549.1.1.10", "1.2.840.113549.1.1.10", "PSS"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "RSASSA-PSS", "sun.security.rsa.RSAPSSSignature", new String[] {"OID.1.2.840.113549.1.1.10", "1.2.840.113549.1.1.10", "PSS"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA224withRSA", "sun.security.rsa.RSASignature$SHA224withRSA", new String[] {"OID.1.2.840.113549.1.1.14", "1.2.840.113549.1.1.14"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA3-224withRSA", "sun.security.rsa.RSASignature$SHA3_224withRSA", new String[] {"OID.2.16.840.1.101.3.4.3.13", "2.16.840.1.101.3.4.3.13"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("AlgorithmParameters", "RSASSA-PSS", "sun.security.rsa.PSSParameters", new String[] {"OID.1.2.840.113549.1.1.10", "1.2.840.113549.1.1.10", "PSS"}, new String[] {});
    add("KeyFactory", "RSA", "sun.security.rsa.RSAKeyFactory$Legacy", new String[] {"OID.1.2.840.113549.1.1", "1.2.840.113549.1.1", "1.2.840.113549.1.1.1"}, new String[] {});
    add("Signature", "SHA1withRSA", "sun.security.rsa.RSASignature$SHA1withRSA", new String[] {"OID.1.2.840.113549.1.1.5", "1.2.840.113549.1.1.5", "1.3.14.3.2.29"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "MD5withRSA", "sun.security.rsa.RSASignature$MD5withRSA", new String[] {"OID.1.2.840.113549.1.1.4", "1.2.840.113549.1.1.4"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "MD2withRSA", "sun.security.rsa.RSASignature$MD2withRSA", new String[] {"OID.1.2.840.113549.1.1.2", "1.2.840.113549.1.1.2"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA256withRSA", "sun.security.rsa.RSASignature$SHA256withRSA", new String[] {"OID.1.2.840.113549.1.1.11", "1.2.840.113549.1.1.11"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA384withRSA", "sun.security.rsa.RSASignature$SHA384withRSA", new String[] {"OID.1.2.840.113549.1.1.12", "1.2.840.113549.1.1.12"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA3-256withRSA", "sun.security.rsa.RSASignature$SHA3_256withRSA", new String[] {"OID.2.16.840.1.101.3.4.3.14", "2.16.840.1.101.3.4.3.14"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA3-384withRSA", "sun.security.rsa.RSASignature$SHA3_384withRSA", new String[] {"OID.2.16.840.1.101.3.4.3.15", "2.16.840.1.101.3.4.3.15"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA3-512withRSA", "sun.security.rsa.RSASignature$SHA3_512withRSA", new String[] {"OID.2.16.840.1.101.3.4.3.16", "2.16.840.1.101.3.4.3.16"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA512/224withRSA", "sun.security.rsa.RSASignature$SHA512_224withRSA", new String[] {"OID.1.2.840.113549.1.1.15", "1.2.840.113549.1.1.15"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA512/256withRSA", "sun.security.rsa.RSASignature$SHA512_256withRSA", new String[] {"OID.1.2.840.113549.1.1.16", "1.2.840.113549.1.1.16"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("Signature", "SHA512withRSA", "sun.security.rsa.RSASignature$SHA512withRSA", new String[] {"OID.1.2.840.113549.1.1.13", "1.2.840.113549.1.1.13"}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
  }

  private void add(String type, String algorithm, String className, String[] aliases, String[] attrs) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < attrs.length; i += 2) {
      map.put(attrs[i], attrs[i + 1]);
    }
    putService(new Provider.Service(this, type, algorithm, className, Arrays.asList(aliases), map));
  }
}
