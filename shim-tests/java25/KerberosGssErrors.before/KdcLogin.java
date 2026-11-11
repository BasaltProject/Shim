/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.KeyTab;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

final class KdcLogin {
  static final org.ietf.jgss.Oid KRB5;
  static final org.ietf.jgss.Oid SPNEGO;

  static {
    try {
      KRB5 = new org.ietf.jgss.Oid("1.2.840.113554.1.2.2");
      SPNEGO = new org.ietf.jgss.Oid("1.3.6.1.5.5.2");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static Subject loginAlice(String entry, Map<String, String> opts, String password) throws Exception {
    return login(entry, opts, "alice", password == null ? "" : password);
  }

  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try {
      Object o = c.call();
      System.out.println(n + " -> " + (o instanceof byte[] ? "bytes" + (((byte[]) o).length > 100 ? "long" : String.valueOf(((byte[]) o).length))
          : o instanceof Object[] ? Arrays.toString((Object[]) o) : String.valueOf(o).replaceAll("@[0-9a-f]{5,8}", "@H")));
    } catch (Throwable e0) {
      Throwable e = e0;
      while (e instanceof java.security.PrivilegedActionException && e.getCause() != null) {
        e = e.getCause();
      }
      System.out.println(n + " !! " + e.getClass().getName() + ": "
          + (e instanceof ClassCastException ? "" : e.getMessage())
          + (e instanceof org.ietf.jgss.GSSException ? " major=" + ((org.ietf.jgss.GSSException) e).getMajor()
          + " minor=" + ((org.ietf.jgss.GSSException) e).getMinor() + " "
          + ((org.ietf.jgss.GSSException) e).getMajorString() : ""));
    }
  }

  private KdcLogin() {
  }

  static Subject login(String name, final Map<String, String> opts, final String user, final String password)
      throws Exception {
    Configuration conf = new Configuration() {
      public AppConfigurationEntry[] getAppConfigurationEntry(String n) {
        return new AppConfigurationEntry[] {new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>(opts))};
      }
    };
    LoginContext lc = new LoginContext(name, null, new CallbackHandler() {
      public void handle(Callback[] cbs) {
        for (Callback c : cbs) {
          if (c instanceof NameCallback) {
            ((NameCallback) c).setName(user);
          }
          if (c instanceof PasswordCallback) {
            ((PasswordCallback) c).setPassword(password.toCharArray());
          }
        }
      }
    }, conf);
    lc.login();
    return lc.getSubject();
  }

  static String describe(Subject s) {
    StringBuilder sb = new StringBuilder("principals=");
    for (java.security.Principal p : s.getPrincipals()) {
      sb.append(p).append(' ');
    }
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KerberosTicket) {
        KerberosTicket t = (KerberosTicket) o;
        sb.append("\n ticket client=").append(t.getClient()).append(" server=").append(t.getServer())
            .append(" etype=").append(t.getSessionKeyType()).append(" flags=").append(flags(t.getFlags()))
            .append(" addrs=").append(t.getClientAddresses() == null ? "none" : "set")
            .append(" renewable=").append(t.isRenewable()).append(" initial=").append(t.isInitial());
      } else if (o instanceof KerberosKey) {
        KerberosKey k = (KerberosKey) o;
        sb.append("\n key ").append(k.getPrincipal()).append(" type=").append(k.getKeyType()).append(" v=")
            .append(k.getVersionNumber());
      } else if (o instanceof KeyTab) {
        sb.append("\n keytab bound=").append(((KeyTab) o).isBound()).append(" exists=").append(((KeyTab) o).exists());
      } else {
        sb.append("\n other ").append(o.getClass().getName());
      }
    }
    return sb.toString();
  }

  static String flags(boolean[] f) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < f.length; i++) {
      if (f[i]) {
        sb.append(i).append(',');
      }
    }
    return sb.toString();
  }

  static String chain(Throwable e) {
    StringBuilder sb = new StringBuilder(e.getClass().getName() + ": " + e.getMessage());
    for (Throwable c = e.getCause(); c != null; c = c.getCause()) {
      sb.append(" / ").append(c.getClass().getName()).append(": ").append(c.getMessage());
    }
    return sb.toString();
  }

  static String scrub(String s) {
    return s.replaceAll(" till=\\d+", "").replaceAll("addresses=\\d+", "addresses=N");
  }

  static void writeCcache(File f, KerberosTicket t) throws IOException {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    DataOutputStream o = new DataOutputStream(bo);
    o.writeShort(0x0504);
    o.writeShort(0);
    principal(o, t.getClient().getName(), t.getClient().getNameType());
    principal(o, t.getClient().getName(), t.getClient().getNameType());
    principal(o, t.getServer().getName(), t.getServer().getNameType());
    o.writeShort(t.getSessionKeyType());
    byte[] key = t.getSessionKey().getEncoded();
    o.writeInt(key.length);
    o.write(key);
    o.writeInt((int) (t.getAuthTime().getTime() / 1000));
    o.writeInt((int) (t.getStartTime().getTime() / 1000));
    o.writeInt((int) (t.getEndTime().getTime() / 1000));
    o.writeInt(t.getRenewTill() == null ? 0 : (int) (t.getRenewTill().getTime() / 1000));
    o.writeByte(0);
    int flags = 0;
    boolean[] fl = t.getFlags();
    for (int i = 0; i < 32; i++) {
      if (fl[i]) {
        flags |= 0x80000000 >>> i;
      }
    }
    o.writeInt(flags);
    o.writeInt(0);
    o.writeInt(0);
    byte[] tkt = t.getEncoded();
    o.writeInt(tkt.length);
    o.write(tkt);
    o.writeInt(0);
    Files.write(f.toPath(), bo.toByteArray());
  }

  private static void principal(DataOutputStream o, String full, int type) throws IOException {
    int at = full.indexOf('@');
    String[] comps = full.substring(0, at).split("/");
    o.writeInt(type);
    o.writeInt(comps.length);
    byte[] realm = full.substring(at + 1).getBytes("UTF-8");
    o.writeInt(realm.length);
    o.write(realm);
    for (String c : comps) {
      byte[] b = c.getBytes("UTF-8");
      o.writeInt(b.length);
      o.write(b);
    }
  }
}
