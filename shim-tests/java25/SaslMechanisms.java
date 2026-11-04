/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.security.sasl.*; import javax.security.auth.callback.*; import java.util.*; import java.nio.charset.StandardCharsets;
public class SaslMechanisms {
  static String hex(byte[] b) { if (b == null) return "null"; StringBuilder s = new StringBuilder(); for (byte x : b) s.append(String.format("%02x", x)); return s.toString(); }
  static CallbackHandler cbh(String user, String pw) { return cbs -> { for (Callback c : cbs) { if (c instanceof NameCallback) ((NameCallback) c).setName(user); else if (c instanceof PasswordCallback) ((PasswordCallback) c).setPassword(pw.toCharArray()); else if (c instanceof AuthorizeCallback) { ((AuthorizeCallback) c).setAuthorized(true); } else throw new UnsupportedCallbackException(c); } }; }
  public static void main(String[] a) throws Exception {
    SaslClient pc = Sasl.createSaslClient(new String[]{"PLAIN"}, "authz", "ldap", "host", null, cbh("bob", "pw"));
    System.out.println(pc.getMechanismName() + " " + pc.hasInitialResponse() + " " + hex(pc.evaluateChallenge(new byte[0])) + " " + pc.isComplete() + " " + pc.getNegotiatedProperty(Sasl.QOP));
    try { pc.evaluateChallenge(new byte[0]); } catch (Exception e) { System.out.println(e); }
    SaslClient ex = Sasl.createSaslClient(new String[]{"EXTERNAL"}, "me", "p", "h", null, null); System.out.println(hex(ex.evaluateChallenge(new byte[0])));
    
    SaslClient cm = Sasl.createSaslClient(new String[]{"CRAM-MD5"}, null, "p", "h", null, cbh("tim", "tanstaaftanstaaf"));
    System.out.println(cm.hasInitialResponse() + " " + new String(cm.evaluateChallenge("<1896.697170952@postoffice.example.net>".getBytes()), StandardCharsets.UTF_8));
    SaslServer srv = Sasl.createSaslServer("CRAM-MD5", "p", "srv.example.com", null, cbh("tim", "pw1"));
    byte[] ch = srv.evaluateResponse(new byte[0]); System.out.println(new String(ch).replaceAll("<[^>]*@", "<X@"));
    SaslClient c2 = Sasl.createSaslClient(new String[]{"CRAM-MD5"}, null, "p", "h", null, cbh("tim", "pw1")); byte[] resp = c2.evaluateChallenge(ch);
    System.out.println(srv.evaluateResponse(resp) + " " + srv.isComplete() + " " + srv.getAuthorizationID());
    SaslServer srv2 = Sasl.createSaslServer("CRAM-MD5", "p", "srv", null, cbh("tim", "WRONG")); byte[] ch2 = srv2.evaluateResponse(new byte[0]);
    try { srv2.evaluateResponse(c2Resp(ch2)); } catch (Exception e) { System.out.println(e.getClass().getName() + ": " + e.getMessage()); }
    Map<String,Object> pol = new HashMap<>(); pol.put(Sasl.POLICY_NOPLAINTEXT, "true");
    System.out.println(Sasl.createSaslClient(new String[]{"PLAIN"}, null, "p", "h", pol, cbh("a", "b")) + " " + (Sasl.createSaslClient(new String[]{"PLAIN", "CRAM-MD5"}, null, "p", "h", pol, cbh("a", "b")).getMechanismName()));
    System.out.println(Sasl.createSaslClient(new String[]{"NOPE"}, null, "p", "h", null, null) + " " + Sasl.createSaslServer("NOPE", "p", "h", null, null));
    try { Sasl.createSaslClient(new String[]{"PLAIN"}, null, "p", "h", null, null); } catch (Exception e) { System.out.println(e); }
    Set<String> names = new TreeSet<>(); for (Enumeration<SaslClientFactory> e = Sasl.getSaslClientFactories(); e.hasMoreElements();) names.addAll(Arrays.asList(e.nextElement().getMechanismNames(null))); System.out.println(names.contains("PLAIN") + " " + names.contains("CRAM-MD5") + " " + names.contains("EXTERNAL"));
    System.out.println(new SaslException("m", new RuntimeException("c")) + " | " + new AuthorizeCallback("a", "z").getAuthorizedID() + " | " + new RealmCallback("p", "d").getDefaultText());
  }
  static byte[] c2Resp(byte[] ch) throws Exception { SaslClient c = Sasl.createSaslClient(new String[]{"CRAM-MD5"}, null, "p", "h", null, cbh("tim", "pw1")); return c.evaluateChallenge(ch); }
}
