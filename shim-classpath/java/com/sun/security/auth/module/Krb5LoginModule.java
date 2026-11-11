/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.auth.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.KeyTab;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import sun.security.krb5.CCache;
import sun.security.krb5.Config;
import sun.security.krb5.Creds;
import sun.security.krb5.Crypto;
import sun.security.krb5.KrbException;
import sun.security.krb5.KrbProto;
import sun.security.krb5.PrincipalName;

public class Krb5LoginModule implements LoginModule {
  private Subject subject;
  private CallbackHandler callbackHandler;
  private Map<String, Object> sharedState;
  private Map<String, ?> options;

  private boolean debug;
  private boolean storeKey;
  private boolean doNotPrompt;
  private boolean useTicketCache;
  private boolean useKeyTab;
  private boolean isInitiator = true;
  private boolean tryFirstPass;
  private boolean useFirstPass;
  private boolean storePass;
  private boolean clearPass;
  private boolean refreshKrb5Config;
  private boolean renewTGT;
  private String ticketCacheName;
  private String keyTabName;
  private String princName;

  private boolean succeeded;
  private boolean commitSucceeded;
  private Creds creds;
  private PrincipalName principal;
  private char[] password;
  private KeyTab keyTab;
  private List<KerberosKey> storedKeys = new ArrayList<KerberosKey>();
  private KerberosTicket ticket;
  private KerberosPrincipal kerbPrincipal;
  private boolean addedKeyTab;

  public Krb5LoginModule() {
  }

