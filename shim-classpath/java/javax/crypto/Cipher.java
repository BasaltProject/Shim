/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;

public class Cipher {
  public static final int ENCRYPT_MODE = 1;
  public static final int DECRYPT_MODE = 2;
  public static final int WRAP_MODE = 3;
  public static final int UNWRAP_MODE = 4;
  public static final int PUBLIC_KEY = 1;
  public static final int PRIVATE_KEY = 2;
  public static final int SECRET_KEY = 3;

  private Provider provider;
  private CipherSpi spi;
  private final String transformation;
  private boolean initialized = false;
  private int opmode = 0;

  private final Object lock;
  private Service firstService;
  private Iterator<Service> serviceIterator;
  private List<Transform> transforms;

  private static final int S_NO = 0;
  private static final int S_MAYBE = 1;
  private static final int S_YES = 2;

  private static final int I_KEY = 1;
  private static final int I_PARAMSPEC = 2;
  private static final int I_PARAMS = 3;
  private static final int I_CERT = 4;

  protected Cipher(CipherSpi cipherSpi, Provider provider, String transformation) {
    if (cipherSpi == null) {
      throw new NullPointerException();
    }
    this.spi = cipherSpi;
    this.provider = provider;
    this.transformation = transformation;
    this.lock = null;
  }

  Cipher(CipherSpi cipherSpi, String transformation) {
    this.spi = cipherSpi;
    this.transformation = transformation;
    this.lock = null;
  }

  private Cipher(CipherSpi firstSpi, Service firstService, Iterator<Service> serviceIterator, String transformation,
      List<Transform> transforms) {
    this.spi = firstSpi;
    this.firstService = firstService;
    this.serviceIterator = serviceIterator;
    this.transforms = transforms;
    this.transformation = transformation;
    this.lock = new Object();
  }

  private static String[] tokenizeTransformation(String transformation) throws NoSuchAlgorithmException {
    if (transformation == null) {
      throw new NoSuchAlgorithmException("No transformation given");
    }
    if (transformation.matches("PBEWithHmacSHA512/(224|256)AndAES_(128|256)")) {
      return new String[] {transformation, null, null};
    }
    String[] tokens = transformation.split("/", 3);
    String[] parts = new String[3];
    int count = tokens.length;
    for (int i = 0; i < count; i++) {
      parts[i] = tokens[i].trim();
    }
    if (parts[0] == null || parts[0].isEmpty()) {
      throw new NoSuchAlgorithmException("Invalid transformation: algorithm not specified-" + transformation);
    }
    if (tokens.length == 2) {
      throw new NoSuchAlgorithmException("Invalid transformation format:" + transformation);
    }
    if (tokens.length >= 3 && (parts[1].isEmpty() || parts[2].isEmpty())) {
      throw new NoSuchAlgorithmException("Invalid transformation: missing mode and/or padding-" + transformation);
    }
    return parts;
  }

  private static List<Transform> getTransforms(String transformation) throws NoSuchAlgorithmException {
    String[] parts = tokenizeTransformation(transformation);
    String alg = parts[0];
    String mode = parts[1];
    String pad = parts[2];
    if ((mode != null) && (mode.length() == 0)) {
      mode = null;
    }
    if ((pad != null) && (pad.length() == 0)) {
      pad = null;
    }
    if ((mode == null) && (pad == null)) {
      Transform tr = new Transform(alg, "", null, null);
      return Collections.singletonList(tr);
    }
    List<Transform> list = new ArrayList<Transform>(4);
    list.add(new Transform(alg, "/" + mode + "/" + pad, null, null));
    list.add(new Transform(alg, "/" + mode, null, pad));
    list.add(new Transform(alg, "//" + pad, mode, null));
    list.add(new Transform(alg, "", mode, pad));
    return list;
  }

  private static final class Transform {
    final String transform;
    final String suffix;
    final String mode;
    final String pad;

    Transform(String alg, String suffix, String mode, String pad) {
      this.transform = alg + suffix;
      this.suffix = suffix.toUpperCase(Locale.ENGLISH);
      this.mode = (mode == null) ? null : mode.toUpperCase(Locale.ENGLISH);
      this.pad = (pad == null) ? null : pad.toUpperCase(Locale.ENGLISH);
    }

    void setModePadding(CipherSpi spi) throws NoSuchAlgorithmException, NoSuchPaddingException {
      if (mode != null) {
        spi.engineSetMode(mode);
      }
      if (pad != null) {
        spi.engineSetPadding(pad);
      }
    }

