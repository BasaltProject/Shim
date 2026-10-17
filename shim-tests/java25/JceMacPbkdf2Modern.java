/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*;
public class JceMacPbkdf2Modern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H").replaceAll("JceMacPbkdf2Modern\\$\\d", "Anon")); }
  public static void main(String[] a) throws Exception {
    String[] macs = {"HmacMD5","HmacSHA1","HmacSHA224","HmacSHA256","HmacSHA384","HmacSHA512","HmacSHA512/224","HmacSHA512/256","HmacSHA3-224","HmacSHA3-256","HmacSHA3-384","HmacSHA3-512"};
    for (String m : macs) {
      p("kg " + m, () -> { KeyGenerator kg = KeyGenerator.getInstance(m); SecretKey k = kg.generateKey(); return k.getEncoded().length + " " + k.getAlgorithm() + " " + k.getFormat() + " " + kg.getProvider().getName(); });
      p("mac " + m, () -> { Mac mac = Mac.getInstance(m); mac.init(new SecretKeySpec("key".getBytes(), m)); mac.update("The quick brown fox".getBytes()); byte[] r = mac.doFinal(); return mac.getMacLength() + " " + HexFormat.of().formatHex(r) + " " + mac.getAlgorithm() + " " + mac.getProvider().getName(); });
      p("mac big " + m, () -> { Mac mac = Mac.getInstance(m); byte[] key = new byte[200]; for (int i = 0; i < key.length; i++) key[i] = (byte) i; mac.init(new SecretKeySpec(key, "RAW")); mac.update(new byte[1000]); return HexFormat.of().formatHex(mac.doFinal()); });
    }
    p("kg size", () -> { KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256"); kg.init(128); return kg.generateKey().getEncoded().length; });
    p("kg size 7", () -> { KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256"); kg.init(7); return kg.generateKey().getEncoded().length; });
    p("kg size 0", () -> { KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256"); kg.init(0); return kg.generateKey().getEncoded().length; });
    p("kg size -8", () -> { KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256"); kg.init(-8); return kg.generateKey().getEncoded().length; });
    p("kg params", () -> { KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256"); kg.init(new IvParameterSpec(new byte[1])); return "x"; });
    p("kg aes size 100", () -> { KeyGenerator kg = KeyGenerator.getInstance("AES"); kg.init(100); return "x"; });
    p("kg aes default", () -> KeyGenerator.getInstance("AES").generateKey().getEncoded().length);
    p("kg unknown", () -> KeyGenerator.getInstance("FOO"));
    p("kg prov", () -> KeyGenerator.getInstance("AES", "SunJCE").getProvider());
    p("kg prov missing", () -> KeyGenerator.getInstance("AES", "SUN"));
    p("kg prov unknown", () -> KeyGenerator.getInstance("AES", "NOPE"));
    p("mac unknown", () -> Mac.getInstance("FOO"));
    p("mac prov missing", () -> Mac.getInstance("HmacSHA1", "SUN"));
    p("mac no init", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.update((byte) 1); return "x"; });
    p("mac no init final", () -> { Mac m = Mac.getInstance("HmacSHA256"); return m.doFinal(); });
    p("mac no init reset", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.reset(); return "ok " + m.getMacLength(); });
    p("mac null key", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(null); return "x"; });
    p("mac non secret", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new PublicKey() { public String getAlgorithm() { return "x"; } public String getFormat() { return "X.509"; } public byte[] getEncoded() { return new byte[1]; } }); return "x"; });
    p("mac bad params", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x"), new IvParameterSpec(new byte[1])); return "x"; });
    p("mac null encoded", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKey() { public String getAlgorithm() { return "x"; } public String getFormat() { return "RAW"; } public byte[] getEncoded() { return null; } }); return "x"; });
    p("mac update null", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); m.update((byte[]) null); m.update((java.nio.ByteBuffer) null); return "x"; });
    p("mac update range", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); m.update(new byte[4], 3, 4); return "x"; });
    p("mac final buf", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); m.doFinal(new byte[10], 0); return "x"; });
    p("mac final buf ok", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); byte[] out = new byte[40]; m.doFinal(out, 5); return HexFormat.of().formatHex(out); });
    p("mac reuse", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); m.update(new byte[]{1}); byte[] a1 = m.doFinal(); m.update(new byte[]{1}); byte[] a2 = m.doFinal(); m.update(new byte[]{2}); m.reset(); m.update(new byte[]{1}); byte[] a3 = m.doFinal(); return Arrays.equals(a1, a2) + " " + Arrays.equals(a1, a3); });
    p("mac clone", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); m.update(new byte[]{1}); Mac c = (Mac) m.clone(); m.update(new byte[]{2}); c.update(new byte[]{3}); return HexFormat.of().formatHex(m.doFinal()).substring(0, 8) + " " + HexFormat.of().formatHex(c.doFinal()).substring(0, 8); });
    p("mac bytebuffer", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(new byte[4], "x")); java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(10); bb.put(new byte[10]).flip(); m.update(bb); return HexFormat.of().formatHex(m.doFinal()).substring(0, 12) + " " + bb.remaining(); });
    p("mac doFinal(input)", () -> { Mac m = Mac.getInstance("HmacSHA1"); m.init(new SecretKeySpec("k".getBytes(), "x")); return HexFormat.of().formatHex(m.doFinal("abc".getBytes())); });
    p("mac reinit key", () -> { Mac m = Mac.getInstance("HmacSHA1"); m.init(new SecretKeySpec("k".getBytes(), "x")); m.update((byte) 1); m.init(new SecretKeySpec("j".getBytes(), "x")); return HexFormat.of().formatHex(m.doFinal()); });
    p("mac toString", () -> { Mac m = Mac.getInstance("HmacSHA1"); return m.toString().replaceAll("@.*", "@H"); });
    p("mac rfc4231", () -> { Mac m = Mac.getInstance("HmacSHA256"); m.init(new SecretKeySpec(HexFormat.of().parseHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b"), "x")); return HexFormat.of().formatHex(m.doFinal("Hi There".getBytes())); });
    p("mac alias", () -> { Mac m = Mac.getInstance("hmacsha256"); return m.getAlgorithm() + " " + Mac.getInstance("HMACSHA1").getAlgorithm(); });
    p("pbkdf2", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); SecretKey k = f.generateSecret(new PBEKeySpec("password".toCharArray(), "salt".getBytes(), 2, 160)); return HexFormat.of().formatHex(k.getEncoded()) + " " + k.getAlgorithm() + " " + k.getFormat() + " " + (k instanceof javax.crypto.interfaces.PBEKey) + " " + f.getAlgorithm() + " " + f.getProvider().getName(); });
    p("pbkdf2 sha256", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); SecretKey k = f.generateSecret(new PBEKeySpec("password".toCharArray(), "salt".getBytes(), 1000, 256)); return HexFormat.of().formatHex(k.getEncoded()); });
    p("pbkdf2 sha512", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512"); SecretKey k = f.generateSecret(new PBEKeySpec("passwordPASSWORDpassword".toCharArray(), "saltSALTsaltSALTsaltSALTsaltSALTsalt".getBytes(), 4096, 512)); return HexFormat.of().formatHex(k.getEncoded()); });
    p("pbkdf2 key sizes", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); StringBuilder sb = new StringBuilder(); for (int bits : new int[]{8, 100, 128, 255, 256, 257, 520, 1024}) { try { sb.append(bits).append(':').append(f.generateSecret(new PBEKeySpec("p".toCharArray(), "salt".getBytes(), 3, bits)).getEncoded().length).append(' '); } catch (Exception e) { sb.append(bits).append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(' '); } } return sb; });
    p("pbkdf2 no salt", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec("p".toCharArray())));
    p("pbkdf2 no keylength", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec("p".toCharArray(), new byte[8], 3)));
    p("pbkdf2 bad spec", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new SecretKeySpec(new byte[8], "x")));
    p("pbkdf2 null spec", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(null));
    p("pbkdf2 cleared", () -> { PBEKeySpec s = new PBEKeySpec("p".toCharArray(), new byte[8], 3, 128); s.clearPassword(); return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(s); });
    p("pbkdf2 empty pw", () -> HexFormat.of().formatHex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(new char[0], "salt".getBytes(), 2, 128)).getEncoded()));
    p("pbkdf2 unicode", () -> HexFormat.of().formatHex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec("pässwörd€😀".toCharArray(), "salt".getBytes(), 2, 128)).getEncoded()));
    p("pbkdf2 spec back", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); SecretKey k = f.generateSecret(new PBEKeySpec("password".toCharArray(), "salt".getBytes(), 2, 160)); PBEKeySpec s = (PBEKeySpec) f.getKeySpec(k, PBEKeySpec.class); return new String(s.getPassword()) + " " + s.getIterationCount() + " " + s.getKeyLength() + " " + new String(s.getSalt()); });
    p("pbkdf2 pbekey", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); javax.crypto.interfaces.PBEKey k = (javax.crypto.interfaces.PBEKey) f.generateSecret(new PBEKeySpec("password".toCharArray(), "salt".getBytes(), 2, 160)); return new String(k.getPassword()) + " " + k.getIterationCount() + " " + new String(k.getSalt()); });
    p("pbkdf2 translate", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); SecretKey k = f.generateSecret(new PBEKeySpec("password".toCharArray(), "salt".getBytes(), 2, 160)); return f.translateKey(k) == k ? "same" : "different"; });
    p("pbkdf2 translate foreign", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); return f.translateKey(new SecretKeySpec(new byte[8], "AES")); });
    p("pbkdf2 translate null", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").translateKey(null));
    p("skf unknown", () -> SecretKeyFactory.getInstance("FOO"));
    p("skf prov", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "SunJCE").getProvider());
    p("skf prov missing", () -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "SUN"));
    p("skf iter zero", () -> new PBEKeySpec("p".toCharArray(), new byte[8], 0, 128));
    p("skf key neg", () -> new PBEKeySpec("p".toCharArray(), new byte[8], 1, -1));
    p("skf empty salt", () -> new PBEKeySpec("p".toCharArray(), new byte[0], 1, 128));
    p("pbekeyspec getters", () -> { PBEKeySpec s = new PBEKeySpec("pw".toCharArray()); return s.getSalt() + " " + s.getIterationCount() + " " + s.getKeyLength(); });
    p("pbekeyspec cleared", () -> { PBEKeySpec s = new PBEKeySpec("pw".toCharArray()); s.clearPassword(); return s.getPassword(); });
    p("pbekeyspec cleared twice", () -> { PBEKeySpec s = new PBEKeySpec("pw".toCharArray()); s.clearPassword(); s.clearPassword(); return "ok"; });
  }
}