  @SuppressWarnings("unchecked")
  public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
      Map<String, ?> options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = (Map<String, Object>) sharedState;
    this.options = options;
    debug = bool("debug");
    storeKey = bool("storeKey");
    doNotPrompt = bool("doNotPrompt");
    useTicketCache = bool("useTicketCache");
    useKeyTab = bool("useKeyTab");
    tryFirstPass = bool("tryFirstPass");
    useFirstPass = bool("useFirstPass");
    storePass = bool("storePass");
    clearPass = bool("clearPass");
    refreshKrb5Config = bool("refreshKrb5Config");
    renewTGT = bool("renewTGT");
    if (options.get("isInitiator") != null) {
      isInitiator = bool("isInitiator");
    }
    ticketCacheName = (String) options.get("ticketCache");
    keyTabName = (String) options.get("keyTab");
    princName = (String) options.get("principal");
    if (bool("useDefaultCcache")) {
      useTicketCache = true;
    }
  }

  private boolean bool(String k) {
    Object v = options.get(k);
    return v != null && v.toString().equalsIgnoreCase("true");
  }

  public boolean login() throws LoginException {
    if (refreshKrb5Config) {
      try {
        Config.refresh();
      } catch (KrbException e) {
        throw wrap(e);
      }
    }
    if (doNotPrompt && !useTicketCache && !useKeyTab && !tryFirstPass && !useFirstPass) {
      throw new LoginException("Configuration Error - either doNotPrompt should be  false or at least one of"
          + " useTicketCache,  useKeyTab, tryFirstPass and useFirstPass should be true");
    }
    if (ticketCacheName != null && !useTicketCache) {
      throw new LoginException("Configuration Error - useTicketCache should be set when ticketCache is set");
    }
    if (keyTabName != null && !useKeyTab) {
      throw new LoginException("Configuration Error - useKeyTab should be set to true to use the keytab"
          + keyTabName);
    }
    try {
      attempt();
    } catch (KrbException e) {
      throw wrap(e);
    }
    succeeded = true;
    return true;
  }

  private LoginException wrap(KrbException e) {
    LoginException le = new LoginException(e.getMessage());
    le.initCause(e);
    return le;
  }

  private void attempt() throws LoginException, KrbException {
    String realmDefault = Config.getInstance().defaultRealm();
    if (useTicketCache && isInitiator && readCache(realmDefault)) {
      return;
    }
    if (princName == null) {
      princName = promptName();
    }
    if (princName == null) {
      throw new LoginException("Unable to obtain Principal Name for authentication ");
    }
    principal = new PrincipalName(princName, PrincipalName.KRB_NT_PRINCIPAL, realmDefault);
    if (useKeyTab) {
      File f = keyTabName != null ? new File(keyTabName) : null;
      KerberosPrincipal kp = new KerberosPrincipal(principal.toString());
      keyTab = f != null ? KeyTab.getInstance(kp, f) : KeyTab.getInstance(kp);
      if (keyTab.exists() && keyTab.getKeys(kp).length > 0) {
        if (isInitiator) {
          final KerberosKey[] ks = keyTab.getKeys(kp);
          creds = KrbProto.asExchange(principal, new KrbProto.KeyProvider() {
            public byte[] key(int etype, String salt, byte[] params) {
              for (KerberosKey k : ks) {
                if (k.getKeyType() == etype) {
                  return k.getEncoded();
                }
              }
              return null;
            }
          });
        }
        kerbPrincipal = kp;
        return;
      }
      keyTab = null;
      if (doNotPrompt) {
        throw new LoginException("Unable to obtain password from user\n");
      }
    }
    getPassword();
    final char[] pw = password;
    if (pw == null) {
      throw new LoginException("Unable to obtain password from user\n");
    }
    if (isInitiator) {
      creds = KrbProto.asExchange(principal, new KrbProto.KeyProvider() {
        public byte[] key(int etype, String salt, byte[] params) {
          return Crypto.string2key(etype, pw, salt, params);
        }
      });
    }
    if (storeKey) {
      String salt = principal.getRealmString();
      for (String s : principal.getNameStrings()) {
        salt += s;
      }
      int[] ets = Config.getInstance().etypes("default_tkt_enctypes");
      KerberosPrincipal kp = new KerberosPrincipal(principal.toString());
      for (int et : ets) {
        storedKeys.add(new KerberosKey(kp, Crypto.string2key(et, pw, salt, Crypto.defaultString2KeyParams(et)), et, 0));
      }
    }
    kerbPrincipal = new KerberosPrincipal(principal.toString());
  }

  private boolean readCache(String realmDefault) throws KrbException {
    String path = ticketCacheName != null ? ticketCacheName : CCache.defaultPath();
    if (path == null) {
      return false;
    }
    File f = new File(path);
    if (!f.isFile()) {
      return false;
    }
    try {
      CCache cc = CCache.read(f);
      PrincipalName want = princName == null ? null
          : new PrincipalName(princName, PrincipalName.KRB_NT_PRINCIPAL, cc.defaultPrincipal.getRealmString());
      if (want != null && !want.equals(cc.defaultPrincipal)) {
        return false;
      }
      for (Creds c : cc.creds) {
        if (c.server.isKrbtgt() && c.server.getNameStrings()[1].equals(c.server.getRealmString())
            && c.endTime > System.currentTimeMillis()) {
          creds = c;
          principal = cc.defaultPrincipal;
          kerbPrincipal = new KerberosPrincipal(principal.toString());
          return true;
        }
      }
    } catch (IOException e) {
      return false;
    }
    return false;
  }

  private String promptName() throws LoginException {
    if (tryFirstPass || useFirstPass) {
      Object n = sharedState == null ? null : sharedState.get("javax.security.auth.login.name");
      if (n != null) {
        return n.toString();
      }
    }
    if (doNotPrompt) {
      return null;
    }
    if (callbackHandler == null) {
      throw new LoginException("No CallbackHandler available to garner authentication information from the user");
    }
    NameCallback nc = new NameCallback("Kerberos username [" + System.getProperty("user.name") + "]: ");
    try {
      callbackHandler.handle(new Callback[] {nc});
    } catch (IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException(uce.getMessage() + " not available to garner authentication information from the user");
    }
    String n = nc.getName();
    return n == null || n.isEmpty() ? System.getProperty("user.name") : n;
  }

  private void getPassword() throws LoginException {
    if (tryFirstPass || useFirstPass) {
      Object p = sharedState == null ? null : sharedState.get("javax.security.auth.login.password");
      if (p instanceof char[]) {
        password = (char[]) p;
        return;
      }
      if (useFirstPass) {
        return;
      }
    }
    if (doNotPrompt) {
      return;
    }
    if (callbackHandler == null) {
      throw new LoginException("No CallbackHandler available to garner authentication information from the user");
    }
    PasswordCallback pc = new PasswordCallback("Password for " + princName + ": ", false);
    try {
      callbackHandler.handle(new Callback[] {pc});
    } catch (IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException(uce.getMessage() + " not available to garner authentication information from the user");
    }
    password = pc.getPassword();
    pc.clearPassword();
  }

  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    }
    if (storePass && sharedState != null && !useFirstPass && !tryFirstPass) {
      sharedState.put("javax.security.auth.login.name", princName);
      if (password != null) {
        sharedState.put("javax.security.auth.login.password", password);
      }
    }
    if (subject.isReadOnly()) {
      cleanup();
      throw new LoginException("Subject is Readonly");
    }
    subject.getPrincipals().add(kerbPrincipal);
    if (creds != null) {
      ticket = new KerberosTicket(creds.ticket, new KerberosPrincipal(creds.client.toString(),
          creds.client.getNameType()), new KerberosPrincipal(creds.server.toString(), creds.server.getNameType()),
          creds.key, creds.keyType, creds.flagArray(), new Date(creds.authTime), new Date(creds.startTime),
          new Date(creds.endTime), creds.renewTill < 0 ? null : new Date(creds.renewTill), creds.addresses);
      subject.getPrivateCredentials().add(ticket);
    }
    if (keyTab != null && !isInitiator) {
      subject.getPrivateCredentials().add(keyTab);
      addedKeyTab = true;
    }
    if (storeKey) {
      for (KerberosKey k : storedKeys) {
        subject.getPrivateCredentials().add(k);
      }
    }
    if (clearPass) {
      password = null;
      if (sharedState != null) {
        sharedState.remove("javax.security.auth.login.name");
        sharedState.remove("javax.security.auth.login.password");
      }
    }
    commitSucceeded = true;
    return true;
  }

  private void cleanup() {
    creds = null;
    password = null;
    storedKeys.clear();
    keyTab = null;
  }

  public boolean abort() throws LoginException {
    if (!succeeded) {
      return false;
    }
    if (commitSucceeded) {
      logout();
    } else {
      succeeded = false;
      cleanup();
    }
    return true;
  }

  public boolean logout() throws LoginException {
    if (subject.isReadOnly()) {
      cleanup();
      throw new LoginException("Subject is Readonly");
    }
    if (kerbPrincipal != null) {
      subject.getPrincipals().remove(kerbPrincipal);
    }
    if (ticket != null) {
      subject.getPrivateCredentials().remove(ticket);
      try {
        ticket.destroy();
      } catch (DestroyFailedException e) {
        // already destroyed
      }
    }
    if (addedKeyTab && keyTab != null) {
      subject.getPrivateCredentials().remove(keyTab);
    }
    for (KerberosKey k : storedKeys) {
      subject.getPrivateCredentials().remove(k);
    }
    succeeded = false;
    commitSucceeded = false;
    cleanup();
    return true;
  }
}
