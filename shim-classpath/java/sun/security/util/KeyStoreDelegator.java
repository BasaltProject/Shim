/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;

public class KeyStoreDelegator extends KeyStoreSpi {
  private final KeyStoreSpi primary;
  private final KeyStoreSpi fallback;
  private KeyStoreSpi current;

  protected KeyStoreDelegator(KeyStoreSpi primary, KeyStoreSpi fallback) {
    this.primary = primary;
    this.fallback = fallback;
    this.current = primary;
  }

  @Override
  public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
    return current.engineGetKey(alias, password);
  }

  @Override
  public Certificate[] engineGetCertificateChain(String alias) {
    return current.engineGetCertificateChain(alias);
  }

  @Override
  public Certificate engineGetCertificate(String alias) {
    return current.engineGetCertificate(alias);
  }

  @Override
  public Date engineGetCreationDate(String alias) {
    return current.engineGetCreationDate(alias);
  }

  @Override
  public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
      throws KeyStoreException {
    current.engineSetKeyEntry(alias, key, password, chain);
  }

  @Override
  public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    current.engineSetKeyEntry(alias, key, chain);
  }

  @Override
  public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    current.engineSetCertificateEntry(alias, cert);
  }

  @Override
  public void engineDeleteEntry(String alias) throws KeyStoreException {
    current.engineDeleteEntry(alias);
  }

  @Override
  public Enumeration<String> engineAliases() {
    return current.engineAliases();
  }

  @Override
  public boolean engineContainsAlias(String alias) {
    return current.engineContainsAlias(alias);
  }

  @Override
  public int engineSize() {
    return current.engineSize();
  }

  @Override
  public boolean engineIsKeyEntry(String alias) {
    return current.engineIsKeyEntry(alias);
  }

  @Override
  public boolean engineIsCertificateEntry(String alias) {
    return current.engineIsCertificateEntry(alias);
  }

  @Override
  public String engineGetCertificateAlias(Certificate cert) {
    return current.engineGetCertificateAlias(cert);
  }

  @Override
  public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    current.engineStore(stream, password);
  }

  @Override
  public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    current = primary;
    if (stream == null) {
      primary.engineLoad(null, password);
      return;
    }
    BufferedInputStream in = new BufferedInputStream(stream);
    in.mark(16);
    boolean primaryOk = primary.engineProbe(in);
    in.reset();
    if (!primaryOk) {
      in.mark(16);
      boolean fallbackOk = fallback.engineProbe(in);
      in.reset();
      if (fallbackOk) {
        current = fallback;
      }
    }
    current.engineLoad(in, password);
  }

  @Override
  public boolean engineProbe(InputStream stream) throws IOException {
    return primary.engineProbe(stream);
  }
}
