/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;

public class KerberosTicket implements Destroyable, Refreshable, java.io.Serializable {
  private static final long serialVersionUID = 7395334370157380539L;

  private static final int FORWARDABLE_TICKET_FLAG = 1;
  private static final int FORWARDED_TICKET_FLAG = 2;
  private static final int PROXIABLE_TICKET_FLAG = 3;
  private static final int PROXY_TICKET_FLAG = 4;
  private static final int POSTDATED_TICKET_FLAG = 6;
  private static final int RENEWABLE_TICKET_FLAG = 8;
  private static final int INITIAL_TICKET_FLAG = 9;
  private static final int NUM_FLAGS = 32;

  private byte[] asn1Encoding;
  private KeyImpl sessionKey;
  private boolean[] flags;
  private Date authTime;
  private Date startTime;
  private Date endTime;
  private Date renewTill;
  private KerberosPrincipal client;
  private KerberosPrincipal server;
  private InetAddress[] clientAddresses;
  private transient boolean destroyed = false;

  public KerberosTicket(byte[] asn1Encoding, KerberosPrincipal client, KerberosPrincipal server, byte[] sessionKey,
      int keyType, boolean[] flags, Date authTime, Date startTime, Date endTime, Date renewTill,
      InetAddress[] clientAddresses) {
    init(asn1Encoding, client, server, sessionKey, keyType, flags, authTime, startTime, endTime, renewTill,
        clientAddresses);
  }

  private void init(byte[] asn1Encoding, KerberosPrincipal client, KerberosPrincipal server, byte[] sessionKey,
      int keyType, boolean[] flags, Date authTime, Date startTime, Date endTime, Date renewTill,
      InetAddress[] clientAddresses) {
    if (sessionKey == null) {
      throw new IllegalArgumentException("Session key for ticket cannot be null");
    }
    init(asn1Encoding, client, server, new KeyImpl(sessionKey, keyType), flags, authTime, startTime, endTime,
        renewTill, clientAddresses);
  }

  private void init(byte[] asn1Encoding, KerberosPrincipal client, KerberosPrincipal server, KeyImpl key,
      boolean[] flags, Date authTime, Date startTime, Date endTime, Date renewTill, InetAddress[] clientAddresses) {
    if (asn1Encoding == null) {
      throw new IllegalArgumentException("ASN.1 encoding of ticket cannot be null");
    }
    this.asn1Encoding = asn1Encoding.clone();
    if (client == null) {
      throw new IllegalArgumentException("Client name in ticket cannot be null");
    }
    this.client = client;
    if (server == null) {
      throw new IllegalArgumentException("Server name in ticket cannot be null");
    }
    this.server = server;
    this.sessionKey = key;
    if (flags != null) {
      if (flags.length >= NUM_FLAGS) {
        this.flags = flags.clone();
      } else {
        this.flags = new boolean[NUM_FLAGS];
        for (int i = 0; i < flags.length; i++) {
          this.flags[i] = flags[i];
        }
      }
    } else {
      this.flags = new boolean[NUM_FLAGS];
    }
    if (renewTill != null) {
      this.renewTill = new Date(renewTill.getTime());
    }
    if (authTime != null) {
      this.authTime = new Date(authTime.getTime());
    }
    if (startTime != null) {
      this.startTime = new Date(startTime.getTime());
    } else {
      this.startTime = this.authTime;
    }
    if (endTime == null) {
      throw new IllegalArgumentException("End time for ticket validity cannot be null");
    }
    this.endTime = new Date(endTime.getTime());
    if (clientAddresses != null) {
      this.clientAddresses = clientAddresses.clone();
    }
  }

  public final KerberosPrincipal getClient() {
    return client;
  }

  public final KerberosPrincipal getServer() {
    return server;
  }

  public final javax.crypto.SecretKey getSessionKey() {
    if (destroyed) {
      throw new IllegalStateException("This ticket is no longer valid");
    }
    return sessionKey;
  }

