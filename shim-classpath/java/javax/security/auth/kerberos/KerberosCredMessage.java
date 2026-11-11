/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.util.Arrays;
import javax.security.auth.Destroyable;

public final class KerberosCredMessage implements Destroyable {
  private KerberosPrincipal sender;
  private KerberosPrincipal recipient;
  private byte[] message;
  private boolean destroyed = false;

  public KerberosCredMessage(KerberosPrincipal sender, KerberosPrincipal recipient, byte[] message) {
    if (sender == null || recipient == null || message == null) {
      throw new NullPointerException();
    }
    this.sender = sender;
    this.recipient = recipient;
    this.message = message.clone();
  }

  public KerberosPrincipal getSender() {
    return sender;
  }

  public KerberosPrincipal getRecipient() {
    return recipient;
  }

  public byte[] getEncoded() {
    if (destroyed) {
      throw new IllegalStateException("This object is no longer valid");
    }
    return message.clone();
  }

  public void destroy() {
    if (!destroyed) {
      destroyed = true;
      Arrays.fill(message, (byte) 0);
    }
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  public String toString() {
    if (destroyed) {
      return "Destroyed KerberosCredMessage";
    }
    return "KRB_CRED from " + sender + " to " + recipient;
  }

  public int hashCode() {
    return Arrays.hashCode(message) * 31 + sender.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof KerberosCredMessage)) {
      return false;
    }
    KerberosCredMessage o = (KerberosCredMessage) obj;
    return !destroyed && !o.destroyed && sender.equals(o.sender) && recipient.equals(o.recipient)
        && Arrays.equals(message, o.message);
  }
}
