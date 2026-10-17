/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import sun.security.jca.GetInstance;

public abstract class MessageDigest extends MessageDigestSpi {
  private String algorithm;
  private static final int INITIAL = 0;
  private static final int IN_PROGRESS = 1;
  private int state = INITIAL;
  private Provider provider;

  protected MessageDigest(String algorithm) {
    this.algorithm = algorithm;
  }

  public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("MessageDigest", MessageDigestSpi.class, algorithm);
    return wrap(instance, algorithm);
  }

  public static MessageDigest getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance =
        GetInstance.getInstance("MessageDigest", MessageDigestSpi.class, algorithm, provider);
    return wrap(instance, algorithm);
  }

  public static MessageDigest getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance =
        GetInstance.getInstance("MessageDigest", MessageDigestSpi.class, algorithm, provider);
    return wrap(instance, algorithm);
  }

  private static MessageDigest wrap(GetInstance.Instance instance, String algorithm) {
    MessageDigest md;
    if (instance.impl instanceof MessageDigest) {
      md = (MessageDigest) instance.impl;
      md.provider = instance.provider;
    } else {
      md = new Delegate((MessageDigestSpi) instance.impl, algorithm, instance.provider);
    }
    return md;
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public void update(byte input) {
    engineUpdate(input);
    state = IN_PROGRESS;
  }

  public void update(byte[] input, int offset, int len) {
    if (input == null) {
      throw new IllegalArgumentException("No input buffer given");
    }
    if (input.length - offset < len) {
      throw new IllegalArgumentException("Input buffer too short");
    }
    engineUpdate(input, offset, len);
    state = IN_PROGRESS;
  }

  public void update(byte[] input) {
    engineUpdate(input, 0, input.length);
    state = IN_PROGRESS;
  }

  public final void update(ByteBuffer input) {
    if (input == null) {
      throw new NullPointerException();
    }
    engineUpdate(input);
    state = IN_PROGRESS;
  }

  public byte[] digest() {
    byte[] result = engineDigest();
    state = INITIAL;
    return result;
  }

  public int digest(byte[] buf, int offset, int len) throws DigestException {
    if (buf == null) {
      throw new IllegalArgumentException("No output buffer given");
    }
    if (buf.length - offset < len) {
      throw new IllegalArgumentException("Output buffer too small for specified offset and length");
    }
    int numBytes = engineDigest(buf, offset, len);
    state = INITIAL;
    return numBytes;
  }

  public byte[] digest(byte[] input) {
    update(input);
    return digest();
  }

  @Override
  public String toString() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream p = new PrintStream(baos);
    p.print(algorithm + " Message Digest from " + provider.getName() + ", ");
    switch (state) {
    case INITIAL:
      p.print("<initialized>");
      break;
    case IN_PROGRESS:
      p.print("<in progress>");
      break;
    default:
      break;
    }
    p.println();
    return baos.toString();
  }

  public static boolean isEqual(byte[] digesta, byte[] digestb) {
    if (digesta == digestb) {
      return true;
    }
    if (digesta == null || digestb == null) {
      return false;
    }
    int lenA = digesta.length;
    int lenB = digestb.length;
    if (lenB == 0) {
      return lenA == 0;
    }
    int result = 0;
    result |= lenA - lenB;
    for (int i = 0; i < lenA; i++) {
      int indexB = ((i - lenB) >>> 31) * i;
      result |= digesta[i] ^ digestb[indexB];
    }
    return result == 0;
  }

  public void reset() {
    engineReset();
    state = INITIAL;
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public final int getDigestLength() {
    int digestLen = engineGetDigestLength();
    if (digestLen == 0) {
      try {
        MessageDigest md = (MessageDigest) clone();
        byte[] digest = md.digest();
        return digest.length;
      } catch (CloneNotSupportedException e) {
        return digestLen;
      }
    }
    return digestLen;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    if (this instanceof Cloneable) {
      return super.clone();
    }
    throw new CloneNotSupportedException();
  }

  private static final class Delegate extends MessageDigest {
    private MessageDigestSpi digestSpi;

    Delegate(MessageDigestSpi digestSpi, String algorithm, Provider p) {
      super(algorithm);
      this.digestSpi = digestSpi;
      super.provider = p;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
      if (digestSpi instanceof Cloneable) {
        MessageDigestSpi digestSpiClone = (MessageDigestSpi) digestSpi.clone();
        MessageDigest that = new Delegate(digestSpiClone, ((MessageDigest) this).algorithm, super.provider);
        that.state = ((MessageDigest) this).state;
        return that;
      }
      throw new CloneNotSupportedException();
    }

    @Override
    protected int engineGetDigestLength() {
      return digestSpi.engineGetDigestLength();
    }

    @Override
    protected void engineUpdate(byte input) {
      digestSpi.engineUpdate(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
      digestSpi.engineUpdate(input, offset, len);
    }

    @Override
    protected void engineUpdate(ByteBuffer input) {
      digestSpi.engineUpdate(input);
    }

    @Override
    protected byte[] engineDigest() {
      return digestSpi.engineDigest();
    }

    @Override
    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
      return digestSpi.engineDigest(buf, offset, len);
    }

    @Override
    protected void engineReset() {
      digestSpi.engineReset();
    }
  }
}
