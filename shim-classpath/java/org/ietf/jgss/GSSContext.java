/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.ietf.jgss;

public interface GSSContext {
  int DEFAULT_LIFETIME = 0;
  int INDEFINITE_LIFETIME = 2147483647;

  byte[] acceptSecContext(byte[] bs, int i, int i2) throws org.ietf.jgss.GSSException;
  void acceptSecContext(java.io.InputStream inputStream, java.io.OutputStream outputStream) throws org.ietf.jgss.GSSException;
  void dispose() throws org.ietf.jgss.GSSException;
  byte[] export() throws org.ietf.jgss.GSSException;
  boolean getAnonymityState();
  boolean getConfState();
  boolean getCredDelegState();
  org.ietf.jgss.GSSCredential getDelegCred() throws org.ietf.jgss.GSSException;
  boolean getIntegState();
  int getLifetime();
  byte[] getMIC(byte[] bs, int i, int i2, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  void getMIC(java.io.InputStream inputStream, java.io.OutputStream outputStream, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  org.ietf.jgss.Oid getMech() throws org.ietf.jgss.GSSException;
  boolean getMutualAuthState();
  boolean getReplayDetState();
  boolean getSequenceDetState();
  org.ietf.jgss.GSSName getSrcName() throws org.ietf.jgss.GSSException;
  org.ietf.jgss.GSSName getTargName() throws org.ietf.jgss.GSSException;
  int getWrapSizeLimit(int i, boolean flag, int i2) throws org.ietf.jgss.GSSException;
  byte[] initSecContext(byte[] bs, int i, int i2) throws org.ietf.jgss.GSSException;
  int initSecContext(java.io.InputStream inputStream, java.io.OutputStream outputStream) throws org.ietf.jgss.GSSException;
  boolean isEstablished();
  boolean isInitiator() throws org.ietf.jgss.GSSException;
  boolean isProtReady();
  boolean isTransferable() throws org.ietf.jgss.GSSException;
  void requestAnonymity(boolean flag) throws org.ietf.jgss.GSSException;
  void requestConf(boolean flag) throws org.ietf.jgss.GSSException;
  void requestCredDeleg(boolean flag) throws org.ietf.jgss.GSSException;
  void requestInteg(boolean flag) throws org.ietf.jgss.GSSException;
  void requestLifetime(int i) throws org.ietf.jgss.GSSException;
  void requestMutualAuth(boolean flag) throws org.ietf.jgss.GSSException;
  void requestReplayDet(boolean flag) throws org.ietf.jgss.GSSException;
  void requestSequenceDet(boolean flag) throws org.ietf.jgss.GSSException;
  void setChannelBinding(org.ietf.jgss.ChannelBinding channelBinding) throws org.ietf.jgss.GSSException;
  byte[] unwrap(byte[] bs, int i, int i2, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  void unwrap(java.io.InputStream inputStream, java.io.OutputStream outputStream, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  void verifyMIC(byte[] bs, int i, int i2, byte[] bs2, int i3, int i4, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  void verifyMIC(java.io.InputStream inputStream, java.io.InputStream inputStream2, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  byte[] wrap(byte[] bs, int i, int i2, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
  void wrap(java.io.InputStream inputStream, java.io.OutputStream outputStream, org.ietf.jgss.MessageProp messageProp) throws org.ietf.jgss.GSSException;
}