  public final int getSessionKeyType() {
    if (destroyed) {
      throw new IllegalStateException("This ticket is no longer valid");
    }
    return sessionKey.getKeyType();
  }

  public final boolean isForwardable() {
    return flags == null ? false : flags[FORWARDABLE_TICKET_FLAG];
  }

  public final boolean isForwarded() {
    return flags == null ? false : flags[FORWARDED_TICKET_FLAG];
  }

  public final boolean isProxiable() {
    return flags == null ? false : flags[PROXIABLE_TICKET_FLAG];
  }

  public final boolean isProxy() {
    return flags == null ? false : flags[PROXY_TICKET_FLAG];
  }

  public final boolean isPostdated() {
    return flags == null ? false : flags[POSTDATED_TICKET_FLAG];
  }

  public final boolean isRenewable() {
    return flags == null ? false : flags[RENEWABLE_TICKET_FLAG];
  }

  public final boolean isInitial() {
    return flags == null ? false : flags[INITIAL_TICKET_FLAG];
  }

  public final boolean[] getFlags() {
    return (flags == null ? null : flags.clone());
  }

  public final java.util.Date getAuthTime() {
    return (authTime == null) ? null : (Date) authTime.clone();
  }

  public final java.util.Date getStartTime() {
    return (startTime == null) ? null : (Date) startTime.clone();
  }

  public final java.util.Date getEndTime() {
    return (Date) endTime.clone();
  }

  public final java.util.Date getRenewTill() {
    return (renewTill == null) ? null : (Date) renewTill.clone();
  }

  public final java.net.InetAddress[] getClientAddresses() {
    return (clientAddresses == null) ? null : clientAddresses.clone();
  }

  public final byte[] getEncoded() {
    if (destroyed) {
      throw new IllegalStateException("This ticket is no longer valid");
    }
    return asn1Encoding.clone();
  }

  public boolean isCurrent() {
    return (System.currentTimeMillis() <= getEndTime().getTime());
  }

  public void refresh() throws RefreshFailedException {
    if (destroyed) {
      throw new RefreshFailedException("A destroyed ticket cannot be renewed.");
    }
    if (!isRenewable()) {
      throw new RefreshFailedException("This ticket is not renewable");
    }
    if (System.currentTimeMillis() > getRenewTill().getTime()) {
      throw new RefreshFailedException("This ticket is past its last renewal time.");
    }
    throw new RefreshFailedException("Failed to renew Kerberos Ticket for client " + client + " and server "
        + server + " - no Kerberos KDC is available");
  }

