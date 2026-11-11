/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.sasl;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

public class Sasl {
  private Sasl() {
  }

  public static final String QOP = "javax.security.sasl.qop";
  public static final String STRENGTH = "javax.security.sasl.strength";
  public static final String SERVER_AUTH = "javax.security.sasl.server.authentication";
  public static final String BOUND_SERVER_NAME = "javax.security.sasl.bound.server.name";
  public static final String MAX_BUFFER = "javax.security.sasl.maxbuffer";
  public static final String RAW_SEND_SIZE = "javax.security.sasl.rawsendsize";
  public static final String REUSE = "javax.security.sasl.reuse";
  public static final String POLICY_NOPLAINTEXT = "javax.security.sasl.policy.noplaintext";
  public static final String POLICY_NOACTIVE = "javax.security.sasl.policy.noactive";
  public static final String POLICY_NODICTIONARY = "javax.security.sasl.policy.nodictionary";
  public static final String POLICY_NOANONYMOUS = "javax.security.sasl.policy.noanonymous";
  public static final String POLICY_FORWARD_SECRECY = "javax.security.sasl.policy.forward";
  public static final String POLICY_PASS_CREDENTIALS = "javax.security.sasl.policy.credentials";
  public static final String CREDENTIALS = "javax.security.sasl.credentials";

  public static SaslClient createSaslClient(String[] mechanisms, String authorizationId, String protocol,
      String serverName, Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    SaslClient mech = null;
    SaslClientFactory fac;
    String className;
    String mechName;
    for (int j = 0; j < mechanisms.length; j++) {
      if ((mechName = mechanisms[j]) == null) {
        throw new NullPointerException("Mechanism name cannot be null");
      } else if (mechName.length() == 0) {
        continue;
      }
      String mechFilter = "SaslClientFactory." + mechName;
      Provider[] provs = Security.getProviders(mechFilter);
      for (int i = 0; provs != null && i < provs.length; i++) {
        className = provs[i].getProperty(mechFilter);
        if (className == null) {
          continue;
        }
        fac = (SaslClientFactory) loadFactory(provs[i], className);
        if (fac != null) {
          mech = fac.createSaslClient(new String[] {mechanisms[j]}, authorizationId, protocol, serverName, props, cbh);
          if (mech != null) {
            return mech;
          }
        }
      }
      mech = new com.sun.security.sasl.ClientFactoryImpl().createSaslClient(new String[] {mechName}, authorizationId,
          protocol, serverName, props, cbh);
      if (mech != null) {
        return mech;
      }
    }
    return null;
  }

  private static Object loadFactory(Provider p, String className) throws SaslException {
    try {
      ClassLoader cl = p.getClass().getClassLoader();
      Class<?> implClass = Class.forName(className, true, cl);
      return implClass.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException e) {
      throw new SaslException("Cannot load class " + className, e);
    } catch (ReflectiveOperationException e) {
      throw new SaslException("Cannot instantiate class " + className, e);
    } catch (SecurityException e) {
      throw new SaslException("Cannot access class " + className, e);
    }
  }

  public static SaslServer createSaslServer(String mechanism, String protocol, String serverName,
      Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    SaslServer mech = null;
    SaslServerFactory fac;
    String className;
    if (mechanism == null) {
      throw new NullPointerException("Mechanism name cannot be null");
    } else if (mechanism.length() == 0) {
      return null;
    }
    String mechFilter = "SaslServerFactory." + mechanism;
    Provider[] provs = Security.getProviders(mechFilter);
    for (int i = 0; provs != null && i < provs.length; i++) {
      className = provs[i].getProperty(mechFilter);
      if (className == null) {
        throw new SaslException("Provider does not support " + mechFilter);
      }
      fac = (SaslServerFactory) loadFactory(provs[i], className);
      if (fac != null) {
        mech = fac.createSaslServer(mechanism, protocol, serverName, props, cbh);
        if (mech != null) {
          return mech;
        }
      }
    }
    return new com.sun.security.sasl.ServerFactoryImpl().createSaslServer(mechanism, protocol, serverName, props, cbh);
  }

  public static Enumeration<SaslClientFactory> getSaslClientFactories() {
    List<SaslClientFactory> list = new ArrayList<SaslClientFactory>();
    for (Provider p : Security.getProviders()) {
      for (Object k : p.keySet()) {
        String key = String.valueOf(k);
        if (key.startsWith("SaslClientFactory.")) {
          try {
            list.add((SaslClientFactory) loadFactory(p, p.getProperty(key)));
          } catch (SaslException e) {
          }
        }
      }
    }
    return Collections.enumeration(list);
  }

  public static Enumeration<SaslServerFactory> getSaslServerFactories() {
    List<SaslServerFactory> list = new ArrayList<SaslServerFactory>();
    for (Provider p : Security.getProviders()) {
      for (Object k : p.keySet()) {
        String key = String.valueOf(k);
        if (key.startsWith("SaslServerFactory.")) {
          try {
            list.add((SaslServerFactory) loadFactory(p, p.getProperty(key)));
          } catch (SaslException e) {
          }
        }
      }
    }
    return Collections.enumeration(list);
  }
}
