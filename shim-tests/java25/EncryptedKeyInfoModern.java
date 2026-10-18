/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
public class EncryptedKeyInfoModern {
  static String hex(byte[] b) { StringBuilder sb = new StringBuilder(); for (byte x : b) sb.append(String.format("%02x", x)); return sb.toString(); }
  static byte[] hexb(String h) { byte[] b = new byte[h.length() / 2]; for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16); return b; }
  static byte[] cat(byte[]... p) { int n = 0; for (byte[] x : p) n += x.length; byte[] r = new byte[n]; int o = 0; for (byte[] x : p) { System.arraycopy(x, 0, r, o, x.length); o += x.length; } return r; }
  static byte[] len(int n) { if (n < 128) return new byte[] {(byte) n}; if (n < 256) return new byte[] {(byte) 0x81, (byte) n}; return new byte[] {(byte) 0x82, (byte) (n >> 8), (byte) n}; }
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    byte[] plain = new byte[100]; for (int i = 0; i < plain.length; i++) plain[i] = (byte) i;
    SecretKey k = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128").generateSecret(new PBEKeySpec("password".toCharArray()));
    t("key", () -> k.getAlgorithm() + " " + k.getFormat() + " " + hex(k.getEncoded()) + " " + k.getClass().getSimpleName());
    Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
    c.init(Cipher.ENCRYPT_MODE, k, new PBEParameterSpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, 1000, new IvParameterSpec(new byte[16])));
    byte[] enc = c.doFinal(plain);
    t("enc", () -> hex(enc) + " " + c.getBlockSize() + " " + hex(c.getIV()) + " " + c.getAlgorithm() + " " + c.getOutputSize(10));
    AlgorithmParameters ap = c.getParameters();
    t("params", () -> ap.getAlgorithm() + " " + hex(ap.getEncoded()) + " " + ap.getParameterSpec(PBEParameterSpec.class).getIterationCount() + " " + ap.getParameterSpec(PBEParameterSpec.class).getParameterSpec().getClass().getSimpleName() + " " + ap.toString().replace("\n", "|"));
    byte[] pbes2 = hexb("06092a864886f70d01050d"); byte[] pd = ap.getEncoded();
    byte[] alg = cat(pbes2, pd); byte[] algSeq = cat(new byte[] {0x30}, len(alg.length), alg); byte[] oct = cat(new byte[] {0x04}, len(enc.length), enc); byte[] body = cat(algSeq, oct);
    EncryptedPrivateKeyInfo e = new EncryptedPrivateKeyInfo(cat(new byte[] {0x30}, len(body.length), body));
    t("epki", () -> e.getAlgName() + " " + hex(e.getEncoded()) + " " + hex(e.getEncryptedData()).length() + " " + e.getAlgParameters().getAlgorithm());
    byte[] der = e.getEncoded();
    EncryptedPrivateKeyInfo e2 = new EncryptedPrivateKeyInfo(der);
    t("parsed", () -> e2.getAlgName() + " " + Arrays.equals(e2.getEncoded(), der) + " " + e2.getAlgParameters().getAlgorithm() + " " + Arrays.equals(e2.getEncryptedData(), enc));
    Cipher d = Cipher.getInstance(e2.getAlgName());
    d.init(Cipher.DECRYPT_MODE, k, e2.getAlgParameters());
    t("decrypt", () -> Arrays.equals(d.doFinal(enc), plain));
    t("wrongpw", () -> { Cipher d2 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); d2.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128").generateSecret(new PBEKeySpec("nope".toCharArray())), e2.getAlgParameters()); return d2.doFinal(enc).length; });
    t("alg1", () -> new EncryptedPrivateKeyInfo("PBEWithMD5AndDES", enc).getAlgName());
    t("algBad", () -> new EncryptedPrivateKeyInfo("NoSuchAlg", enc));
    t("algNull", () -> new EncryptedPrivateKeyInfo((String) null, enc));
    t("dataNull", () -> new EncryptedPrivateKeyInfo(ap, null));
    t("dataEmpty", () -> new EncryptedPrivateKeyInfo(ap, new byte[0]));
    t("derNull", () -> new EncryptedPrivateKeyInfo((byte[]) null));
    t("derBad", () -> new EncryptedPrivateKeyInfo(new byte[] {1, 2, 3}));
    t("derEmpty", () -> new EncryptedPrivateKeyInfo(new byte[0]));
    t("dataCopy", () -> { byte[] d1 = e.getEncryptedData(); d1[0] ^= 1; return Arrays.equals(e.getEncryptedData(), enc); });
    for (String n : new String[] {"PBEWithHmacSHA1AndAES_128", "PBEWithHmacSHA224AndAES_256", "PBEWithHmacSHA384AndAES_128", "PBEWithHmacSHA512/224AndAES_128", "PBEWithHmacSHA512/256AndAES_256", "PBEWithHmacSHA512AndAES_256", "PBES2"}) {
      t(n, () -> { Cipher cc = Cipher.getInstance(n); return "cipher " + cc.getAlgorithm() + " " + SecretKeyFactory.getInstance(n).getAlgorithm() + " " + AlgorithmParameters.getInstance(n).getAlgorithm(); });
    }
    for (String n : new String[] {"PBEWithHmacSHA1AndAES_128", "PBEWithHmacSHA224AndAES_256", "PBEWithHmacSHA384AndAES_128", "PBEWithHmacSHA512/224AndAES_128", "PBEWithHmacSHA512/256AndAES_256", "PBEWithHmacSHA512AndAES_256"}) {
      t("vec " + n, () -> { Cipher cc = Cipher.getInstance(n); cc.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance(n).generateSecret(new PBEKeySpec("pw".toCharArray())), new PBEParameterSpec(new byte[8], 100, new IvParameterSpec(new byte[16]))); byte[] o = cc.doFinal("hello".getBytes()); return hex(o) + " " + hex(cc.getParameters().getEncoded()); });
    }
    t("randomParams", () -> { Cipher ce2 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); ce2.init(Cipher.ENCRYPT_MODE, k); PBEParameterSpec rp = ce2.getParameters().getParameterSpec(PBEParameterSpec.class); return rp.getIterationCount() + " " + rp.getSalt().length + " " + ((IvParameterSpec) rp.getParameterSpec()).getIV().length; });
    t("wrongKey", () -> { Cipher ce3 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); ce3.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES")); return 1; });
    t("noIv", () -> { Cipher ce3 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); ce3.init(Cipher.ENCRYPT_MODE, k, new PBEParameterSpec(new byte[8], 1000)); return ce3.getIV().length; });
    t("noIvDecrypt", () -> { Cipher ce3 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); ce3.init(Cipher.DECRYPT_MODE, k, new PBEParameterSpec(new byte[8], 1000)); return 1; });
    t("badSpec", () -> { Cipher ce3 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); ce3.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(new byte[16])); return 1; });
    t("getKeySpecCipher", () -> { Cipher dd = Cipher.getInstance("PBEWithHmacSHA256AndAES_128"); dd.init(Cipher.DECRYPT_MODE, k, e.getAlgParameters()); try { return e.getKeySpec(dd); } catch (Exception x) { return x.getClass().getName(); } });
    t("getKeySpecNull", () -> e.getKeySpec((Cipher) null));
    t("getKeySpecKeyNull", () -> e.getKeySpec((Key) null));
    t("keyspec", () -> new PBEKeySpec("abc".toCharArray(), new byte[] {1}, 5, 128).getKeyLength());
    t("keyFac", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128"); try { f.generateSecret(new SecretKeySpec(new byte[1], "x")); } catch (Exception x) { return x.getClass().getName() + ":" + x.getMessage(); } return 0; });
    t("keyFacTrans", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128"); return f.translateKey(k).getAlgorithm(); });
  }
}
