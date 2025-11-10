/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.AlgorithmParametersSpi;
import java.security.DEREncodable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;
import javax.crypto.spec.PBEKeySpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class EncryptedPrivateKeyInfo implements DEREncodable {
  private static final String kPbes2 = "1.2.840.113549.1.5.13";
  private static final String[][] kNames = {
      {"PBES2", kPbes2},
      {"PBEWithMD5AndDES", "1.2.840.113549.1.5.3"},
      {"PBEWithSHA1AndDESede", "1.2.840.113549.1.12.1.3"},
      {"PBEWithSHA1AndRC2_40", "1.2.840.113549.1.12.1.6"},
      {"PBEWithSHA1AndRC2_128", "1.2.840.113549.1.12.1.5"},
      {"PBEWithSHA1AndRC4_40", "1.2.840.113549.1.12.1.2"},
      {"PBEWithSHA1AndRC4_128", "1.2.840.113549.1.12.1.1"}};

  private final String oid;
  private final String algName;
  private final AlgorithmParameters params;
  private final byte[] encryptedData;
  private final byte[] encoded;

  public EncryptedPrivateKeyInfo(byte[] encoded) throws IOException {
    Objects.requireNonNull(encoded, "the encoded parameter must be non-null");
    this.encoded = encoded.clone();
    DerValue val = new DerValue(this.encoded);
    if (val.getTag() != DerValue.tag_Sequence) {
      throw new IOException("overrun, bytes = " + val.length());
    }
    DerInputStream seq = val.data();
    DerValue algId = seq.getDerValue();
    DerValue data = seq.getDerValue();
    if (seq.available() != 0 || data.getTag() != DerValue.tag_OctetString) {
      throw new IOException("overrun, bytes = " + seq.available());
    }
    DerInputStream algIn = algId.data();
    this.oid = algIn.getDerValue().getOID().toString();
    this.encryptedData = data.getOctetString();
    AlgorithmParameters ap = null;
    String name = nameOf(oid);
    if (algIn.available() != 0) {
      DerValue paramValue = algIn.getDerValue();
      if (paramValue.getTag() != DerValue.tag_Null) {
        byte[] paramBytes = paramValue.toByteArray();
        ap = parametersFor(oid, paramBytes);
        if (oid.equals(kPbes2) && ap != null) {
          name = ap.toString();
        }
      }
    }
    this.params = ap;
    this.algName = name;
  }

  public EncryptedPrivateKeyInfo(String algName, byte[] encryptedData) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algName, "the algName parameter must be non-null");
    String id = oidOf(algName);
    this.oid = id;
    this.algName = algName;
    this.params = null;
    this.encryptedData = checkData(encryptedData);
    this.encoded = encode(id, null, this.encryptedData);
  }

  public EncryptedPrivateKeyInfo(AlgorithmParameters algParams, byte[] encryptedData)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algParams, "algParams must be non-null");
    String id = oidOf(algParams.getAlgorithm());
    this.oid = id;
    this.algName = algParams.getAlgorithm();
    this.params = algParams;
    this.encryptedData = checkData(encryptedData);
    byte[] paramBytes;
    try {
      paramBytes = algParams.getEncoded();
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid parameters", e);
    }
    this.encoded = encode(id, paramBytes, this.encryptedData);
  }

  private EncryptedPrivateKeyInfo(String oid, String algName, AlgorithmParameters params, byte[] data) {
    this.oid = oid;
    this.algName = algName;
    this.params = params;
    this.encryptedData = data.clone();
    byte[] paramBytes = null;
    if (params != null) {
      try {
        paramBytes = params.getEncoded();
      } catch (IOException e) {
        throw new IllegalArgumentException("Invalid parameters", e);
      }
    }
    this.encoded = encode(oid, paramBytes, data);
  }

  private static byte[] checkData(byte[] encryptedData) {
    Objects.requireNonNull(encryptedData, "the encryptedData parameter must be non-null");
    if (encryptedData.length == 0) {
      throw new IllegalArgumentException("the encryptedData parameter must not be empty");
    }
    return encryptedData.clone();
  }

  private static String oidOf(String name) throws NoSuchAlgorithmException {
    for (String[] e : kNames) {
      if (e[0].equalsIgnoreCase(name)) {
        return e[1];
      }
    }
    if (name.matches("[0-9]+(\\.[0-9]+)+")) {
      return name;
    }
    throw new NoSuchAlgorithmException("unrecognized algorithm name: " + name);
  }

  private static String nameOf(String oid) {
    for (String[] e : kNames) {
      if (e[1].equals(oid)) {
        return e[0];
      }
    }
    return AlgorithmId.nameOf(oid);
  }

  private static AlgorithmParameters parametersFor(String oid, byte[] paramBytes) throws IOException {
    try {
      if (oid.equals(kPbes2)) {
        Provider provider = Security.getProvider("SunJCE");
        AlgorithmParametersSpi spi = new com.sun.crypto.provider.PBES2Parameters();
        AlgorithmParameters ap = new AlgorithmParameters(spi, provider, "PBES2") {
        };
        ap.init(paramBytes);
        return ap;
      }
      AlgorithmParameters ap = AlgorithmParameters.getInstance(nameOf(oid));
      ap.init(paramBytes);
      return ap;
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }

  private static byte[] encode(String oid, byte[] paramBytes, byte[] data) {
    DerOutputStream alg = new DerOutputStream();
    alg.putOID(ObjectIdentifier.of(oid));
    if (paramBytes != null) {
      alg.write(paramBytes);
    } else {
      alg.putNull();
    }
    DerOutputStream all = new DerOutputStream();
    all.putDerValue(new DerValue(DerValue.tag_Sequence, alg.toByteArray()));
    all.putOctetString(data);
    try {
      return new DerValue(DerValue.tag_Sequence, all.toByteArray()).toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public String getAlgName() {
    return algName;
  }

  public AlgorithmParameters getAlgParameters() {
    return params;
  }

  public byte[] getEncryptedData() {
    return encryptedData.clone();
  }

  public PKCS8EncodedKeySpec getKeySpec(Cipher cipher) throws InvalidKeySpecException {
    byte[] encoded;
    try {
      encoded = cipher.doFinal(encryptedData);
      checkPKCS8Encoding(encoded);
    } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
      throw new InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec", e);
    }
    return new PKCS8EncodedKeySpec(encoded);
  }

  private static void checkPKCS8Encoding(byte[] encodedKey) throws IOException {
    DerValue val = new DerValue(encodedKey);
    if (val.getTag() != DerValue.tag_Sequence) {
      throw new IOException("invalid key encoding");
    }
    DerInputStream in = val.data();
    if (in.getDerValue().getTag() != DerValue.tag_Integer) {
      throw new IOException("invalid key encoding");
    }
    DerValue alg = in.getDerValue();
    if (alg.getTag() != DerValue.tag_Sequence) {
      throw new IOException("invalid key encoding");
    }
    if (in.getDerValue().getTag() != DerValue.tag_OctetString) {
      throw new IOException("invalid key encoding");
    }
  }

  private PKCS8EncodedKeySpec decrypt(Key decryptKey, Provider provider, String providerName)
      throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
    Objects.requireNonNull(decryptKey);
    Cipher cipher;
    try {
      if (provider != null) {
        cipher = Cipher.getInstance(algName, provider);
      } else if (providerName != null) {
        cipher = Cipher.getInstance(algName, providerName);
      } else {
        cipher = Cipher.getInstance(algName);
      }
    } catch (NoSuchPaddingException e) {
      throw new NoSuchAlgorithmException(e);
    }
    try {
      cipher.init(Cipher.DECRYPT_MODE, decryptKey, params);
      byte[] decrypted = cipher.doFinal(encryptedData);
      checkPKCS8Encoding(decrypted);
      return new PKCS8EncodedKeySpec(decrypted);
    } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | IOException e) {
      throw new IllegalArgumentException("Cannot retrieve the PKCS8EncodedKeySpec", e);
    }
  }

  public PKCS8EncodedKeySpec getKeySpec(Key decryptKey) throws NoSuchAlgorithmException, InvalidKeyException {
    try {
      return decrypt(decryptKey, null, null);
    } catch (NoSuchProviderException e) {
      throw new IllegalStateException(e);
    } catch (IllegalArgumentException e) {
      throw new InvalidKeyException(e.getMessage(), e.getCause());
    }
  }

  public PKCS8EncodedKeySpec getKeySpec(Key decryptKey, String providerName)
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
    Objects.requireNonNull(providerName);
    try {
      return decrypt(decryptKey, null, providerName);
    } catch (IllegalArgumentException e) {
      throw new InvalidKeyException(e.getMessage(), e.getCause());
    }
  }

  public PKCS8EncodedKeySpec getKeySpec(Key decryptKey, Provider provider)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Objects.requireNonNull(provider);
    try {
      return decrypt(decryptKey, provider, null);
    } catch (NoSuchProviderException e) {
      throw new IllegalStateException(e);
    } catch (IllegalArgumentException e) {
      throw new InvalidKeyException(e.getMessage(), e.getCause());
    }
  }

  public byte[] getEncoded() {
    return encoded.clone();
  }

  public PrivateKey getKey(char[] password) throws NoSuchAlgorithmException, InvalidKeyException {
    return getKey(password, null);
  }

  private PrivateKey getKey(char[] password, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
    Objects.requireNonNull(password);
    PBEKeySpec spec = new PBEKeySpec(password);
    try {
      SecretKey key = SecretKeyFactory.getInstance(algName).generateSecret(spec);
      return keyFrom(getKeySpec(key, provider != null ? provider : Security.getProvider("SunJCE")));
    } catch (InvalidKeySpecException e) {
      throw new InvalidKeyException("Cannot retrieve the PrivateKey", e);
    } finally {
      spec.clearPassword();
    }
  }

  public PrivateKey getKey(Key decryptKey, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
    try {
      return keyFrom(getKeySpec(decryptKey, provider));
    } catch (InvalidKeySpecException e) {
      throw new InvalidKeyException("Cannot retrieve the PrivateKey", e);
    }
  }

  private static PrivateKey keyFrom(PKCS8EncodedKeySpec spec) throws NoSuchAlgorithmException, InvalidKeySpecException {
    try {
      DerInputStream in = new DerValue(spec.getEncoded()).data();
      in.getDerValue();
      String oid = in.getDerValue().data().getDerValue().getOID().toString();
      return KeyFactory.getInstance(AlgorithmId.keyAlgorithm(oid)).generatePrivate(spec);
    } catch (IOException e) {
      throw new InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec", e);
    }
  }

  public static EncryptedPrivateKeyInfo encryptKey(PrivateKey key, char[] password) {
    return encryptKey(key, password, "PBEWithHmacSHA256AndAES_128", null, null);
  }

  public static EncryptedPrivateKeyInfo encryptKey(PrivateKey key, char[] password, String algorithm,
      AlgorithmParameterSpec params, Provider provider) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(password);
    Objects.requireNonNull(algorithm);
    PBEKeySpec spec = new PBEKeySpec(password);
    try {
      SecretKey pbeKey = provider != null ? SecretKeyFactory.getInstance(algorithm, provider).generateSecret(spec)
          : SecretKeyFactory.getInstance(algorithm).generateSecret(spec);
      return encryptKey(key, pbeKey, algorithm, params, provider, null);
    } catch (java.security.GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    } finally {
      spec.clearPassword();
    }
  }

  public static EncryptedPrivateKeyInfo encryptKey(PrivateKey key, Key encryptKey, String algorithm,
      AlgorithmParameterSpec params, Provider provider, SecureRandom random) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(encryptKey);
    Objects.requireNonNull(algorithm);
    try {
      Cipher cipher = provider != null ? Cipher.getInstance(algorithm, provider) : Cipher.getInstance(algorithm);
      cipher.init(Cipher.ENCRYPT_MODE, encryptKey, params, random);
      byte[] encrypted = cipher.doFinal(key.getEncoded());
      AlgorithmParameters ap = cipher.getParameters();
      if (ap == null) {
        return new EncryptedPrivateKeyInfo(algorithm, encrypted);
      }
      String oid = algorithm.startsWith("PBEWithHmac") ? kPbes2 : oidOf(algorithm);
      return new EncryptedPrivateKeyInfo(oid, algorithm, ap, encrypted);
    } catch (java.security.GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
