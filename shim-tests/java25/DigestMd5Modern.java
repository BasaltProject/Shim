/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.util.*;
import javax.security.auth.callback.*;
import javax.security.sasl.*;

public class DigestMd5Modern {
  static CallbackHandler h(String user, String pw) {
    return cbs -> {
      for (Callback c : cbs) {
        if (c instanceof NameCallback n) n.setName(user);
        else if (c instanceof PasswordCallback p) p.setPassword(pw.toCharArray());
        else if (c instanceof RealmCallback r) r.setText(r.getDefaultText());
        else if (c instanceof AuthorizeCallback z) z.setAuthorized(true);
        else throw new UnsupportedCallbackException(c);
      }
    };
  }

  public static void main(String[] a) throws Exception {
    for (String qop : new String[] {"auth", "auth-int", "auth-conf"}) {
      Map<String, Object> props = new HashMap<>();
      props.put(Sasl.QOP, qop);
      SaslServer srv = Sasl.createSaslServer("DIGEST-MD5", "imap", "host", props, h("u", "secret"));
      SaslClient cl = Sasl.createSaslClient(new String[] {"DIGEST-MD5"}, null, "imap", "host", props, h("u", "secret"));
      byte[] ch = srv.evaluateResponse(new byte[0]);
      byte[] resp = cl.evaluateChallenge(ch);
      byte[] fin = srv.evaluateResponse(resp);
      cl.evaluateChallenge(fin);
      System.out.println(qop + " " + srv.isComplete() + " " + cl.isComplete() + " " + cl.getNegotiatedProperty(Sasl.QOP));
      if (!qop.equals("auth")) {
        byte[] msg = "payload".getBytes();
        byte[] w = cl.wrap(msg, 0, msg.length);
        System.out.println(new String(srv.unwrap(w, 0, w.length)));
      }
    }
  }
}
