/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public abstract class CertPath implements Serializable {
  private static final long serialVersionUID = 6068470306649138683L;

  private final String type;

  protected CertPath(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public abstract Iterator<String> getEncodings();

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CertPath)) {
      return false;
    }
    CertPath otherCP = (CertPath) other;
    if (!otherCP.getType().equals(type)) {
      return false;
    }
    List<? extends Certificate> thisCertList = this.getCertificates();
    List<? extends Certificate> otherCertList = otherCP.getCertificates();
    return thisCertList.equals(otherCertList);
  }

  @Override
  public int hashCode() {
    int hashCode = type.hashCode();
    hashCode = 31 * hashCode + getCertificates().hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Iterator<? extends Certificate> stringIterator = getCertificates().iterator();
    sb.append("\n" + type + " Cert Path: length = " + getCertificates().size() + ".\n");
    sb.append("[\n");
    int i = 1;
    while (stringIterator.hasNext()) {
      sb.append("=========================================================Certificate " + i + " start.\n");
      Certificate stringCert = stringIterator.next();
      sb.append(stringCert.toString());
      sb.append("\n=========================================================Certificate " + i + " end.\n\n\n");
      i++;
    }
    sb.append("\n]");
    return sb.toString();
  }

  public abstract byte[] getEncoded() throws CertificateEncodingException;

  public abstract byte[] getEncoded(String encoding) throws CertificateEncodingException;

  public abstract List<? extends Certificate> getCertificates();

  protected Object writeReplace() throws ObjectStreamException {
    try {
      return new CertPathRep(type, getEncoded());
    } catch (CertificateException ce) {
      NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + type);
      nse.initCause(ce);
      throw nse;
    }
  }

  protected static class CertPathRep implements Serializable {
    private static final long serialVersionUID = 3015633072427920915L;

    private String type;
    private byte[] data;

    protected CertPathRep(String type, byte[] data) {
      this.type = type;
      this.data = data;
    }

    protected Object readResolve() throws ObjectStreamException {
      try {
        CertificateFactory cf = CertificateFactory.getInstance(type);
        return cf.generateCertPath(new ByteArrayInputStream(data));
      } catch (CertificateException ce) {
        NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + type);
        nse.initCause(ce);
        throw nse;
      }
    }
  }
}
