import java.security.Key;
import java.security.KeyStore;
import java.security.AlgorithmParameters;
import java.util.HexFormat;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoCiphersModern {
  static final HexFormat H = HexFormat.of();

  static byte[] seq(int n, int mul) {
    byte[] b = new byte[n];
    for (int i = 0; i < n; i++) {
      b[i] = (byte) (i * mul + 3);
    }
    return b;
  }

  static void p(Object o) {
    System.out.println(o);
  }

  public static void main(String[] a) throws Exception {
    byte[] pt = seq(37, 7);
    for (int kl : new int[] {4, 16, 56}) {
      SecretKeySpec k = new SecretKeySpec(seq(kl, 5), "Blowfish");
      Cipher c = Cipher.getInstance("Blowfish/ECB/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, k);
      byte[] ct = c.doFinal(seq(32, 3));
      p("bf-ecb " + kl + " " + H.formatHex(ct));
      c.init(Cipher.DECRYPT_MODE, k);
      p(H.formatHex(c.doFinal(ct)).equals(H.formatHex(seq(32, 3))));
      Cipher cbc = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
      cbc.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(seq(8, 9)));
      byte[] e = cbc.doFinal(pt);
      p("bf-cbc " + H.formatHex(e));
      cbc.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(seq(8, 9)));
      p(H.formatHex(cbc.doFinal(e)).equals(H.formatHex(pt)));
    }
    Cipher bfd = Cipher.getInstance("Blowfish");
    bfd.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(seq(16, 1), "Blowfish"));
    p("bf-default " + H.formatHex(bfd.doFinal(pt)) + " " + bfd.getBlockSize());
    KeyGenerator kg = KeyGenerator.getInstance("Blowfish");
    p("bf-kg " + kg.generateKey().getEncoded().length + " " + kg.getAlgorithm());
    kg.init(256);
    p("bf-kg256 " + kg.generateKey().getEncoded().length);

    SecretKeySpec ck = new SecretKeySpec(seq(32, 11), "ChaCha20");
    Cipher cc = Cipher.getInstance("ChaCha20");
    cc.init(Cipher.ENCRYPT_MODE, ck, new ChaCha20ParameterSpec(seq(12, 13), 7));
    byte[] cct = cc.doFinal(pt);
    p("cc20 " + H.formatHex(cct));
    cc.init(Cipher.DECRYPT_MODE, ck, new ChaCha20ParameterSpec(seq(12, 13), 7));
    p(H.formatHex(cc.doFinal(cct)).equals(H.formatHex(pt)));
    Cipher cc2 = Cipher.getInstance("ChaCha20");
    cc2.init(Cipher.ENCRYPT_MODE, ck, new ChaCha20ParameterSpec(seq(12, 13), 1));
    byte[] part = cc2.update(pt, 0, 10);
    byte[] rest = cc2.doFinal(pt, 10, pt.length - 10);
    p("cc20-split " + H.formatHex(part) + H.formatHex(rest));
    try {
      cc2.init(Cipher.ENCRYPT_MODE, ck, new ChaCha20ParameterSpec(seq(12, 13), 1));
    } catch (Exception e) {
      p("reuse " + e.getClass().getName() + " " + e.getMessage());
    }
    KeyGenerator ckg = KeyGenerator.getInstance("ChaCha20");
    p("cc20-kg " + ckg.generateKey().getEncoded().length);

    Cipher cp = Cipher.getInstance("ChaCha20-Poly1305");
    cp.init(Cipher.ENCRYPT_MODE, ck, new IvParameterSpec(seq(12, 17)));
    cp.updateAAD(seq(13, 2));
    byte[] pct = cp.doFinal(pt);
    p("c20p1305 " + pct.length + " " + H.formatHex(pct));
    cp.init(Cipher.DECRYPT_MODE, ck, new IvParameterSpec(seq(12, 17)));
    cp.updateAAD(seq(13, 2));
    p(H.formatHex(cp.doFinal(pct)).equals(H.formatHex(pt)));
    pct[3] ^= 1;
    cp.init(Cipher.DECRYPT_MODE, ck, new IvParameterSpec(seq(12, 17)));
    cp.updateAAD(seq(13, 2));
    try {
      cp.doFinal(pct);
    } catch (AEADBadTagException e) {
      p("badtag " + e.getMessage());
    }
    Cipher e0 = Cipher.getInstance("ChaCha20-Poly1305");
    e0.init(Cipher.ENCRYPT_MODE, ck, new IvParameterSpec(seq(12, 19)));
    p("empty " + H.formatHex(e0.doFinal()));
    p("params " + (e0.getParameters() != null) + " " + H.formatHex(e0.getIV()));

    for (int kl : new int[] {16, 24, 32}) {
      SecretKeySpec kek = new SecretKeySpec(seq(kl, 3), "AES");
      Cipher w = Cipher.getInstance("AESWrap");
      w.init(Cipher.WRAP_MODE, kek);
      SecretKey target = new SecretKeySpec(seq(24, 21), "AES");
      byte[] wrapped = w.wrap(target);
      p("kw " + kl + " " + H.formatHex(wrapped));
      w.init(Cipher.UNWRAP_MODE, kek);
      Key back = w.unwrap(wrapped, "AES", Cipher.SECRET_KEY);
      p(H.formatHex(back.getEncoded()).equals(H.formatHex(target.getEncoded())));
      wrapped[5] ^= 1;
      try {
        w.unwrap(wrapped, "AES", Cipher.SECRET_KEY);
      } catch (Exception e) {
        p("kw-bad " + e.getClass().getName());
      }
    }
    Cipher kw2 = Cipher.getInstance("AES/KW/NoPadding");
    kw2.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(seq(16, 3), "AES"));
    p("kw2 " + H.formatHex(kw2.doFinal(seq(16, 5))) + " " + kw2.getBlockSize());
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(null, null);
    char[] pw = "pw".toCharArray();
    ks.setEntry("sec", new KeyStore.SecretKeyEntry(new SecretKeySpec(seq(32, 23), "AES")), new KeyStore.PasswordProtection(pw));
    ks.setEntry("mac", new KeyStore.SecretKeyEntry(new SecretKeySpec(seq(20, 29), "HmacSHA256")), new KeyStore.PasswordProtection(pw));
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    ks.store(bos, pw);
    KeyStore k2 = KeyStore.getInstance("PKCS12");
    k2.load(new java.io.ByteArrayInputStream(bos.toByteArray()), pw);
    Key sk = k2.getKey("sec", pw);
    p("ks " + java.util.Collections.list(k2.aliases()) + " " + k2.size() + " " + k2.isKeyEntry("sec") + " " + k2.entryInstanceOf("sec", KeyStore.SecretKeyEntry.class)
        + " " + sk.getAlgorithm() + " " + sk.getFormat() + " " + H.formatHex(sk.getEncoded()) + " " + k2.getKey("mac", pw).getAlgorithm());
    try {
      k2.getKey("sec", "bad".toCharArray());
    } catch (Exception e) {
      p("badpw " + e.getClass().getName());
    }
  }
}