  public void destroy() throws DestroyFailedException {
    if (!destroyed) {
      Arrays.fill(asn1Encoding, (byte) 0);
      client = null;
      server = null;
      sessionKey.destroy();
      flags = null;
      authTime = null;
      startTime = null;
      endTime = null;
      renewTill = null;
      clientAddresses = null;
      destroyed = true;
    }
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  public String toString() {
    if (destroyed) {
      return "Destroyed KerberosTicket";
    }
    StringBuilder caddrBuf = new StringBuilder();
    if (clientAddresses != null) {
      for (int i = 0; i < clientAddresses.length; i++) {
        caddrBuf.append("clientAddresses[" + i + "] = " + clientAddresses[i].toString());
      }
    }
    return ("Ticket (hex) = " + "\n" + new sun.security.util.HexDumpEncoder().encodeBuffer(asn1Encoding) + "\n" + "Client Principal = "
        + client.toString() + "\n" + "Server Principal = " + server.toString() + "\n" + "Session Key = "
        + sessionKey.toString() + "\n" + "Forwardable Ticket " + flags[FORWARDABLE_TICKET_FLAG] + "\n"
        + "Forwarded Ticket " + flags[FORWARDED_TICKET_FLAG] + "\n" + "Proxiable Ticket "
        + flags[PROXIABLE_TICKET_FLAG] + "\n" + "Proxy Ticket " + flags[PROXY_TICKET_FLAG] + "\n"
        + "Postdated Ticket " + flags[POSTDATED_TICKET_FLAG] + "\n" + "Renewable Ticket "
        + flags[RENEWABLE_TICKET_FLAG] + "\n" + "Initial Ticket " + flags[INITIAL_TICKET_FLAG] + "\n"
        + "Auth Time = " + String.valueOf(authTime) + "\n" + "Start Time = " + String.valueOf(startTime) + "\n"
        + "End Time = " + endTime.toString() + "\n" + "Renew Till = " + String.valueOf(renewTill) + "\n"
        + "Client Addresses " + (clientAddresses == null ? " Null " : caddrBuf.toString() + "\n"));
  }

  public int hashCode() {
    int result = 17;
    if (isDestroyed()) {
      return result;
    }
    result = result * 37 + Arrays.hashCode(getEncoded());
    result = result * 37 + endTime.hashCode();
    result = result * 37 + client.hashCode();
    result = result * 37 + server.hashCode();
    result = result * 37 + sessionKey.hashCode();
    if (authTime != null) {
      result = result * 37 + authTime.hashCode();
    }
    if (startTime != null) {
      result = result * 37 + startTime.hashCode();
    }
    if (renewTill != null) {
      result = result * 37 + renewTill.hashCode();
    }
    result = result * 37 + Arrays.hashCode(clientAddresses);
    return result * 37 + Arrays.hashCode(flags);
  }

  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof KerberosTicket)) {
      return false;
    }
    KerberosTicket otherTicket = ((KerberosTicket) other);
    if (isDestroyed() || otherTicket.isDestroyed()) {
      return false;
    }
    if (!Arrays.equals(getEncoded(), otherTicket.getEncoded()) || !endTime.equals(otherTicket.getEndTime())
        || !server.equals(otherTicket.getServer()) || !client.equals(otherTicket.getClient())
        || !sessionKey.equals(otherTicket.getSessionKey()) || !Arrays.equals(clientAddresses, otherTicket.getClientAddresses())
        || !Arrays.equals(flags, otherTicket.getFlags())) {
      return false;
    }
    if (authTime == null) {
      if (otherTicket.getAuthTime() != null) {
        return false;
      }
    } else if (!authTime.equals(otherTicket.getAuthTime())) {
      return false;
    }
    if (startTime == null) {
      if (otherTicket.getStartTime() != null) {
        return false;
      }
    } else if (!startTime.equals(otherTicket.getStartTime())) {
      return false;
    }
    if (renewTill == null) {
      return otherTicket.getRenewTill() == null;
    }
    return renewTill.equals(otherTicket.getRenewTill());
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    if (sessionKey == null) {
      throw new java.io.InvalidObjectException("Session key cannot be null");
    }
    destroyed = false;
  }

  private static final class KeyImpl implements SecretKey {
    private static final long serialVersionUID = -7889313790214321193L;

    private final byte[] keyBytes;
    private final int keyType;
    private transient boolean destroyed;

    KeyImpl(byte[] bytes, int type) {
      keyBytes = bytes.clone();
      keyType = type;
    }

    int getKeyType() {
      if (destroyed) {
        throw new IllegalStateException("This key is no longer valid");
      }
      return keyType;
    }

    public String getAlgorithm() {
      return keyType == 0 ? "NULL" : sun.security.krb5.Crypto.name(keyType);
    }

    public boolean isDestroyed() {
      return destroyed;
    }

    public String getFormat() {
      return "RAW";
    }

    public byte[] getEncoded() {
      if (destroyed) {
        throw new IllegalStateException("This key is no longer valid");
      }
      return keyBytes.clone();
    }

    public void destroy() {
      destroyed = true;
      Arrays.fill(keyBytes, (byte) 0);
    }

    public boolean equals(Object o) {
      return o instanceof KeyImpl && ((KeyImpl) o).keyType == keyType && Arrays.equals(((KeyImpl) o).keyBytes, keyBytes);
    }

    public int hashCode() {
      return Arrays.hashCode(keyBytes) * 37 + keyType;
    }

    public String toString() {
      return "keyType=" + keyType + ", " + keyBytes.length + "-byte key";
    }
  }
}
