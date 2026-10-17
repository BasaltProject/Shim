/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.net.ServerSocketFactory;

public abstract class SSLServerSocketFactory extends ServerSocketFactory {
  private static SSLServerSocketFactory theFactory;
  private static boolean propertyChecked;

  protected SSLServerSocketFactory() {
  }

  public static synchronized ServerSocketFactory getDefault() {
    if (theFactory != null) {
      return theFactory;
    }
    if (!propertyChecked) {
      propertyChecked = true;
      String clsName = Security.getProperty("ssl.ServerSocketFactory.provider");
      if (clsName != null) {
        try {
          Class<?> cls = Class.forName(clsName, true, ClassLoader.getSystemClassLoader());
          theFactory = (SSLServerSocketFactory) cls.getDeclaredConstructor().newInstance();
          return theFactory;
        } catch (Exception e) {
          theFactory = new DefaultSSLServerSocketFactory(e);
          return theFactory;
        }
      }
    }
    try {
      return SSLContext.getDefault().getServerSocketFactory();
    } catch (NoSuchAlgorithmException e) {
      return new DefaultSSLServerSocketFactory(e);
    }
  }

  public abstract String[] getDefaultCipherSuites();

  public abstract String[] getSupportedCipherSuites();
}
