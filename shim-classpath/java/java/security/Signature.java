/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.nio.ByteBuffer;
import java.security.Provider.Service;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.Objects;
import sun.security.jca.GetInstance;

public abstract class Signature extends SignatureSpi {
  private String algorithm;
  Provider provider;

  protected static final int UNINITIALIZED = 0;
  protected static final int SIGN = 2;
  protected static final int VERIFY = 3;
  protected int state = UNINITIALIZED;

  protected Signature(String algorithm) {
    this.algorithm = algorithm;
  }

  public static Signature getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Iterator<Service> t = GetInstance.getServices("Signature", algorithm);
    if (!t.hasNext()) {
      throw new NoSuchAlgorithmException(algorithm + " Signature not available");
    }
    NoSuchAlgorithmException failure;
    do {
      Service s = t.next();
      if (isSpi(s)) {
        return new Delegate(s, t, algorithm);
      }
      try {
        GetInstance.Instance instance = GetInstance.getInstance(s, SignatureSpi.class);
        return getInstance(instance, algorithm);
      } catch (NoSuchAlgorithmException e) {
        failure = e;
      }
    } while (t.hasNext());
    throw failure;
  }

  private static Signature getInstance(GetInstance.Instance instance, String algorithm) {
    Signature sig;
    if (instance.impl instanceof Signature) {
      sig = (Signature) instance.impl;
      sig.algorithm = algorithm;
    } else {
      SignatureSpi spi = (SignatureSpi) instance.impl;
      sig = new Delegate(spi, algorithm);
    }
    sig.provider = instance.provider;
    return sig;
  }

  private static final java.util.Map<String,
      Boolean> signatureInfo = new java.util.concurrent.ConcurrentHashMap<String, Boolean>();

  private static boolean isSpi(Service s) {
    if (s.getType().equals("Cipher")) {
      return true;
    }
    String className = s.getClassName();
    Boolean result = signatureInfo.get(className);
    if (result == null) {
      try {
        Object instance = s.newInstance(null);
        result = !(instance instanceof Signature);
        signatureInfo.put(className, result);
      } catch (Exception e) {
        return false;
      }
    }
    return result.booleanValue();
  }

  public static Signature getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Iterator<Service> t = GetInstance.getServices("Signature", algorithm, provider);
    return getInstance(algorithm, t);
  }

  public static Signature getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    Iterator<Service> t = GetInstance.getServices("Signature", algorithm, provider);
    return getInstance(algorithm, t);
  }

  private static Signature getInstance(String algorithm, Iterator<Service> t) throws NoSuchAlgorithmException {
    if (!t.hasNext()) {
      throw new NoSuchAlgorithmException("no such algorithm: " + algorithm + " for provider ");
    }
    Service s = t.next();
    if (isSpi(s)) {
      return new Delegate(s, java.util.Collections.<Service>emptyIterator(), algorithm);
    }
    GetInstance.Instance instance = GetInstance.getInstance(s, SignatureSpi.class);
    return getInstance(instance, algorithm);
  }

  void chooseFirstProvider() {
  }

  public final Provider getProvider() {
    chooseFirstProvider();
    return this.provider;
  }

  public final void initVerify(PublicKey publicKey) throws InvalidKeyException {
    engineInitVerify(publicKey);
    state = VERIFY;
  }

  public final void initVerify(java.security.cert.Certificate certificate) throws InvalidKeyException {
    engineInitVerify(certificate.getPublicKey());
    state = VERIFY;
  }

  public final void initSign(PrivateKey privateKey) throws InvalidKeyException {
    engineInitSign(privateKey);
    state = SIGN;
  }

  public final void initSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
    engineInitSign(privateKey, random);
    state = SIGN;
  }

  public final byte[] sign() throws SignatureException {
    if (state == SIGN) {
      return engineSign();
    }
    throw new SignatureException("object not initialized for signing");
  }

  public final int sign(byte[] outbuf, int offset, int len) throws SignatureException {
    if (outbuf == null) {
      throw new IllegalArgumentException("No output buffer given");
    }
    if (offset < 0 || len < 0) {
      throw new IllegalArgumentException("offset or len is less than 0");
    }
    if (outbuf.length - offset < len) {
      throw new IllegalArgumentException("Output buffer too small for specified offset and length");
    }
    if (state != SIGN) {
      throw new SignatureException("object not initialized for signing");
    }
    return engineSign(outbuf, offset, len);
  }

  public final boolean verify(byte[] signature) throws SignatureException {
    if (state == VERIFY) {
      return engineVerify(signature);
    }
    throw new SignatureException("object not initialized for verification");
  }

  public final boolean verify(byte[] signature, int offset, int length) throws SignatureException {
    if (state == VERIFY) {
      if (signature == null) {
        throw new IllegalArgumentException("signature is null");
      }
      if (offset < 0 || length < 0) {
        throw new IllegalArgumentException("offset or length is less than 0");
      }
      if (signature.length - offset < length) {
        throw new IllegalArgumentException("signature too small for specified offset and length");
      }
      return engineVerify(signature, offset, length);
    }
    throw new SignatureException("object not initialized for verification");
  }

  public final void update(byte b) throws SignatureException {
    if (state == VERIFY || state == SIGN) {
      engineUpdate(b);
    } else {
      throw new SignatureException("object not initialized for signature or verification");
    }
  }

  public final void update(byte[] data) throws SignatureException {
    update(data, 0, data.length);
  }

  public final void update(byte[] data, int off, int len) throws SignatureException {
    if (state == SIGN || state == VERIFY) {
      if (data == null) {
        throw new IllegalArgumentException("data is null");
      }
      if (off < 0 || len < 0) {
        throw new IllegalArgumentException("off or len is less than 0");
      }
      if (data.length - off < len) {
        throw new IllegalArgumentException("data too small for specified offset and length");
      }
      engineUpdate(data, off, len);
    } else {
      throw new SignatureException("object not initialized for signature or verification");
    }
  }

  public final void update(ByteBuffer data) throws SignatureException {
    if ((state != SIGN) && (state != VERIFY)) {
      throw new SignatureException("object not initialized for signature or verification");
    }
    if (data == null) {
      throw new NullPointerException();
    }
    engineUpdate(data);
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  @Override
  public String toString() {
    String initState = "";
    switch (state) {
    case UNINITIALIZED:
      initState = "<not initialized>";
      break;
    case VERIFY:
      initState = "<initialized for verifying>";
      break;
    case SIGN:
      initState = "<initialized for signing>";
      break;
    default:
      break;
    }
    return "Signature object: " + getAlgorithm() + initState;
  }

  @Deprecated
  public final void setParameter(String param, Object value) throws InvalidParameterException {
    engineSetParameter(param, value);
  }

  public final void setParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    engineSetParameter(params);
  }

  public final AlgorithmParameters getParameters() {
    return engineGetParameters();
  }

  @Deprecated
  public final Object getParameter(String param) throws InvalidParameterException {
    return engineGetParameter(param);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    if (this instanceof Cloneable) {
      return super.clone();
    }
    throw new CloneNotSupportedException();
  }

  private static final class Delegate extends Signature {
    private SignatureSpi sigSpi;
    private final Object lock;
    private Service firstService;
    private Iterator<Service> serviceIterator;

    private static final int I_PUB = 1;
    private static final int I_PRIV = 2;
    private static final int I_PRIV_SR = 3;

    Delegate(SignatureSpi sigSpi, String algorithm) {
      super(algorithm);
      this.sigSpi = sigSpi;
      this.lock = null;
    }

    Delegate(Service service, Iterator<Service> iterator, String algorithm) {
      super(algorithm);
      this.firstService = service;
      this.serviceIterator = iterator;
      this.lock = new Object();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
      chooseFirstProvider();
      if (sigSpi instanceof Cloneable) {
        SignatureSpi sigSpiClone = (SignatureSpi) sigSpi.clone();
        Signature that = new Delegate(sigSpiClone, ((Signature) this).algorithm);
        that.provider = ((Signature) this).provider;
        that.state = ((Signature) this).state;
        return that;
      }
      throw new CloneNotSupportedException();
    }

    private static SignatureSpi newInstance(Service s) throws NoSuchAlgorithmException {
      Object o = s.newInstance(null);
      if (!(o instanceof SignatureSpi)) {
        throw new NoSuchAlgorithmException("Not a SignatureSpi: " + o.getClass().getName());
      }
      return (SignatureSpi) o;
    }

    @Override
    void chooseFirstProvider() {
      if (sigSpi != null) {
        return;
      }
      synchronized (lock) {
        if (sigSpi != null) {
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
          if (!isSpi(s)) {
            continue;
          }
          try {
            sigSpi = newInstance(s);
            provider = s.getProvider();
            firstService = null;
            serviceIterator = null;
            return;
          } catch (NoSuchAlgorithmException e) {
            lastException = e;
          }
        }
        ProviderException e = new ProviderException("Could not construct SignatureSpi instance");
        if (lastException != null) {
          e.initCause(lastException);
        }
        throw e;
      }
    }

    private void chooseProvider(int type, Key key, SecureRandom random) throws InvalidKeyException {
      synchronized (lock) {
        if (sigSpi != null) {
          init(sigSpi, type, key, random);
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
          if (!isSpi(s)) {
            continue;
          }
          try {
            SignatureSpi spi = newInstance(s);
            init(spi, type, key, random);
            provider = s.getProvider();
            sigSpi = spi;
            firstService = null;
            serviceIterator = null;
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
        if (lastException instanceof RuntimeException) {
          throw (RuntimeException) lastException;
        }
        String k = (key != null) ? key.getClass().getName() : "(null)";
        throw new InvalidKeyException("No installed provider supports this key: " + k, lastException);
      }
    }

    private void init(SignatureSpi spi, int type, Key key, SecureRandom random) throws InvalidKeyException {
      switch (type) {
      case I_PUB:
        spi.engineInitVerify((PublicKey) key);
        break;
      case I_PRIV:
        spi.engineInitSign((PrivateKey) key);
        break;
      case I_PRIV_SR:
        spi.engineInitSign((PrivateKey) key, random);
        break;
      default:
        throw new AssertionError("Internal error: " + type);
      }
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
      if (sigSpi != null) {
        sigSpi.engineInitVerify(publicKey);
      } else {
        chooseProvider(I_PUB, publicKey, null);
      }
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
      if (sigSpi != null) {
        sigSpi.engineInitSign(privateKey);
      } else {
        chooseProvider(I_PRIV, privateKey, null);
      }
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom sr) throws InvalidKeyException {
      if (sigSpi != null) {
        sigSpi.engineInitSign(privateKey, sr);
      } else {
        chooseProvider(I_PRIV_SR, privateKey, sr);
      }
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
      chooseFirstProvider();
      sigSpi.engineUpdate(b);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
      chooseFirstProvider();
      sigSpi.engineUpdate(b, off, len);
    }

    @Override
    protected void engineUpdate(ByteBuffer data) {
      chooseFirstProvider();
      sigSpi.engineUpdate(data);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
      chooseFirstProvider();
      return sigSpi.engineSign();
    }

    @Override
    protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
      chooseFirstProvider();
      return sigSpi.engineSign(outbuf, offset, len);
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
      chooseFirstProvider();
      return sigSpi.engineVerify(sigBytes);
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
      chooseFirstProvider();
      return sigSpi.engineVerify(sigBytes, offset, length);
    }

    @Override
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
      chooseFirstProvider();
      sigSpi.engineSetParameter(param, value);
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
      chooseFirstProvider();
      sigSpi.engineSetParameter(params);
    }

    @Override
    protected Object engineGetParameter(String param) throws InvalidParameterException {
      chooseFirstProvider();
      return sigSpi.engineGetParameter(param);
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
      chooseFirstProvider();
      return sigSpi.engineGetParameters();
    }
  }
}
