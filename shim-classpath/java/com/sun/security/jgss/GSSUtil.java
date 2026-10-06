/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.jgss;

import javax.security.auth.Subject;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;

public class GSSUtil {
  private GSSUtil() {
  }

  public static Subject createSubject(GSSName principals, GSSCredential credentials) {
    Subject subject = new Subject();
    if (principals != null) {
      subject.getPrincipals().add(new javax.security.auth.kerberos.KerberosPrincipal(principals.toString()));
    }
    if (credentials != null) {
      subject.getPrivateCredentials().add(credentials);
    }
    return subject;
  }
}
