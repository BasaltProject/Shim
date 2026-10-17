/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;

public class CertificateRevokedException extends CertificateException {
  private static final long serialVersionUID = 7839996631571608627L;

  private Date revocationDate;
  private final CRLReason reason;
  private final X500Principal authority;
  private transient Map<String, Extension> extensions;

  public CertificateRevokedException(Date revocationDate, CRLReason reason, X500Principal authority,
      Map<String, Extension> extensions) {
    if (revocationDate == null || reason == null || authority == null || extensions == null) {
      throw new NullPointerException();
    }
    this.revocationDate = new Date(revocationDate.getTime());
    this.reason = reason;
    this.authority = authority;
    this.extensions = Collections.unmodifiableMap(new HashMap<String, Extension>(extensions));
  }

  public Date getRevocationDate() {
    return (Date) revocationDate.clone();
  }

  public CRLReason getRevocationReason() {
    return reason;
  }

  public X500Principal getAuthorityName() {
    return authority;
  }

  public Date getInvalidityDate() {
    Extension ext = getExtensions().get("2.5.29.24");
    if (ext == null) {
      return null;
    }
    try {
      byte[] data = ext.getValue();
      sun.security.util.DerValue v = new sun.security.util.DerValue(data);
      Date date = new Date(parseTime(v.getAsString()));
      return date;
    } catch (java.io.IOException ioe) {
      return null;
    }
  }

  private static long parseTime(String s) {
    java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyyMMddHHmmss'Z'");
    f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    try {
      return f.parse(s).getTime();
    } catch (java.text.ParseException e) {
      return 0L;
    }
  }

  public Map<String, Extension> getExtensions() {
    return extensions;
  }

  @Override
  public String getMessage() {
    return "Certificate has been revoked, reason: " + reason + ", revocation date: " + revocationDate
        + ", authority: " + authority + ", extension OIDs: " + extensions.keySet();
  }
}
