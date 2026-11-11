/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss.spi;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

public interface GSSContextSpi {
  byte[] init(byte[] in, int off, int len) throws GSSException;

  byte[] accept(byte[] in, int off, int len) throws GSSException;

  boolean isEstablished();

  boolean isProtReady();

  byte[] wrap(byte[] in, int off, int len, MessageProp prop) throws GSSException;

  byte[] unwrap(byte[] in, int off, int len, MessageProp prop) throws GSSException;

  byte[] getMIC(byte[] in, int off, int len, MessageProp prop) throws GSSException;

  void verifyMIC(byte[] tok, int tokOff, int tokLen, byte[] msg, int msgOff, int msgLen, MessageProp prop)
      throws GSSException;

  int getWrapSizeLimit(int qop, boolean conf, int maxTokenSize) throws GSSException;

  GSSName getSrcName() throws GSSException;

  GSSName getTargName() throws GSSException;

  int getLifetime();

  Oid getMech();

  boolean getMutualAuth();

  boolean getReplayDet();

  boolean getSequenceDet();

  boolean getConf();

  boolean getInteg();

  boolean getCredDeleg();

  boolean getDelegPolicy();

  org.ietf.jgss.GSSCredential getDelegCred() throws GSSException;

  boolean getAnonymity();

  boolean isTransferable();

  Object inquire(com.sun.security.jgss.InquireType type) throws GSSException;

  void dispose();
}