    int supportsModePadding(Service s) {
      int smode = supportsMode(s);
      if (smode == S_NO) {
        return smode;
      }
      int spad = supportsPadding(s);
      return Math.min(smode, spad);
    }

    int supportsMode(Service s) {
      return supports(s, "SupportedModes", mode);
    }

    int supportsPadding(Service s) {
      return supports(s, "SupportedPaddings", pad);
    }

    private static int supports(Service s, String attrName, String value) {
      if (value == null) {
        return S_YES;
      }
      String regexp = s.getAttribute(attrName);
      if (regexp == null) {
        return S_MAYBE;
      }
      return matches(regexp, value) ? S_YES : S_NO;
    }

    private static boolean matches(String regexp, String str) {
      for (String alt : regexp.split("\\|")) {
        if (alt.equals(str)) {
          return true;
        }
      }
      return false;
    }
  }

  private static Transform getTransform(Service s, List<Transform> transforms) {
    String alg = s.getAlgorithm().toUpperCase(Locale.ENGLISH);
    for (Transform tr : transforms) {
      if (alg.endsWith(tr.suffix)) {
        return tr;
      }
    }
    return null;
  }

  public static final Cipher getInstance(String transformation) throws NoSuchAlgorithmException,
      NoSuchPaddingException {
    if ((transformation == null) || transformation.isEmpty()) {
      throw new NoSuchAlgorithmException("Null or empty transformation");
    }
    List<Transform> transforms = getTransforms(transformation);
    List<Service> services = new ArrayList<Service>();
    for (Transform transform : transforms) {
      Iterator<Service> it = GetInstance.getServices("Cipher", transform.transform);
      while (it.hasNext()) {
        Service s = it.next();
        if (!services.contains(s)) {
          services.add(s);
        }
      }
    }
    Iterator<Service> t = services.iterator();
    Exception failure = null;
    while (t.hasNext()) {
      Service s = t.next();
      Transform tr = getTransform(s, transforms);
      if (tr == null) {
        continue;
      }
      int canuse = tr.supportsModePadding(s);
      if (canuse == S_NO) {
        continue;
      }
      if (canuse == S_MAYBE) {
        try {
          CipherSpi spi = (CipherSpi) s.newInstance(null);
          tr.setModePadding(spi);
          Cipher cipher = new Cipher(spi, transformation);
          cipher.provider = s.getProvider();
          return cipher;
        } catch (Exception e) {
          failure = e;
        }
      } else {
        return new Cipher(null, s, t, transformation, transforms);
      }
    }
    throw new NoSuchAlgorithmException("Cannot find any provider supporting " + transformation, failure);
  }

  public static final Cipher getInstance(String transformation, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
    if ((transformation == null) || transformation.isEmpty()) {
      throw new NoSuchAlgorithmException("Null or empty transformation");
    }
    if ((provider == null) || (provider.length() == 0)) {
      throw new IllegalArgumentException("Missing provider");
    }
    Provider p = Providers.named(provider);
    if (p == null) {
      throw new NoSuchProviderException("No such provider: " + provider);
    }
    return getInstance(transformation, p);
  }

  public static final Cipher getInstance(String transformation, Provider provider)
      throws NoSuchAlgorithmException, NoSuchPaddingException {
    if ((transformation == null) || transformation.isEmpty()) {
      throw new NoSuchAlgorithmException("Null or empty transformation");
    }
    if (provider == null) {
      throw new IllegalArgumentException("Missing provider");
    }
    Exception failure = null;
    List<Transform> transforms = getTransforms(transformation);
    boolean providerChecked = false;
    String paddingError = null;
    for (Transform tr : transforms) {
      Service s = provider.getService("Cipher", tr.transform);
      if (s == null) {
        continue;
      }
      if (!providerChecked) {
        providerChecked = true;
      }
      if (tr.supportsMode(s) == S_NO) {
        continue;
      }
      if (tr.supportsPadding(s) == S_NO) {
        paddingError = tr.pad;
        continue;
      }
      try {
        CipherSpi spi = (CipherSpi) s.newInstance(null);
        tr.setModePadding(spi);
        Cipher cipher = new Cipher(spi, transformation);
        cipher.provider = s.getProvider();
        return cipher;
      } catch (Exception e) {
        failure = e;
      }
    }
    if (failure instanceof NoSuchPaddingException) {
      throw (NoSuchPaddingException) failure;
    }
    if (paddingError != null) {
      throw new NoSuchPaddingException("Padding not supported: " + paddingError);
    }
    throw new NoSuchAlgorithmException("No such algorithm: " + transformation, failure);
  }

