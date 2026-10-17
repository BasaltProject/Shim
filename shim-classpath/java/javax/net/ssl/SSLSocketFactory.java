/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.net.SocketFactory;

public abstract class SSLSocketFactory extends SocketFactory {
  private static SSLSocketFactory theFactory;
  private static boolean propertyChecked;

  public SSLSocketFactory() {
  }

  private static void log(String msg) {
  }

  public static synchronized SocketFactory getDefault() {
    if (theFactory != null) {
      return theFactory;
    }
    if (!propertyChecked) {
      propertyChecked = true;
      String clsName = Security.getProperty("ssl.SocketFactory.provider");
      if (clsName != null) {
        log("setting up default SSLSocketFactory");
        try {
          Class<?> cls = Class.forName(clsName, true, ClassLoader.getSystemClassLoader());
          theFactory = (SSLSocketFactory) cls.getDeclaredConstructor().newInstance();
          log("instantiated an instance of class " + clsName);
          return theFactory;
        } catch (Exception e) {
          log("SSLSocketFactory instantiation failed: " + e.toString());
          theFactory = new DefaultSSLSocketFactory(e);
          return theFactory;
        }
      }
    }
    try {
      return SSLContext.getDefault().getSocketFactory();
    } catch (NoSuchAlgorithmException e) {
      return new DefaultSSLSocketFactory(e);
    }
  }

  public abstract String[] getDefaultCipherSuites();

  public abstract String[] getSupportedCipherSuites();

  public abstract Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException;

  public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
    throw new UnsupportedOperationException();
  }
}
