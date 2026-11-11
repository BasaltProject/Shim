/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public final class KrbCred {
  private static final int kUsage = 14;

  private KrbCred() {
  }

  public static byte[] encode(Creds fwd, Creds sealWith) throws KrbException {
    try {
      byte[] info = Asn1.seq(Asn1.ctx(0, KrbProto.keyBlock(fwd.keyType, fwd.key)),
          Asn1.ctx(1, Asn1.general(fwd.client.getRealmString())), Asn1.ctx(2, fwd.client.encode()),
          Asn1.ctx(3, Asn1.bits32(fwd.flags)), Asn1.ctx(4, Asn1.time(fwd.authTime)),
          Asn1.ctx(5, Asn1.time(fwd.startTime)), Asn1.ctx(6, Asn1.time(fwd.endTime)),
          fwd.renewTill < 0 ? null : Asn1.ctx(7, Asn1.time(fwd.renewTill)),
          Asn1.ctx(8, Asn1.general(fwd.server.getRealmString())), Asn1.ctx(9, fwd.server.encode()));
      byte[] part = Asn1.app(29, Asn1.seq(Asn1.ctx(0, Asn1.seq(info))));
      byte[] enc = Crypto.encrypt(sealWith.keyType, sealWith.key, kUsage, part);
      return Asn1.app(22, Asn1.seq(Asn1.ctx(0, Asn1.integer(5)), Asn1.ctx(1, Asn1.integer(22)),
          Asn1.ctx(2, Asn1.seq(fwd.ticket)), Asn1.ctx(3, KrbProto.encData(sealWith.keyType, -1, enc))));
    } catch (GeneralSecurityException e) {
      throw new KrbCryptoException(e.getMessage());
    }
  }

  public static Creds decode(byte[] message, Creds sealedWith) throws KrbException {
    try {
      Asn1.Node cred = Asn1.parse(message).inner();
      List<Asn1.Node> tickets = cred.need(2).children();
      Asn1.Node ep = cred.need(3);
      byte[] plain = Crypto.decrypt(ep.need(0).asInt(), sealedWith.key, kUsage, ep.need(2).content());
      Asn1.Node info = Asn1.parse(plain).inner().need(0).children().get(0);
      Creds c = new Creds();
      c.ticket = tickets.get(0).encoded();
      Asn1.Node k = info.need(0);
      c.keyType = k.need(0).asInt();
      c.key = k.need(1).content();
      c.client = PrincipalName.decode(info.need(2), info.need(1).asString());
      c.flags = info.need(3).asBits32();
      c.authTime = info.field(4) == null ? 0 : info.field(4).asTime();
      c.startTime = info.field(5) == null ? c.authTime : info.field(5).asTime();
      c.endTime = info.need(6).asTime();
      c.renewTill = info.field(7) == null ? -1 : info.field(7).asTime();
      c.server = PrincipalName.decode(info.need(9), info.need(8).asString());
      return c;
    } catch (IOException | GeneralSecurityException | IndexOutOfBoundsException e) {
      KrbException ke = new KrbException(e.getMessage() == null ? e.toString() : e.getMessage());
      ke.initCause(e);
      throw ke;
    }
  }
}