  void chooseFirstProvider() {
    if ((spi != null) || (serviceIterator == null)) {
      return;
    }
    synchronized (lock) {
      if (spi != null) {
        return;
      }
      Exception lastException = null;
      while ((firstService != null) || serviceIterator.hasNext()) {
        Service s;
        if (firstService != null) {
          s = firstService;
          firstService = null;
        } else {
          s = serviceIterator.next();
        }
        Transform tr = getTransform(s, transforms);
        if (tr == null) {
          continue;
        }
        if (tr.supportsModePadding(s) == S_NO) {
          continue;
        }
        try {
          CipherSpi thisSpi = (CipherSpi) s.newInstance(null);
          tr.setModePadding(thisSpi);
          spi = thisSpi;
          provider = s.getProvider();
          firstService = null;
          serviceIterator = null;
          transforms = null;
          return;
        } catch (Exception e) {
          lastException = e;
        }
      }
      ProviderException e = new ProviderException("Could not construct CipherSpi instance");
      if (lastException != null) {
        e.initCause(lastException);
      }
      throw e;
    }
  }

  private void implInit(CipherSpi thisSpi, int type, int opmode, Key key, AlgorithmParameterSpec paramSpec,
      AlgorithmParameters params, SecureRandom random) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    switch (type) {
    case I_KEY:
      thisSpi.engineInit(opmode, key, random);
      break;
    case I_PARAMSPEC:
      thisSpi.engineInit(opmode, key, paramSpec, random);
      break;
    case I_PARAMS:
      thisSpi.engineInit(opmode, key, params, random);
      break;
    case I_CERT:
      thisSpi.engineInit(opmode, key, random);
      break;
    default:
      throw new AssertionError("Internal Cipher error: " + type);
    }
  }

  private void chooseProvider(int initType, int opmode, Key key, AlgorithmParameterSpec paramSpec,
      AlgorithmParameters params, SecureRandom random) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    synchronized (lock) {
      if (spi != null) {
        implInit(spi, initType, opmode, key, paramSpec, params, random);
        return;
      }
      Exception lastException = null;
      while ((firstService != null) || serviceIterator.hasNext()) {
        Service s;
        if (firstService != null) {
          s = firstService;
          firstService = null;
        } else {
          s = serviceIterator.next();
        }
        if (!s.supportsParameter(key)) {
          continue;
        }
        Transform tr = getTransform(s, transforms);
        if (tr == null) {
          continue;
        }
        if (tr.supportsModePadding(s) == S_NO) {
          continue;
        }
        try {
          CipherSpi thisSpi = (CipherSpi) s.newInstance(null);
          tr.setModePadding(thisSpi);
          implInit(thisSpi, initType, opmode, key, paramSpec, params, random);
          provider = s.getProvider();
          this.spi = thisSpi;
          firstService = null;
          serviceIterator = null;
          transforms = null;
          return;
        } catch (Exception e) {
          if (lastException == null) {
            lastException = e;
          }
        }
      }
      if (lastException instanceof InvalidKeyException) {
        throw (InvalidKeyException) lastException;
      }
      if (lastException instanceof InvalidAlgorithmParameterException) {
        throw (InvalidAlgorithmParameterException) lastException;
      }
      if (lastException instanceof RuntimeException) {
        throw (RuntimeException) lastException;
      }
      String kName = (key != null) ? key.getClass().getName() : "(null)";
      throw new InvalidKeyException("No installed provider supports this key: " + kName, lastException);
    }
  }

  public final Provider getProvider() {
    chooseFirstProvider();
    return this.provider;
  }

  public final String getAlgorithm() {
    return this.transformation;
  }

  public final int getBlockSize() {
    chooseFirstProvider();
    return spi.engineGetBlockSize();
  }

  public final int getOutputSize(int inputLen) {
    if (!initialized && !(this instanceof NullCipher)) {
      throw new IllegalStateException("Cipher not initialized");
    }
    if (inputLen < 0) {
      throw new IllegalArgumentException("Input size must be equal to or greater than zero");
    }
    chooseFirstProvider();
    return spi.engineGetOutputSize(inputLen);
  }

  public final byte[] getIV() {
    chooseFirstProvider();
    return spi.engineGetIV();
  }

  public final AlgorithmParameters getParameters() {
    chooseFirstProvider();
    return spi.engineGetParameters();
  }

  public final ExemptionMechanism getExemptionMechanism() {
    chooseFirstProvider();
    return null;
  }

  private static void checkOpmode(int opmode) {
    if ((opmode < ENCRYPT_MODE) || (opmode > UNWRAP_MODE)) {
      throw new InvalidParameterException("Invalid operation mode");
    }
  }

  private static String getOpmodeString(int opmode) {
    switch (opmode) {
    case ENCRYPT_MODE:
      return "encryption";
    case DECRYPT_MODE:
      return "decryption";
    case WRAP_MODE:
      return "key wrapping";
    case UNWRAP_MODE:
      return "key unwrapping";
    default:
      return "not initialized";
    }
  }

  public final void init(int opmode, Key key) throws InvalidKeyException {
    init(opmode, key, new SecureRandom());
  }

  public final void init(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
    initialized = false;
    checkOpmode(opmode);
    if (spi != null) {
      spi.engineInit(opmode, key, random);
    } else {
      try {
        chooseProvider(I_KEY, opmode, key, null, null, random);
      } catch (InvalidAlgorithmParameterException e) {
        throw new InvalidKeyException(e);
      }
    }
    initialized = true;
    this.opmode = opmode;
  }

  public final void init(int opmode, Key key, AlgorithmParameterSpec params) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    init(opmode, key, params, new SecureRandom());
  }

  public final void init(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    initialized = false;
    checkOpmode(opmode);
    if (spi != null) {
      spi.engineInit(opmode, key, params, random);
    } else {
      chooseProvider(I_PARAMSPEC, opmode, key, params, null, random);
    }
    initialized = true;
    this.opmode = opmode;
  }

  public final void init(int opmode, Key key, AlgorithmParameters params) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    init(opmode, key, params, new SecureRandom());
  }

  public final void init(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    initialized = false;
    checkOpmode(opmode);
    if (spi != null) {
      spi.engineInit(opmode, key, params, random);
    } else {
      chooseProvider(I_PARAMS, opmode, key, null, params, random);
    }
    initialized = true;
    this.opmode = opmode;
  }

  public final void init(int opmode, java.security.cert.Certificate certificate) throws InvalidKeyException {
    init(opmode, certificate, new SecureRandom());
  }

  public final void init(int opmode, java.security.cert.Certificate certificate, SecureRandom random)
      throws InvalidKeyException {
    initialized = false;
    checkOpmode(opmode);
    PublicKey publicKey = certificate.getPublicKey();
    if (spi != null) {
      spi.engineInit(opmode, publicKey, random);
    } else {
      try {
        chooseProvider(I_CERT, opmode, publicKey, null, null, random);
      } catch (InvalidAlgorithmParameterException e) {
        throw new InvalidKeyException(e);
      }
    }
    initialized = true;
    this.opmode = opmode;
  }

  private void checkCipherState() {
    if (!(this instanceof NullCipher)) {
      if (!initialized) {
        throw new IllegalStateException("Cipher not initialized");
      }
      if ((opmode != Cipher.ENCRYPT_MODE) && (opmode != Cipher.DECRYPT_MODE)) {
        throw new IllegalStateException("Cipher not initialized for encryption/decryption");
      }
    }
  }

  public final byte[] update(byte[] input) {
    checkCipherState();
    if (input == null) {
      throw new IllegalArgumentException("Null input buffer");
    }
    chooseFirstProvider();
    if (input.length == 0) {
      return null;
    }
    return spi.engineUpdate(input, 0, input.length);
  }

  public final byte[] update(byte[] input, int inputOffset, int inputLen) {
    checkCipherState();
    if (input == null || inputOffset < 0 || inputLen > (input.length - inputOffset) || inputLen < 0) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    if (inputLen == 0) {
      return null;
    }
    return spi.engineUpdate(input, inputOffset, inputLen);
  }

  public final int update(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException {
    checkCipherState();
    if (input == null || inputOffset < 0 || inputLen > (input.length - inputOffset) || inputLen < 0) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    if (inputLen == 0) {
      return 0;
    }
    return spi.engineUpdate(input, inputOffset, inputLen, output, 0);
  }

  public final int update(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    checkCipherState();
    if (input == null || inputOffset < 0 || inputLen > (input.length - inputOffset) || inputLen < 0
        || outputOffset < 0) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    if (inputLen == 0) {
      return 0;
    }
    return spi.engineUpdate(input, inputOffset, inputLen, output, outputOffset);
  }

  public final int update(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
    checkCipherState();
    if ((input == null) || (output == null)) {
      throw new IllegalArgumentException("Buffers must not be null");
    }
    if (input == output) {
      throw new IllegalArgumentException(
          "Input and output buffers must not be the same object, consider using buffer.duplicate()");
    }
    if (output.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    chooseFirstProvider();
    return spi.engineUpdate(input, output);
  }

  public final byte[] doFinal() throws IllegalBlockSizeException, BadPaddingException {
    checkCipherState();
    chooseFirstProvider();
    return spi.engineDoFinal(null, 0, 0);
  }

  public final int doFinal(byte[] output, int outputOffset) throws IllegalBlockSizeException, ShortBufferException,
      BadPaddingException {
    checkCipherState();
    if ((output == null) || (outputOffset < 0)) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    return spi.engineDoFinal(null, 0, 0, output, outputOffset);
  }

  public final byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException {
    checkCipherState();
    if (input == null) {
      throw new IllegalArgumentException("Null input buffer");
    }
    chooseFirstProvider();
    return spi.engineDoFinal(input, 0, input.length);
  }

  public final byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    checkCipherState();
    if (input == null || inputOffset < 0 || inputLen > (input.length - inputOffset) || inputLen < 0) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    return spi.engineDoFinal(input, inputOffset, inputLen);
  }

  public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException,
      IllegalBlockSizeException, BadPaddingException {
    checkCipherState();
    if (input == null || inputOffset < 0 || inputLen > (input.length - inputOffset) || inputLen < 0) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    return spi.engineDoFinal(input, inputOffset, inputLen, output, 0);
  }

  public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    checkCipherState();
    if (input == null || inputOffset < 0 || inputLen > (input.length - inputOffset) || inputLen < 0
        || outputOffset < 0) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    return spi.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
  }

  public final int doFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException,
      IllegalBlockSizeException, BadPaddingException {
    checkCipherState();
    if ((input == null) || (output == null)) {
      throw new IllegalArgumentException("Buffers must not be null");
    }
    if (input == output) {
      throw new IllegalArgumentException(
          "Input and output buffers must not be the same object, consider using buffer.duplicate()");
    }
    if (output.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    chooseFirstProvider();
    return spi.engineDoFinal(input, output);
  }

  public final byte[] wrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    if (!(this instanceof NullCipher)) {
      if (!initialized) {
        throw new IllegalStateException("Cipher not initialized");
      }
      if (opmode != Cipher.WRAP_MODE) {
        throw new IllegalStateException("Cipher not initialized for wrapping keys");
      }
    }
    chooseFirstProvider();
    return spi.engineWrap(key);
  }

  public final Key unwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    if (!(this instanceof NullCipher)) {
      if (!initialized) {
        throw new IllegalStateException("Cipher not initialized");
      }
      if (opmode != Cipher.UNWRAP_MODE) {
        throw new IllegalStateException("Cipher not initialized for unwrapping keys");
      }
    }
    if ((wrappedKeyType != SECRET_KEY) && (wrappedKeyType != PRIVATE_KEY) && (wrappedKeyType != PUBLIC_KEY)) {
      throw new InvalidParameterException("Invalid key type");
    }
    chooseFirstProvider();
    return spi.engineUnwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
  }

  public static final int getMaxAllowedKeyLength(String transformation) throws NoSuchAlgorithmException {
    if (transformation == null) {
      throw new NullPointerException("transformation == null");
    }
    tokenizeTransformation(transformation);
    return Integer.MAX_VALUE;
  }

  public static final AlgorithmParameterSpec getMaxAllowedParameterSpec(String transformation)
      throws NoSuchAlgorithmException {
    if (transformation == null) {
      throw new NullPointerException("transformation == null");
    }
    tokenizeTransformation(transformation);
    return null;
  }

  public final void updateAAD(byte[] src) {
    if (src == null) {
      throw new IllegalArgumentException("src buffer is null");
    }
    updateAAD(src, 0, src.length);
  }

  public final void updateAAD(byte[] src, int offset, int len) {
    checkCipherState();
    if ((src == null) || (offset < 0) || (len < 0) || len > (src.length - offset)) {
      throw new IllegalArgumentException("Bad arguments");
    }
    chooseFirstProvider();
    if (len == 0) {
      return;
    }
    spi.engineUpdateAAD(src, offset, len);
  }

  public final void updateAAD(ByteBuffer src) {
    checkCipherState();
    if (src == null) {
      throw new IllegalArgumentException("src ByteBuffer is null");
    }
    chooseFirstProvider();
    if (src.remaining() == 0) {
      return;
    }
    spi.engineUpdateAAD(src);
  }

  @Override
  public String toString() {
    return "Cipher." + transformation + ", mode: " + getOpmodeString(opmode) + ", algorithm from: "
        + ((provider == null) ? "(no provider)" : provider.getName());
  }
}
