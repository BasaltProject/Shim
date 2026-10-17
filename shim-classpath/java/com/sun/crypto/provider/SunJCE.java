/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.Provider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SunJCE extends Provider {
  private static final long serialVersionUID = 6812507587804302833L;

  private static final String kInfo = "SunJCE Provider (implements RSA, DES, Triple DES, AES, Blowfish, ARCFOUR, RC2, PBE, "
      + "Diffie-Hellman, HMAC, ChaCha20, DHKEM, ML-KEM, and HKDF)";

  public SunJCE() {
    super("SunJCE", "25", kInfo);
    add("Mac", "HmacSHA512", "com.sun.crypto.provider.HmacCore$HmacSHA512", new String[] {"OID.1.2.840.113549.2.11", "1.2.840.113549.2.11"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA3-384", "com.sun.crypto.provider.HmacCore$HmacSHA3_384", new String[] {"OID.2.16.840.1.101.3.4.2.15", "2.16.840.1.101.3.4.2.15"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("KeyGenerator", "HmacSHA3-224", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA3_224", new String[] {"OID.2.16.840.1.101.3.4.2.13", "2.16.840.1.101.3.4.2.13"}, new String[] {});
    add("AlgorithmParameters", "AES", "com.sun.crypto.provider.AESParameters", new String[] {"OID.2.16.840.1.101.3.4.1", "2.16.840.1.101.3.4.1"}, new String[] {});
    add("Mac", "HmacSHA384", "com.sun.crypto.provider.HmacCore$HmacSHA384", new String[] {"OID.1.2.840.113549.2.10", "1.2.840.113549.2.10"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA3-512", "com.sun.crypto.provider.HmacCore$HmacSHA3_512", new String[] {"OID.2.16.840.1.101.3.4.2.16", "2.16.840.1.101.3.4.2.16"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("KeyGenerator", "HmacSHA384", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA384", new String[] {"OID.1.2.840.113549.2.10", "1.2.840.113549.2.10"}, new String[] {});
    add("Mac", "HmacSHA512/256", "com.sun.crypto.provider.HmacCore$HmacSHA512_256", new String[] {"OID.1.2.840.113549.2.13", "1.2.840.113549.2.13"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA3-224", "com.sun.crypto.provider.HmacCore$HmacSHA3_224", new String[] {"OID.2.16.840.1.101.3.4.2.13", "2.16.840.1.101.3.4.2.13"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA512/224", "com.sun.crypto.provider.HmacCore$HmacSHA512_224", new String[] {"OID.1.2.840.113549.2.12", "1.2.840.113549.2.12"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA3-256", "com.sun.crypto.provider.HmacCore$HmacSHA3_256", new String[] {"OID.2.16.840.1.101.3.4.2.14", "2.16.840.1.101.3.4.2.14"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("KeyGenerator", "HmacSHA512/224", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA512_224", new String[] {"OID.1.2.840.113549.2.12", "1.2.840.113549.2.12"}, new String[] {});
    add("KeyGenerator", "HmacSHA512", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA512", new String[] {"OID.1.2.840.113549.2.11", "1.2.840.113549.2.11"}, new String[] {});
    add("KeyGenerator", "HmacSHA512/256", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA512_256", new String[] {"OID.1.2.840.113549.2.13", "1.2.840.113549.2.13"}, new String[] {});
    add("KeyGenerator", "HmacSHA3-512", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA3_512", new String[] {"OID.2.16.840.1.101.3.4.2.16", "2.16.840.1.101.3.4.2.16"}, new String[] {});
    add("KeyGenerator", "HmacSHA3-256", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA3_256", new String[] {"OID.2.16.840.1.101.3.4.2.14", "2.16.840.1.101.3.4.2.14"}, new String[] {});
    add("KeyGenerator", "HmacSHA3-384", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA3_384", new String[] {"OID.2.16.840.1.101.3.4.2.15", "2.16.840.1.101.3.4.2.15"}, new String[] {});
    add("Cipher", "AES_192/OFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_OFB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.23", "2.16.840.1.101.3.4.1.23"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "AES_192/CFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_CFB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.24", "2.16.840.1.101.3.4.1.24"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "AES_192/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AES192", new String[] {"OID.2.16.840.1.101.3.4.1.26", "2.16.840.1.101.3.4.1.26"}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "GCM"});
    add("Cipher", "AES_192/ECB/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_ECB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.21", "2.16.840.1.101.3.4.1.21"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA1", "com.sun.crypto.provider.PBKDF2Core$HmacSHA1", new String[] {"OID.1.2.840.113549.1.5.12", "1.2.840.113549.1.5.12"}, new String[] {});
    add("Cipher", "AES_192/CBC/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_CBC_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.22", "2.16.840.1.101.3.4.1.22"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "DESede", "com.sun.crypto.provider.DESedeCipher", new String[] {"TripleDES"}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64", "SupportedPaddings", "NOPADDING|PKCS5PADDING|ISO10126PADDING"});
    add("AlgorithmParameters", "OAEP", "com.sun.crypto.provider.OAEPParameters", new String[] {"OID.1.2.840.113549.1.1.7", "1.2.840.113549.1.1.7"}, new String[] {});
    add("Cipher", "AES_256/ECB/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_ECB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.41", "2.16.840.1.101.3.4.1.41"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA384", "com.sun.crypto.provider.PBKDF2Core$HmacSHA384", new String[] {}, new String[] {});
    add("AlgorithmParameters", "DESede", "com.sun.crypto.provider.DESedeParameters", new String[] {"TripleDES"}, new String[] {});
    add("Cipher", "AES_256/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AES256", new String[] {"OID.2.16.840.1.101.3.4.1.46", "2.16.840.1.101.3.4.1.46"}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "GCM"});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA512/256", "com.sun.crypto.provider.PBKDF2Core$HmacSHA512_256", new String[] {}, new String[] {});
    add("Cipher", "AES", "com.sun.crypto.provider.AESCipher$General", new String[] {"OID.2.16.840.1.101.3.4.1", "2.16.840.1.101.3.4.1"}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "ECB|CBC|PCBC|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64|CFB72|CFB80|CFB88|CFB96|CFB104|CFB112|CFB120|CFB128|OFB72|OFB80|OFB88|OFB96|OFB104|OFB112|OFB120|OFB128", "SupportedPaddings", "NOPADDING|PKCS5PADDING|ISO10126PADDING"});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA256", "com.sun.crypto.provider.PBKDF2Core$HmacSHA256", new String[] {}, new String[] {});
    add("Cipher", "RSA", "com.sun.crypto.provider.RSACipher", new String[] {}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey", "SupportedModes", "ECB", "SupportedPaddings", "NOPADDING|PKCS1PADDING|OAEPPADDING|OAEPWITHMD5ANDMGF1PADDING|OAEPWITHSHA1ANDMGF1PADDING|OAEPWITHSHA-1ANDMGF1PADDING|OAEPWITHSHA-224ANDMGF1PADDING|OAEPWITHSHA-256ANDMGF1PADDING|OAEPWITHSHA-384ANDMGF1PADDING|OAEPWITHSHA-512ANDMGF1PADDING|OAEPWITHSHA-512/224ANDMGF1PADDING|OAEPWITHSHA-512/256ANDMGF1PADDING"});
    add("KeyGenerator", "HmacSHA224", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA224", new String[] {"OID.1.2.840.113549.2.8", "1.2.840.113549.2.8"}, new String[] {});
    add("KeyGenerator", "HmacSHA256", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA256", new String[] {"OID.1.2.840.113549.2.9", "1.2.840.113549.2.9"}, new String[] {});
    add("Cipher", "AES_128/CFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_CFB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.4", "2.16.840.1.101.3.4.1.4"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("KeyGenerator", "HmacSHA1", "com.sun.crypto.provider.HmacKeyGenerators$HmacSHA1", new String[] {"OID.1.2.840.113549.2.7", "1.2.840.113549.2.7"}, new String[] {});
    add("KeyGenerator", "AES", "com.sun.crypto.provider.AESKeyGenerator", new String[] {"OID.2.16.840.1.101.3.4.1", "2.16.840.1.101.3.4.1"}, new String[] {});
    add("Cipher", "AES_128/OFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_OFB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.3", "2.16.840.1.101.3.4.1.3"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "AES_256/CBC/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_CBC_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.42", "2.16.840.1.101.3.4.1.42"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA224", "com.sun.crypto.provider.HmacCore$HmacSHA224", new String[] {"OID.1.2.840.113549.2.8", "1.2.840.113549.2.8"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA1", "com.sun.crypto.provider.HmacCore$HmacSHA1", new String[] {"OID.1.2.840.113549.2.7", "1.2.840.113549.2.7"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Mac", "HmacSHA256", "com.sun.crypto.provider.HmacCore$HmacSHA256", new String[] {"OID.1.2.840.113549.2.9", "1.2.840.113549.2.9"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA512/224", "com.sun.crypto.provider.PBKDF2Core$HmacSHA512_224", new String[] {}, new String[] {});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA224", "com.sun.crypto.provider.PBKDF2Core$HmacSHA224", new String[] {}, new String[] {});
    add("KeyGenerator", "HmacMD5", "com.sun.crypto.provider.HmacKeyGenerators$HmacMD5", new String[] {}, new String[] {});
    add("Signature", "NONEwithRSA", "com.sun.crypto.provider.RSACipherAdaptor", new String[] {}, new String[] {"SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey"});
    add("KeyGenerator", "DES", "com.sun.crypto.provider.DESKeyGenerator", new String[] {}, new String[] {});
    add("Cipher", "AES_128/CBC/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_CBC_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.2", "2.16.840.1.101.3.4.1.2"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "AES_256/OFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_OFB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.43", "2.16.840.1.101.3.4.1.43"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "AES_256/CFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_CFB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.44", "2.16.840.1.101.3.4.1.44"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("SecretKeyFactory", "DES", "com.sun.crypto.provider.DESKeyFactory", new String[] {}, new String[] {});
    add("Cipher", "AES_128/ECB/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_ECB_NoPadding", new String[] {"OID.2.16.840.1.101.3.4.1.1", "2.16.840.1.101.3.4.1.1"}, new String[] {"SupportedKeyFormats", "RAW"});
    add("Cipher", "AES_128/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AES128", new String[] {"OID.2.16.840.1.101.3.4.1.6", "2.16.840.1.101.3.4.1.6"}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "GCM"});
    add("SecretKeyFactory", "PBKDF2WithHmacSHA512", "com.sun.crypto.provider.PBKDF2Core$HmacSHA512", new String[] {}, new String[] {});
    add("Cipher", "AES/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AESGCM", new String[] {}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "GCM"});
    add("SecretKeyFactory", "DESede", "com.sun.crypto.provider.DESedeKeyFactory", new String[] {"TripleDES"}, new String[] {});
    add("AlgorithmParameters", "DES", "com.sun.crypto.provider.DESParameters", new String[] {}, new String[] {});
    add("Cipher", "DES", "com.sun.crypto.provider.DESCipher", new String[] {}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64", "SupportedPaddings", "NOPADDING|PKCS5PADDING|ISO10126PADDING"});
    add("Cipher", "RC2", "com.sun.crypto.provider.RC2Cipher", new String[] {}, new String[] {"SupportedKeyFormats", "RAW", "SupportedModes", "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64", "SupportedPaddings", "NOPADDING|PKCS5PADDING|ISO10126PADDING"});
    add("Mac", "HmacMD5", "com.sun.crypto.provider.HmacCore$HmacMD5", new String[] {}, new String[] {"SupportedKeyFormats", "RAW"});
    add("KeyGenerator", "DESede", "com.sun.crypto.provider.DESedeKeyGenerator", new String[] {"TripleDES"}, new String[] {});
    add("AlgorithmParameters", "GCM", "com.sun.crypto.provider.GCMParameters", new String[] {}, new String[] {});
    add("Cipher", "AES/CTR/NoPadding", "com.sun.crypto.provider.AESCipher$AES_CTR_NoPadding", new String[] {}, new String[] {"SupportedKeyFormats", "RAW"});
  }

  private void add(String type, String algorithm, String className, String[] aliases, String[] attrs) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < attrs.length; i += 2) {
      map.put(attrs[i], attrs[i + 1]);
    }
    putService(new Provider.Service(this, type, algorithm, className, Arrays.asList(aliases), map));
  }
}
